package com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.connection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.ScannerFragment;
import com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.sensor_usage.MultiSensorUsageActivity;
import com.movesense.mds.sampleapp.model.MdsConnectedDevice;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoNewSw;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoOldSw;
import com.polidea.rxandroidble.RxBleDevice;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class MultiConnectionActivity extends AppCompatActivity implements ScannerFragment.DeviceSelectionListener {

    @BindView(R.id.add_movesense1Ll) LinearLayout mAddMovesense1Ll;
    @BindView(R.id.add_movesense2Ll) LinearLayout mAddMovesense2Ll;
    @BindView(R.id.multiConnection_addDevice_Tv_1) TextView mMultiConnectionAddDeviceTv1;
    @BindView(R.id.multiConnection_selectedDeviceName_Tv_1) TextView mMultiConnectionSelectedDeviceNameTv1;
    @BindView(R.id.multiConnection_selectedDeviceSerial_Tv_1) TextView mMultiConnectionSelectedDeviceSerialTv1;
    @BindView(R.id.multiConnection_selectedDeviceInfo_Ll_1) LinearLayout mMultiConnectionSelectedDeviceInfoLl1;
    @BindView(R.id.multiConnection_addDevice_Tv_2) TextView mMultiConnectionAddDeviceTv2;
    @BindView(R.id.multiConnection_selectedDeviceName_Tv_2) TextView mMultiConnectionSelectedDeviceNameTv2;
    @BindView(R.id.multiConnection_selectedDeviceSerial_Tv_2) TextView mMultiConnectionSelectedDeviceSerialTv2;
    @BindView(R.id.multiConnection_selectedDeviceInfo_Ll_2) LinearLayout mMultiConnectionSelectedDeviceInfoLl2;
    @BindView(R.id.multiConnection_connect_Tv) TextView mMultiConnectionConnectTv;
    @BindView(R.id.multiConnection_status_tv) TextView mMultiConnectionStatusTv;

    private final String TAG = MultiConnectionActivity.class.getSimpleName();

    private ScannerFragment scannerFragment;
    private boolean isAddDevice1Pressed = false;
    private RxBleDevice mRxBleDevice1;
    private RxBleDevice mRxBleDevice2;
    private CompositeSubscription mCompositeSubscription;
    private boolean isFirstDeviceConnected = false;
    private boolean isSecondDeviceConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connection);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Multi Connection");
        }

        mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        if (mdsConnectedDevice.getConnection() != null) {

                            if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                                MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();

                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress(),
                                        mdsDeviceInfoNewSw.getDescription(),
                                        mdsDeviceInfoNewSw.getSerial(),
                                        mdsDeviceInfoNewSw.getSw()));

                                if (mRxBleDevice1.getName().equals(mdsDeviceInfoNewSw.getDescription())) {
                                    Log.e(TAG, "call: First device connected");
                                    isFirstDeviceConnected = true;
                                } else if (mRxBleDevice2.getName().equals(mdsDeviceInfoNewSw.getDescription())) {
                                    Log.e(TAG, "call: Second device connected");
                                    isSecondDeviceConnected = true;
                                }

                            } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                                MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();

                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoOldSw.getAddressInfoOld(),
                                        mdsDeviceInfoOldSw.getDescription(),
                                        mdsDeviceInfoOldSw.getSerial(),
                                        mdsDeviceInfoOldSw.getSw()));

                                if (mRxBleDevice1.getName().equals(mdsDeviceInfoOldSw.getDescription())) {
                                    Log.e(TAG, "call: First device connected");
                                    isFirstDeviceConnected = true;
                                } else if (mRxBleDevice2.getName().equals(mdsDeviceInfoOldSw.getDescription())) {
                                    Log.e(TAG, "call: Second device connected");
                                    isSecondDeviceConnected = true;
                                }
                            }

                            if (isFirstDeviceConnected && isSecondDeviceConnected) {
                                startActivity(new Intent(MultiConnectionActivity.this, MultiSensorUsageActivity.class));
                            }
                        }
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }

    @OnClick({R.id.add_movesense1Ll, R.id.add_movesense2Ll, R.id.multiConnection_connect_Tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_movesense1Ll:
                scannerFragment = new ScannerFragment();
                scannerFragment.show(getSupportFragmentManager(), ScannerFragment.class.getName());
                isAddDevice1Pressed = true;
                break;
            case R.id.add_movesense2Ll:
                scannerFragment = new ScannerFragment();
                scannerFragment.show(getSupportFragmentManager(), ScannerFragment.class.getName());
                isAddDevice1Pressed = false;
                break;
            case R.id.multiConnection_connect_Tv:

                if (mRxBleDevice1 != null && mRxBleDevice2 != null) {

                    if (mRxBleDevice1.getName().equals(mRxBleDevice2.getName())) {
                        Toast.makeText(MultiConnectionActivity.this, "You can't connect to the same device twice", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mMultiConnectionStatusTv.setText("Connecting...");
                    blockUI();

                    Mds.builder().build(MultiConnectionActivity.this).connect(mRxBleDevice1.getMacAddress(), null);
                    Mds.builder().build(MultiConnectionActivity.this).connect(mRxBleDevice2.getMacAddress(), null);
                } else {
                    Toast.makeText(this, "Add device for connection", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onDeviceSelected(RxBleDevice device) {
        Log.d(TAG, "onDeviceSelected: " + device.getName() + " " + device.getMacAddress() + " " + isAddDevice1Pressed);

        scannerFragment.dismiss();

        if (isAddDevice1Pressed) {
            mRxBleDevice1 = device;
            mMultiConnectionAddDeviceTv1.setVisibility(View.GONE);
            mMultiConnectionSelectedDeviceInfoLl1.setVisibility(View.VISIBLE);
            mMultiConnectionSelectedDeviceNameTv1.setText(device.getName());
            mMultiConnectionSelectedDeviceSerialTv1.setText(device.getMacAddress());
        } else {
            mRxBleDevice2 = device;
            mMultiConnectionAddDeviceTv2.setVisibility(View.GONE);
            mMultiConnectionSelectedDeviceInfoLl2.setVisibility(View.VISIBLE);
            mMultiConnectionSelectedDeviceNameTv2.setText(device.getName());
            mMultiConnectionSelectedDeviceSerialTv2.setText(device.getMacAddress());
        }
    }

    private void blockUI() {
        mAddMovesense1Ll.setEnabled(false);

        mAddMovesense2Ll.setEnabled(false);

        mMultiConnectionConnectTv.setEnabled(false);
    }

    private void clearUI() {
        mAddMovesense1Ll.setEnabled(true);
        mAddMovesense1Ll.setBackground(ContextCompat.getDrawable(this, R.drawable.black_stroke));

        mAddMovesense2Ll.setEnabled(true);
        mAddMovesense2Ll.setBackground(ContextCompat.getDrawable(this, R.drawable.black_stroke));

        mMultiConnectionConnectTv.setEnabled(true);
        mMultiConnectionConnectTv.setBackground(ContextCompat.getDrawable(this, R.drawable.black_stroke));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
