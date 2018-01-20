package com.movesense.mds.sampleapp.example_app_using_mds_api.movesense;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.movesense.mds.sampleapp.RxBle;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class MovesensePresenter implements MovesenseContract.Presenter {

    private RxBleClient rxBleClient;
    private String TAG = MovesensePresenter.class.getSimpleName();

    private MovesenseContract.View mView;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<RxBleDevice> mMovesenseModelArrayList;
    private CompositeSubscription mCompositeSubscription;

    public MovesensePresenter(MovesenseContract.View view, BluetoothManager bluetoothManager) {
        mView = view;
        mView.setPresenter(this);

        mBluetoothManager = bluetoothManager;
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mMovesenseModelArrayList = new ArrayList<>();

        mCompositeSubscription = new CompositeSubscription();

        rxBleClient = RxBle.Instance.getClient();
    }

    @Override
    public void startScanning() {
        Log.d(TAG, "startScanning()");

        if (!mView.checkLocationPermissionIsGranted()) {
            mView.displayErrorMessage("Location Permission is required");
            return;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "startScanning: BT not available. Turning ON...");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable so run
                mBluetoothAdapter.enable();
            }
        } else {
            Log.d(TAG, "startScanning() startLeScan");

            mCompositeSubscription.add(rxBleClient.scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<RxBleScanResult>() {
                        @Override
                        public void call(RxBleScanResult rxBleScanResult) {
                            Log.d(TAG, "call: Scan result() " + rxBleScanResult.getBleDevice().getName());
                            RxBleDevice rxBleDevice = rxBleScanResult.getBleDevice();

                            if (rxBleDevice.getName() != null && rxBleDevice.getName().contains("Movesense")
                                    && !mMovesenseModelArrayList.contains(rxBleDevice)) {

                                Log.d(TAG, "call: Add to list " + rxBleScanResult.getBleDevice().getName());
                                mMovesenseModelArrayList.add(rxBleDevice);
                                mView.displayScanResult(rxBleDevice, rxBleScanResult.getRssi());
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "call: " + throwable);
                        }
                    }));
        }
    }

    @Override
    public void stopScanning() {
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onBluetoothResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onBluetoothResult: requestCode: " + requestCode + " resultCode: " + resultCode);
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    // The user bluetooth is ready to use.

                    // start scanning again in case of ready Bluetooth
                    startScanning();
                    return;
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    return;
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    return;
                }

            }
        }
    };

    @Override
    public void onCreate() {
        mView.registerReceiver(btReceiver);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
        stopScanning();
    }

    @Override
    public void onDestroy() {
        mView.unregisterReceiver(btReceiver);
        mCompositeSubscription.unsubscribe();
    }
}
