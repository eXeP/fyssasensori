package com.movesense.mds.sampleapp.example_app_using_mds_api.movesense;


import android.content.BroadcastReceiver;
import android.content.Intent;

import com.movesense.mds.sampleapp.example_app_using_mds_api.BasePresenter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.BaseView;
import com.polidea.rxandroidble.RxBleDevice;

public interface MovesenseContract {

    interface Presenter extends BasePresenter {
        void startScanning();

        void stopScanning();

        void onBluetoothResult(int requestCode, int resultCode, Intent data);
    }

    interface View extends BaseView<Presenter> {
        void displayScanResult(RxBleDevice bluetoothDevice, int rssi);

        void displayErrorMessage(String message);

        void registerReceiver(BroadcastReceiver broadcastReceiver);

        void unregisterReceiver(BroadcastReceiver broadcastReceiver);

        boolean checkLocationPermissionIsGranted();
    }
}
