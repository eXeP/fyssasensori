package com.movesense.mds.sampleapp.example_app_using_mds_api.mainView;


import android.support.test.espresso.IdlingResource;

public class DfuTimeIdlingResource implements IdlingResource {

    private final long startTime;
    private final long waitingTime;
    private ResourceCallback mResourceCallback;

    public DfuTimeIdlingResource(long waitingTime) {
        this.waitingTime = waitingTime;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return DfuTimeIdlingResource.class.getName() + ":" + waitingTime;
    }

    @Override
    public boolean isIdleNow() {
        long elapsed = System.currentTimeMillis() - startTime;
        boolean idle = (elapsed >= waitingTime);
        if (idle) {
            mResourceCallback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.mResourceCallback = callback;
    }
}
