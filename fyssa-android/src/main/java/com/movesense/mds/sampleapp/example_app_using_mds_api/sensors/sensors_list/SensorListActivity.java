package com.movesense.mds.sampleapp.example_app_using_mds_api.sensors.sensors_list;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.ThrowableToastingAction;
import com.movesense.mds.sampleapp.example_app_using_mds_api.mainView.MainViewActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.AngularVelocityActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.AppInfoActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.BatteryActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.EcgActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.HeartRateTestActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.LedTestActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.LinearAccelerationTestActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.MagneticFieldTestActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.MultiSubscribeActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.tests.TemperatureTestActivity;
import com.movesense.mds.sampleapp.model.MdsConnectedDevice;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class SensorListActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.sensorList_recyclerView) RecyclerView mSensorListRecyclerView;
    @BindView(R.id.sensorList_deviceInfo_title_tv) TextView mSensorListDeviceInfoTitleTv;
    @BindView(R.id.sensorList_deviceInfo_serial_tv) TextView mSensorListDeviceInfoSerialTv;
    @BindView(R.id.sensorList_deviceInfo_sw_tv) TextView mSensorListDeviceInfoSwTv;

    private CompositeSubscription subscriptions;

    private final String TAG = SensorListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sensors List");
        }

        subscriptions = new CompositeSubscription();

        ArrayList<SensorListItemModel> sensorListItemModels = new ArrayList<>();

        sensorListItemModels.add(new SensorListItemModel(getString(R.string.app_info_name), R.drawable.linear_acc2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.linear_acceleration_name), R.drawable.linear_acc2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.led_name), R.drawable.led2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.temperature_name), R.drawable.temperature2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.heart_rate_name), R.drawable.heart_rate2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.angular_velocity_name), R.drawable.gyro2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.magnetic_field_name), R.drawable.magnetic_field2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.multi_subscription_name), R.drawable.magnetic_field2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.ecg), R.drawable.magnetic_field2));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.battery_energy), R.drawable.magnetic_field2));

        SensorsListAdapter sensorsListAdapter = new SensorsListAdapter(sensorListItemModels, this);
        mSensorListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSensorListRecyclerView.setAdapter(sensorsListAdapter);

        sensorsListAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorListDeviceInfoSerialTv.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0).getSerial());
        mSensorListDeviceInfoSwTv.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0).getSwVersion());

        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        if (mdsConnectedDevice.getConnection() == null) {
                            Log.d(TAG, "Disconnected");

                            if (MovesenseConnectedDevices.getConnectedDevices().size() == 1) {
                                MovesenseConnectedDevices.getConnectedDevices().remove(0);
                            } else {
                                Log.e(TAG, "ERROR: Wrong MovesenseConnectedDevices list size");
                            }

                            startActivity(new Intent(SensorListActivity.this, MainViewActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        }
                    }
                }, new ThrowableToastingAction(this)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscriptions.clear();
    }

    @Override
    public void onClick(View v) {
        String sensorName = (String) v.getTag();

        subscriptions.clear();

        if (getString(R.string.led_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, LedTestActivity.class));
            return;
        } else if (getString(R.string.linear_acceleration_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, LinearAccelerationTestActivity.class));
            return;
        } else if (getString(R.string.temperature_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, TemperatureTestActivity.class));
            return;
        } else if (getString(R.string.angular_velocity_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, AngularVelocityActivity.class));
            return;
        } else if (getString(R.string.magnetic_field_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, MagneticFieldTestActivity.class));
            return;
        } else if (getString(R.string.heart_rate_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, HeartRateTestActivity.class));
            return;
        } else if (getString(R.string.multi_subscription_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, MultiSubscribeActivity.class));
            return;
        } else if (getString(R.string.ecg).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, EcgActivity.class));
            return;
        } else if (getString(R.string.battery_energy).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, BatteryActivity.class));
            return;
        } else if (getString(R.string.app_info_name).equals(sensorName)) {
            startActivity(new Intent(SensorListActivity.this, AppInfoActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit)
                .setMessage(R.string.disconnect_dialog_text)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Disconnecting...");

                        BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(0));
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
