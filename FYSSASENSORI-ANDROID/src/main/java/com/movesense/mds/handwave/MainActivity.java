package com.movesense.mds.handwave;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.movesense.mds.handwave.app_using_mds_api.ConnectingDialog;
import com.movesense.mds.handwave.app_using_mds_api.SelectTestActivity;
import com.movesense.mds.handwave.app_using_mds_api.model.MovesenseConnectedDevices;
import com.movesense.mds.handwave.app_using_mds_api.model.MovesenseDevice;
import com.movesense.mds.handwave.model.MdsConnectedDevice;
import com.movesense.mds.handwave.model.MdsDeviceInfoNewSw;
import com.movesense.mds.handwave.model.MdsDeviceInfoOldSw;
import com.polidea.rxandroidble.RxBleDevice;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements ScanFragment.DeviceSelectionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private CompositeSubscription subscriptions;
    private AlertDialog alertDialog;
    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String version = BuildConfig.VERSION_NAME;
        getSupportActionBar().setTitle("FyssaHeilutus "+ version);


        setContentView(R.layout.activity_main);

        subscriptions = new CompositeSubscription();
        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.close_app)
                .setMessage(R.string.do_you_want_to_close_application)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            finishAndRemoveTask();
                        } else {
                            finish();
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                })
                .create();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, new ScanFragment(), ScanFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onDeviceSelected(final RxBleDevice device) {
        Log.d(TAG, "onDeviceSelected: " + device.getName() + " (" + device.getMacAddress() + ")");
        MdsRx.Instance.connect(device, this);

        ConnectingDialog.INSTANCE.showDialog(this);

        // Monitor for connected devices
        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        // Stop refreshing
                        if (mdsConnectedDevice.getConnection() != null) {
                            ConnectingDialog.INSTANCE.dismissDialog();
                            // Add connected device
                            // Fixme: this should be deleted after 1.0 SW release

                            if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                                MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        device.getMacAddress(),
                                        mdsDeviceInfoNewSw.getDescription(),
                                        mdsDeviceInfoNewSw.getSerial(),
                                        mdsDeviceInfoNewSw.getSw()));
                            } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                                MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        device.getMacAddress(),
                                        mdsDeviceInfoOldSw.getDescription(),
                                        mdsDeviceInfoOldSw.getSerial(),
                                        mdsDeviceInfoOldSw.getSw()));
                            }
                            // We have a new SdsDevice
                            startActivity(new Intent(MainActivity.this, SelectTestActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }
                }, new ThrowableToastingAction(this)));
    }
    @Override
    public void onBackPressed() {
        alertDialog.show();
    }
}
