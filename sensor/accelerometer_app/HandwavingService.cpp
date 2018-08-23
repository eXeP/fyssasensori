#include "HandwavingService.h"
#include "app-resources/resources.h"
#include "common/core/debug.h"
#include "meas_acc/resources.h"
#include "whiteboard/builtinTypes/UnknownStructure.h"
#include "whiteboard/integration/bsp/shared/debug.h"
#include "component_max3000x/resources.h"
#include "system_mode/resources.h"
#include "ui_ind/resources.h"
#include <float.h>
#include <math.h>

#define ASSERT WB_DEBUG_ASSERT

// Time between wake-up and going to power-off mode
#define AVAILABILITY_TIME 120000

// Time between turn on AFE wake circuit to power off
// (must be LED_BLINKING_PERIOD multiple)
#define WAKE_PREPARATION_TIME 6000

// LED blinking period in adertsing mode
#define LED_BLINKING_PERIOD 6000

const char* const HandwavingService::LAUNCHABLE_NAME = "SampleA";
#define SAMPLE_RATE 13
static const whiteboard::ExecutionContextId sExecutionContextId =
    WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::EXECUTION_CONTEXT;

static const whiteboard::LocalResourceId sProviderResources[] = {
    WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::LID,
};


HandwavingService::HandwavingService()
    : ResourceClient(WBDEBUG_NAME(__FUNCTION__), sExecutionContextId),
      ResourceProvider(WBDEBUG_NAME(__FUNCTION__), sExecutionContextId),
      LaunchableModule(LAUNCHABLE_NAME, sExecutionContextId),
      isRunning(false)
{

    mTimer = whiteboard::ID_INVALID_TIMER;
    // Reset max acceleration members
    mMaxAccelerationSq = FLT_MIN;
    mSamplesIncluded = 0;

}

HandwavingService::~HandwavingService()
{
}

bool HandwavingService::initModule()
{
    if (registerProviderResources(sProviderResources) != whiteboard::HTTP_CODE_OK)
    {
        return false;
    }

    mModuleState = WB_RES::ModuleStateValues::INITIALIZED;
    return true;
}

void HandwavingService::deinitModule()
{
    unregisterProviderResources(sProviderResources);
    mModuleState = WB_RES::ModuleStateValues::UNINITIALIZED;
}

/** @see whiteboard::ILaunchableModule::startModule */
bool HandwavingService::startModule()
{
    shutdownCounter = 0;
    mModuleState = WB_RES::ModuleStateValues::STARTED;
    mTimer = whiteboard::ResourceProvider::startTimer((size_t) LED_BLINKING_PERIOD, true);
    whiteboard::RequestId remoteRequestId;
    whiteboard::Result result = startRunning(remoteRequestId);
    return true;
}

void HandwavingService::stopModule() 
{
    whiteboard::ResourceProvider::stopTimer(mTimer);
    mModuleState = WB_RES::ModuleStateValues::STOPPED;
    mTimer = whiteboard::ID_INVALID_TIMER;
    stopRunning();
}


void HandwavingService::onGetRequest(const whiteboard::Request& request,
                                      const whiteboard::ParameterList& parameters)
{
    DEBUGLOG("HandwavingService::onGetRequest() called.");

    if (mModuleState != WB_RES::ModuleStateValues::STARTED)
    {
        return returnResult(request, wb::HTTP_CODE_SERVICE_UNAVAILABLE);
    }

    switch (request.getResourceConstId())
    {
    case WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::ID:
    {

        shutdownCounter = 0;
        return returnResult(request, whiteboard::HTTP_CODE_OK,
                            ResponseOptions::Empty, mMaxAccelerationSq);
    }

    break;

    default:
        // Return error
        return returnResult(request, whiteboard::HTTP_CODE_NOT_IMPLEMENTED);
    }
}

void HandwavingService::onUnsubscribeResult(whiteboard::RequestId requestId,
                                                     whiteboard::ResourceId resourceId,
                                                     whiteboard::Result resultCode,
                                                     const whiteboard::Value& rResultData)
{
    DEBUGLOG("HandwavingService::onUnsubscribeResult() called.");
}

void HandwavingService::onSubscribeResult(whiteboard::RequestId requestId,
                                                   whiteboard::ResourceId resourceId,
                                                   whiteboard::Result resultCode,
                                                   const whiteboard::Value& rResultData)
{
    DEBUGLOG("HandwavingService::onSubscribeResult() called. resourceId: %u, result: %d", resourceId.localResourceId, (uint32_t)resultCode);

    whiteboard::Request relatedIncomingRequest;
    bool relatedRequestFound = mOngoingRequests.get(requestId, relatedIncomingRequest);

    if (relatedRequestFound)
    {
        returnResult(relatedIncomingRequest, wb::HTTP_CODE_OK);
    }
}


whiteboard::Result HandwavingService::startRunning(whiteboard::RequestId& remoteRequestId)
{
    if (isRunning)
    {
        return whiteboard::HTTP_CODE_OK;
    }

    DEBUGLOG("HandwavingService::startRunning()");

    // Reset max acceleration members
    mMaxAccelerationSq = 0.0;
    mSamplesIncluded = 0;

    // Subscribe to LinearAcceleration resource (updates at 13Hz), when subscribe is done, we get callback
    wb::Result result = asyncSubscribe(WB_RES::LOCAL::MEAS_ACC_SAMPLERATE::ID, AsyncRequestOptions(&remoteRequestId, 0, true), SAMPLE_RATE);
    if (!wb::RETURN_OKC(result))
    {
        DEBUGLOG("asyncSubscribe threw error: %u", result);
        return whiteboard::HTTP_CODE_BAD_REQUEST;
    }
    isRunning = true;

    return whiteboard::HTTP_CODE_OK;
}


whiteboard::Result HandwavingService::stopRunning()
{
    if (!isRunning)
    {
        return whiteboard::HTTP_CODE_OK;
    }

    DEBUGLOG("HandwavingService::stopRunning()");

    // Unsubscribe the LinearAcceleration resource, when unsubscribe is done, we get callback
    wb::Result result = asyncUnsubscribe(WB_RES::LOCAL::MEAS_ACC_SAMPLERATE::ID, NULL, SAMPLE_RATE);
    if (!wb::RETURN_OKC(result))
    {
        DEBUGLOG("asyncUnsubscribe threw error: %u", result);
    }
    isRunning = false;
    return whiteboard::HTTP_CODE_OK;
}


// This callback is called when the resource we have subscribed notifies us
void HandwavingService::onNotify(whiteboard::ResourceId resourceId, const whiteboard::Value& value,
                                          const whiteboard::ParameterList& parameters)
{

    // Confirm that it is the correct resource
    switch (resourceId.getConstId())
    {
    case WB_RES::LOCAL::MEAS_ACC_SAMPLERATE::ID:
    {
        const WB_RES::AccData& linearAccelerationValue =
            value.convertTo<const WB_RES::AccData&>();

        if (linearAccelerationValue.arrayAcc.size() <= 0)
        {
            // No value, do nothing...
            return;
        }

        const whiteboard::Array<whiteboard::FloatVector3D>& arrayData = linearAccelerationValue.arrayAcc;
        
        uint32_t relativeTime = linearAccelerationValue.timestamp;

        for (size_t i = 0; i < arrayData.size(); i++)
        {
            whiteboard::FloatVector3D accValue = arrayData[i];
            DEBUGLOG("X, y ,z acc:", accValue.mX, accValue.mY, accValue.mZ);
            float accelerationSq = (accValue.mX * accValue.mX +
                                   accValue.mY * accValue.mY +
                                   accValue.mZ * accValue.mZ) - (9.81*9.81);

            if (mMaxAccelerationSq < accelerationSq)
                mMaxAccelerationSq = accelerationSq;
        }
    }
    break;
    }
}

void HandwavingService::onTimer(whiteboard::TimerId timerId)
{
    if (timerId != mTimer)
    {
        return;
    }

    shutdownCounter = shutdownCounter + LED_BLINKING_PERIOD;
    if (shutdownCounter >= AVAILABILITY_TIME) 
    {
            // Prepare AFE to wake-up mode
        asyncPut(WB_RES::LOCAL::COMPONENT_MAX3000X_WAKEUP::ID,
                 AsyncRequestOptions(NULL, 0, true), (uint8_t)1);


        // Make PUT request to enter power off mode
        asyncPut(WB_RES::LOCAL::SYSTEM_MODE::ID,
                 AsyncRequestOptions(NULL, 0, true), // Force async
                 (uint8_t)1U);                       // WB_RES::SystemMode::FULLPOWEROFF
    }
    else
    {
    // Make PUT request to trigger led blink
    asyncPut(WB_RES::LOCAL::UI_IND_VISUAL::ID, AsyncRequestOptions::Empty,(uint16_t) 2);
    }


}

void HandwavingService::onRemoteWhiteboardDisconnected(whiteboard::WhiteboardId whiteboardId)
{
    DEBUGLOG("HandwavingService::onRemoteWhiteboardDisconnected()");
    stopRunning();
}

void HandwavingService::onClientUnavailable(whiteboard::ClientId clientId)
{
    DEBUGLOG("HandwavingService::onClientUnavailable()");
    stopRunning();
}