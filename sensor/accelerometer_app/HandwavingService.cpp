#include "HandwavingService.h"
#include "app-resources/resources.h"
#include "common/core/debug.h"
#include "meas_acc/resources.h"
#include "whiteboard/builtinTypes/UnknownStructure.h"
#include "component_max3000x/resources.h"
#include "system_mode/resources.h"
#include "ui_ind/resources.h"
#include <float.h>
#include <math.h>
#include "SLinkedList.h"

#define ASSERT WB_DEBUG_ASSERT

// Time between wake-up and going to power-off mode
#define AVAILABILITY_TIME 180000

// Time between turn on AFE wake circuit to power off
// (must be LED_BLINKING_PERIOD multiple)
#define WAKE_PREPARATION_TIME 18000

// LED blinking period in adertsing mode
#define LED_BLINKING_PERIOD 18000

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
      isRunning(false),
      dataSubscription(false),
      runningTime(1),
      previousAcc(sll::SLinkedList<float>(ACCELERATION_AVERAGING_SIZE, 0.0))
{

    mTimer = whiteboard::ID_INVALID_TIMER;
    // Reset max acceleration members
    reset();

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
    whiteboard::Result result = startRunning(mRemoteRequestId);
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
    DEBUGLOG("D/SENSOR/HandwavingService::onGetRequest() called.");

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

void HandwavingService::onPutRequest(const whiteboard::Request& request,
                                      const whiteboard::ParameterList& parameters)
{
 DEBUGLOG("D/SENSOR/HandwavingService::onPutRequest() called.");

    if (mModuleState != WB_RES::ModuleStateValues::STARTED)
    {
        return returnResult(request, wb::HTTP_CODE_SERVICE_UNAVAILABLE);
    }

    switch (request.getResourceConstId())
    {
    case WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::ID:
    {
        runningTime  = 60000*WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::PUT::ParameterListRef(parameters).getHandwaveConfig().time;
        startRunning(mRemoteRequestId);
        keepRunning = true;
        shutdownCounter = 0;
        return returnResult(request, whiteboard::HTTP_CODE_OK);
    }
    default:
        // Return error
        return returnResult(request, whiteboard::HTTP_CODE_NOT_IMPLEMENTED);
    }
}
void HandwavingService::onSubscribe(const whiteboard::Request& request,
                                     const whiteboard::ParameterList& parameters)
{
    DEBUGLOG("D/SENSOR/HandwavingService::onSubscribe()");
    
    switch (request.getResourceConstId())
    {
    case WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::ID:
    {
        DEBUGLOG("D/SENSOR/Subscription for handwaves");
        /*
        bool queueResult = mOngoingRequests.put(mRemoteRequestId, request);
        if (!queueResult) DEBUGLOG("D/SENSOR/Request not put into the map!?");

        //WB_ASSERT(queueResult);
*/
        dataSubscription = true;
        return returnResult(request, whiteboard::HTTP_CODE_OK);
       
        break;
    }
    default:
        DEBUGLOG("D/SENSOR/Shouldn't happen!");
        return returnResult(request, whiteboard::HTTP_CODE_BAD_REQUEST);
        //return ResourceProvider::onSubscribe(request, parameters);
        break;
    }
}


void HandwavingService::onUnsubscribe(const whiteboard::Request& request,
                                       const whiteboard::ParameterList& parameters)
{
    DEBUGLOG("D/SENSOR/HandwavingService::onUnsubscribe()");

    switch (request.getResourceConstId())
    {
    case WB_RES::LOCAL::FYSSA_HANDWAVING_DATA::ID:
        dataSubscription = false;
        returnResult(request, wb::HTTP_CODE_OK);
        break;

    default:
        DEBUGLOG("D/SENSOR/Shouldnt happen!");
        return returnResult(request, whiteboard::HTTP_CODE_BAD_REQUEST);
        //ResourceProvider::onUnsubscribe(request, parameters);
        break;
    }
}


void HandwavingService::onUnsubscribeResult(whiteboard::RequestId requestId,
                                                     whiteboard::ResourceId resourceId,
                                                     whiteboard::Result resultCode,
                                                     const whiteboard::Value& rResultData)
{
    DEBUGLOG("D/SENSOR/HandwavingService::onUnsubscribeResult() called.");
}

void HandwavingService::onSubscribeResult(whiteboard::RequestId requestId,
                                                   whiteboard::ResourceId resourceId,
                                                   whiteboard::Result resultCode,
                                                   const whiteboard::Value& rResultData)
{
    DEBUGLOG("D/SENSOR/HandwavingService::onSubscribeResult() called. resourceId: %u, result: %d", resourceId.localResourceId, (uint32_t)resultCode);

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

    DEBUGLOG("D/SENSOR/HandwavingService::startRunning()");

    // Reset max acceleration members
    reset();



    // Subscribe to LinearAcceleration resource (updates at 13Hz), when subscribe is done, we get callback
    wb::Result result = asyncSubscribe(WB_RES::LOCAL::MEAS_ACC_SAMPLERATE::ID, AsyncRequestOptions(&remoteRequestId, 0, true), SAMPLE_RATE);
    if (!wb::RETURN_OKC(result))
    {
        DEBUGLOG("D/SENSOR/asyncSubscribe threw error: %u", result);
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

    DEBUGLOG("D/SENSOR/HandwavingService::stopRunning()");

    // Unsubscribe the LinearAcceleration resource, when unsubscribe is done, we get callback
    wb::Result result = asyncUnsubscribe(WB_RES::LOCAL::MEAS_ACC_SAMPLERATE::ID, NULL, SAMPLE_RATE);
    if (!wb::RETURN_OKC(result))
    {
        DEBUGLOG("D/SENSOR/asyncUnsubscribe threw error: %u", result);
    }
    isRunning = false;
    keepRunning = false;
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
        whiteboard::FloatVector3D accValue = arrayData[0];
        for (size_t i = 1; i < arrayData.size(); i++)
        {
          accValue += arrayData[i];
        }
        accValue /= arrayData.size();
 
        float accelerationSq = (accValue.mX * accValue.mX +
                               accValue.mY * accValue.mY +
                               accValue.mZ * accValue.mZ) - (100);
            
        previousAcc.push(accelerationSq);
        float hereNow = previousAcc.average;
        if (mMaxAccelerationSq < hereNow)
        {
            DEBUGLOG("D/SENSOR/New value!");
            mMaxAccelerationSq = hereNow;
            if (dataSubscription)     {
                DEBUGLOG("D/SENSORNotifying subscribers");
                updateResource(WB_RES::LOCAL::FYSSA_HANDWAVING_DATA(),
                    ResponseOptions::Empty, mMaxAccelerationSq);
            }
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
    if (!dataSubscription || keepRunning) shutdownCounter = shutdownCounter + LED_BLINKING_PERIOD;
    else shutdownCounter = 0;
    if (shutdownCounter >= AVAILABILITY_TIME && !keepRunning) 
    {
        stopRunning();    
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
        if (shutdownCounter >= runningTime) keepRunning = false;
    // Make PUT request to trigger led blink
        asyncPut(WB_RES::LOCAL::UI_IND_VISUAL::ID, AsyncRequestOptions::Empty,(uint16_t) 2);
    }


}

void HandwavingService::reset()
{
    mMaxAccelerationSq = 0;
    previousAcc.fill(0.0);
    shutdownCounter = 0;
    
}

void HandwavingService::onRemoteWhiteboardDisconnected(whiteboard::WhiteboardId whiteboardId)
{
    DEBUGLOG("D/SENSOR/HandwavingService::onRemoteWhiteboardDisconnected()");
    if (!keepRunning) stopRunning();
}

void HandwavingService::onClientUnavailable(whiteboard::ClientId clientId)
{
    DEBUGLOG("D/SENSOR/HandwavingService::onClientUnavailable()");
    if (!keepRunning) stopRunning();
}
