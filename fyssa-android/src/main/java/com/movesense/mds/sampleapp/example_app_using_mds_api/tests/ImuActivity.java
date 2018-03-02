package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
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
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.ImuModel;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class ImuActivity extends AppCompatActivity implements BleManager.IBleConnectionMonitor {

    private final String TAG = ImuActivity.class.getSimpleName();

    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final String IMU6_PATH = "Meas/IMU6/";
    private final String IMU9_PATH = "Meas/IMU9/";
    private final String HZ_13 = "13";
    @BindView(R.id.imu6_radioBtn) RadioButton mImu6RadioBtn;
    @BindView(R.id.imu9_radioBtn) RadioButton mImu9RadioBtn;


    private String SELECTED_PATH = IMU6_PATH;

    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    @BindView(R.id.switchSubscription) SwitchCompat mSwitchSubscription;
    @BindView(R.id.linearacc_x_axis_textView) TextView mLinearaccXAxisTextView;
    @BindView(R.id.linearacc_y_axis_textView) TextView mLinearaccYAxisTextView;
    @BindView(R.id.linearacc_z_axis_textView) TextView mLinearaccZAxisTextView;
    @BindView(R.id.gyro_x_axis_textView) TextView mGyroXAxisTextView;
    @BindView(R.id.gyro_y_axis_textView) TextView mGyroYAxisTextView;
    @BindView(R.id.gyro_z_axis_textView) TextView mGyroZAxisTextView;
    @BindView(R.id.magn_x_axis_textView) TextView mMagnXAxisTextView;
    @BindView(R.id.magn_y_axis_textView) TextView mMagnYAxisTextView;
    @BindView(R.id.magn_z_axis_textView) TextView mMagnZAxisTextView;

    private MdsSubscription mMdsSubsription;
    private CsvLogger mCsvLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imu);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Imu");
        }

        mCsvLogger = new CsvLogger();

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);
    }

    @OnCheckedChanged(R.id.switchSubscription)
    public void onSwitchCheckedChange(final CompoundButton button, boolean checked) {
        if (checked) {

            mImu6RadioBtn.setEnabled(false);
            mImu9RadioBtn.setEnabled(false);

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            mMdsSubsription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), SELECTED_PATH + HZ_13), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(TAG, "onSuccess(): " + data);

                            ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);

                            mLinearaccXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayAcc()[0].getX()));
                            mLinearaccYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayAcc()[0].getY()));
                            mLinearaccZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayAcc()[0].getZ()));

                            mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                    "Acc,%.6f,%.6f,%.6f, ",
                                    imuModel.getBody().getArrayAcc()[0].getX(),
                                    imuModel.getBody().getArrayAcc()[0].getY(),
                                    imuModel.getBody().getArrayAcc()[0].getZ()));

                            mGyroXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayGyro()[0].getX()));
                            mGyroYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayGyro()[0].getY()));
                            mGyroZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayGyro()[0].getZ()));

                            mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                    "Gyro,%.6f,%.6f,%.6f, ",
                                    imuModel.getBody().getArrayGyro()[0].getX(),
                                    imuModel.getBody().getArrayGyro()[0].getY(),
                                    imuModel.getBody().getArrayGyro()[0].getZ()));

                            if (imuModel.getBody().getArrayMagnl() != null) {
                                mMagnXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayMagnl()[0].getX()));
                                mMagnYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayMagnl()[0].getY()));
                                mMagnZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayMagnl()[0].getZ()));

                                mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                        "Magn,%.6f,%.6f,%.6f, ",
                                        imuModel.getBody().getArrayMagnl()[0].getX(),
                                        imuModel.getBody().getArrayMagnl()[0].getY(),
                                        imuModel.getBody().getArrayMagnl()[0].getZ()));
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(TAG, "onError(): ", error);
                            button.setChecked(false);
                            mImu6RadioBtn.setEnabled(false);
                            mImu9RadioBtn.setEnabled(false);
                        }
                    });
        } else {
            unSubscribe();
            mImu6RadioBtn.setEnabled(true);
            mImu9RadioBtn.setEnabled(true);
        }
    }

    private void unSubscribe() {
        if (mMdsSubsription != null) {
            mMdsSubsription.unsubscribe();
            mMdsSubsription = null;
        }
        mCsvLogger.finishSavingLogs(TAG);
    }

    @OnCheckedChanged({R.id.imu6_radioBtn, R.id.imu9_radioBtn})
    public void onImuRadioGroupChange(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.imu6_radioBtn:
                    SELECTED_PATH = IMU6_PATH;
                    break;
                case R.id.imu9_radioBtn:
                    SELECTED_PATH = IMU9_PATH;
                    break;
            }
        }

        mLinearaccXAxisTextView.setText("x:");
        mLinearaccYAxisTextView.setText("y:");
        mLinearaccZAxisTextView.setText("z:");

        mGyroXAxisTextView.setText("x:");
        mGyroYAxisTextView.setText("y:");
        mGyroZAxisTextView.setText("z:");

        mMagnXAxisTextView.setText("x:");
        mMagnYAxisTextView.setText("y:");
        mMagnZAxisTextView.setText("z:");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(TAG, "onDisconnect: " + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConnectionLostDialog.INSTANCE.showDialog(ImuActivity.this);
            }
        });
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.d(TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }
}
