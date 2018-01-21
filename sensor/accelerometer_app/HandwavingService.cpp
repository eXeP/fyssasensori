#include "HandwavingService.h"
#include "app-resources/resources.h"
#include "common/core/debug.h"
#include "meas_acc/resources.h"
#include "whiteboard/builtinTypes/UnknownStructure.h"
#include "whiteboard/integration/bsp/shared/debug.h"

#include <float.h>
#include <math.h>

#define ASSERT WB_DEBUG_ASSERT

const char* const HandwavingService::LAUNCHABLE_NAME = "SampleA";
#define SAMPLE_RATE 13
static const whiteboard::ExecutionContextId sExecutionContextId =
    WB_RES::LOCAL::FYSSA_HANDWAVING::EXECUTION_CONTEXT;

static const whiteboard::LocalResourceId sProviderResources[] = {
    WB_RES::LOCAL::FYSSA_HANDWAVING::LID,
};

HandwavingService::HandwavingService()
    : ResourceClient(WBDEBUG_NAME(__FUNCTION__), sExecutionContextId),
      ResourceProvider(WBDEBUG_NAME(__FUNCTION__), sExecutionContextId),
      LaunchableModule(LAUNCHABLE_NAME, sExecutionContextId),
      isRunning(false)
{
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
    mModuleState = WB_RES::ModuleStateValues::STARTED;

    return true;
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
    case WB_RES::LOCAL::FYSSA_HANDWAVING::ID:
    {
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
    mMaxAccelerationSq = FLT_MIN;
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
    DEBUGLOG("onNotify() called.");

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
            float accelerationSq = accValue.mX * accValue.mX +
                                   accValue.mY * accValue.mY +
                                   accValue.mZ * accValue.mZ;

            if (mMaxAccelerationSq < accelerationSq)
                mMaxAccelerationSq = accelerationSq;
        }
    }
    break;
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