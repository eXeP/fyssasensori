package com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.connection;

import com.movesense.mds.sampleapp.example_app_using_mds_api.BasePresenter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.BaseView;
import com.polidea.rxandroidble.RxBleDevice;

public interface MultiConnectionContract {

    interface Presenter extends BasePresenter {
        void scanFirstDevice();
        void scanSecondDevice();
        void connect(RxBleDevice rxBleDevice);
        void disconnect(RxBleDevice rxBleDevice);

    }

    interface View extends BaseView<MultiConnectionContract.Presenter> {
        void onFirsDeviceSelectedResult(RxBleDevice rxBleDevice);
        void onSecondDeviceSelectedResult(RxBleDevice rxBleDevice);
        void displayErrorMessage(String message);
    }
}
