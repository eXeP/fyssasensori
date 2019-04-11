package com.movesense.mds.handwave.scanner;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.movesense.mds.handwave.bluetooth.BleManager;
import com.movesense.mds.handwave.bluetooth.MdsRx;
import com.movesense.mds.handwave.R;
import com.movesense.mds.fyssabailu.ThrowableToastingAction;
import com.movesense.mds.handwave.fyssa_app.FyssaApp;
import com.movesense.mds.handwave.model.MdsDeviceInfoNewSw;
import com.movesense.mds.handwave.model.MdsDeviceInfoOldSw;
import com.movesense.mds.handwave.update_app.ConnectingDialog;
import com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices;

import com.polidea.rxandroidble.RxBleDevice;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.movesense.mds.handwave.fyssa_app.FyssaMainActivity.removeAndDisconnectFromDevices;

public abstract class ScanActivity  extends AppCompatActivity implements com.movesense.mds.handwave.scanner.ScanFragment.DeviceSelectionListener {
    private final String TAG = ScanActivity.class.getSimpleName();

    private CompositeSubscription subscriptions;
    private FyssaApp app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (FyssaApp) getApplication();

        // Already connected!!
        if (MovesenseConnectedDevices.getConnectedDevices().size()>=1) {
            BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = true;
            continueToActivity();
            return;
        }
        subscriptions = new CompositeSubscription();
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Log.d(TAG, "getting scanFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, new com.movesense.mds.handwave.scanner.ScanFragment(), com.movesense.mds.handwave.scanner.ScanFragment.class.getSimpleName())
                    .commit();
        }

    }

    protected abstract void continueToActivity();

    @Override
    public void onDeviceSelected(final RxBleDevice device) {
        Log.d(TAG, "onDeviceSelected: " + device.getName() + " (" + device.getMacAddress() + ")");
        MdsRx.Instance.connect(device, this);
        BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = true;
        ConnectingDialog.INSTANCE.showDialog(this);

        // Monitor for connected devices
        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mdsConnectedDevice -> {
                    // Stop refreshing
                    if (mdsConnectedDevice.getConnection() != null) {
                        Log.d(TAG, "onDeviceSelected found a connected device");
                        ConnectingDialog.INSTANCE.dismissDialog();
                        // Add connected device
                        // Fixme: this should be deleted after 1.0 SW release

                        if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                            MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();
                            MovesenseConnectedDevices.addConnectedDevice(new com.movesense.mds.fyssabailu.update_app.model.MovesenseDevice(
                                    device.getMacAddress(),
                                    mdsDeviceInfoNewSw.getDescription(),
                                    mdsDeviceInfoNewSw.getSerial(),
                                    mdsDeviceInfoNewSw.getSw()));
                        } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                            MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();
                            MovesenseConnectedDevices.addConnectedDevice(new com.movesense.mds.fyssabailu.update_app.model.MovesenseDevice(
                                    device.getMacAddress(),
                                    mdsDeviceInfoOldSw.getDescription(),
                                    mdsDeviceInfoOldSw.getSerial(),
                                    mdsDeviceInfoOldSw.getSw()));
                        }
                        // We have a new SdsDevice
                        subscriptions.unsubscribe();
                        subscriptions.clear();
                        continueToActivity();
                    }
                }, new ThrowableToastingAction(this)));
    }
    @Override
    public void onBackPressed() {
        subscriptions.clear();
        ConnectingDialog.INSTANCE.dismissDialog();
        removeAndDisconnectFromDevices();
        startActivity(new Intent(ScanActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onDestroy() {
        ConnectingDialog.INSTANCE.dismissDialog();
        super.onDestroy();
    }

}
