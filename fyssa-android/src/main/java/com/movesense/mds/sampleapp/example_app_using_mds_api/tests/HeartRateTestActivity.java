package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.ConnectionLostDialog;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.FormatHelper;
import com.movesense.mds.sampleapp.example_app_using_mds_api.csv.CsvLogger;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.HeartRate;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class HeartRateTestActivity extends AppCompatActivity implements BleManager.IBleConnectionMonitor {

    private final String LOG_TAG = HeartRateTestActivity.class.getSimpleName();
    private final String HEART_RATE_PATH = "Meas/Hr";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    private MdsSubscription mdsSubscription;
    private CsvLogger mCsvLogger;

    @BindView(R.id.heart_rate_switch) SwitchCompat heartRateSwitch;
    @BindView(R.id.heart_rate_value_textView) TextView heartRateValueTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_test);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Heart Rate");
        }

        mCsvLogger = new CsvLogger();

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());
    }

    @OnCheckedChanged(R.id.heart_rate_switch)
    public void onCheckedChange(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0).getSerial(),
                            HEART_RATE_PATH)
                    , new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(LOG_TAG, "Heart rate onNotification() : " + data);
                            HeartRate heartRate = new Gson().fromJson(data, HeartRate.class);

                            if (heartRate != null) {
                                heartRateValueTextView.setText(String.format(Locale.getDefault(),
                                        "Value: %d", heartRate.body.rrData[0]));

                                mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                        "%d", heartRate.body.rrData[0]));
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(LOG_TAG, "Heart rate error", error);
                        }
                    });

        } else {
            unSubscribe();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    private void unSubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }
        mCsvLogger.finishSavingLogs(LOG_TAG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        if (requestCode == LogsManager.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
//            // if request is cancelled grantResults array is empty
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//                }
//            }
//        }
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(LOG_TAG, "onDisconnect: " + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConnectionLostDialog.INSTANCE.showDialog(HeartRateTestActivity.this);
            }
        });
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(LOG_TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }
}
