package com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.sensor_usage;


import com.movesense.mds.sampleapp.MdsRx;

import rx.Observable;

public class MultiSensorUsagePresenter implements MultiSensorUsageContract.Presenter {

    private final MultiSensorUsageContract.View mView;

    public MultiSensorUsagePresenter(MultiSensorUsageContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public Observable<String> subscribeLinearAcc(String uri) {
        return MdsRx.Instance.subscribe(uri);

    }
}
