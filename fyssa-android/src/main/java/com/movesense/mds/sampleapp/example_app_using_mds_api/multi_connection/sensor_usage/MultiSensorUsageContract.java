package com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.sensor_usage;


import com.movesense.mds.sampleapp.example_app_using_mds_api.BasePresenter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.BaseView;

import rx.Observable;

public interface MultiSensorUsageContract {

    interface Presenter extends BasePresenter {
        Observable<String> subscribeLinearAcc(String uri);

    }

    interface View extends BaseView<MultiSensorUsageContract.Presenter> {

    }
}
