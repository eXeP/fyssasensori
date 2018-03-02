package com.fyssasensori;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.RxBle;
import com.movesense.mds.sampleapp.ThrowableToastingAction;
import com.movesense.mds.sampleapp.example_app_using_mds_api.ConnectingDialog;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseAdapter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseContract;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesensePresenter;
import com.movesense.mds.sampleapp.model.MdsConnectedDevice;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoNewSw;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoOldSw;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pietu on 1/21/18.
 */

public class FyssaScanActivity extends AppCompatActivity implements MovesenseContract.View, View.OnClickListener {
    @BindView(R.id.movesense_recyclerView)
    RecyclerView mMovesenseRecyclerView;
    @BindView(R.id.movesense_infoTv)
    TextView mMovesenseInfoTv;
    @BindView(R.id.movesense_progressBar)
    ProgressBar mMovesenseProgressBar;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;

    private MovesenseContract.Presenter mMovesensePresenter;
    private ArrayList<RxBleDevice> mMovesenseModels;
    private CompositeSubscription scanningSubscriptions;
    private CompositeSubscription connectedDevicesSubscriptions;

    private final String TAG = FyssaScanActivity.class.getSimpleName();
    private MovesenseAdapter mMovesenseAdapter;

    private FyssaApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fyssa_scan);
        ButterKnife.bind(this);

        app = (FyssaApp) getApplication();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Yhdistä Movesense-laitteesi");
        }

        scanningSubscriptions = new CompositeSubscription();
        connectedDevicesSubscriptions = new CompositeSubscription();

        mMovesensePresenter = new MovesensePresenter(this,
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE));

        mMovesensePresenter.onCreate();

        mMovesenseModels = new ArrayList<>();

        mMovesenseAdapter = new MovesenseAdapter(mMovesenseModels, this);
        mMovesenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMovesenseRecyclerView.setAdapter(mMovesenseAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable so run
            bluetoothAdapter.enable();
        }
        startScanning();
        //unsub scanningSubscriptions.unsubscribe();
    }

    @Override
    public void displayScanResult(RxBleDevice bluetoothDevice, int rssi) {
        Log.d(TAG, "displayScanResult: " + bluetoothDevice.getName());
        mMovesenseModels.add(bluetoothDevice);
        Log.d(TAG, app.getMemoryTools().getSerial() + " vs " + bluetoothDevice.getMacAddress());
        if(bluetoothDevice.getMacAddress().equals(app.getMemoryTools().getSerial())) {
            connectToDevice(bluetoothDevice);
        } else {
            MovesenseAdapter movesenseAdapter = new MovesenseAdapter(mMovesenseModels, this);
            mMovesenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mMovesenseRecyclerView.setAdapter(movesenseAdapter);

            movesenseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerReceiver(BroadcastReceiver broadcastReceiver) {
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public boolean checkLocationPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(FyssaScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(FyssaScanActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(FyssaScanActivity.this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(FyssaScanActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(FyssaScanActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setPresenter(MovesenseContract.Presenter presenter) {
        mMovesensePresenter = presenter;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    startScanning();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMovesensePresenter.onBluetoothResult(requestCode, resultCode, data);
    }

    private void startScanning() {
        // Make sure we have location permission
        if (!checkLocationPermission()) {
            return;
        }

        Log.d(TAG, "START SCANNING !!!");
        // Start scanning
        scanningSubscriptions.add(RxBle.Instance.getClient().scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxBleScanResult>() {
                    @Override
                    public void call(RxBleScanResult rxBleScanResult) {
                        Log.d(TAG, "call: SCANNED: " + rxBleScanResult.getBleDevice().getName() + " " + rxBleScanResult.getBleDevice().getMacAddress());
                        RxBleDevice rxBleDevice = rxBleScanResult.getBleDevice();

                        if (rxBleDevice.getName() != null && rxBleDevice.getName().contains("Movesense")
                                && !mMovesenseModels.contains(rxBleDevice)) {

                            Log.d(TAG, "call: Add to list " + rxBleScanResult.getBleDevice().getName());
                            Log.d(TAG, app.getMemoryTools().getSerial() + " vs " +  rxBleScanResult.getBleDevice().getMacAddress());
                            if( rxBleScanResult.getBleDevice().getMacAddress().equals(app.getMemoryTools().getSerial())) {
                                connectToDevice( rxBleScanResult.getBleDevice());
                            } else {
                                mMovesenseModels.add(rxBleDevice);
                                // mView.displayScanResult(rxBleDevice, rxBleScanResult.getRssi());
                                mMovesenseAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "scanBleDevices(): ", throwable);
                    }
                }));
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(FyssaScanActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        final RxBleDevice rxBleDevice = (RxBleDevice) v.getTag();
        connectToDevice(rxBleDevice);
    }

    private void connectToDevice(final RxBleDevice rxBleDevice) {
        Log.d(TAG, "Connecting to : " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());

        mMovesenseProgressBar.setVisibility(View.GONE);

        Mds.builder().build(this).connect(rxBleDevice.getMacAddress(), null);

        // We are in connecting progress we don't need to scan anymore
        scanningSubscriptions.unsubscribe();
        mMovesensePresenter.stopScanning();

        ConnectingDialog.INSTANCE.showDialog(this, rxBleDevice.getMacAddress());

        // Monitor for connected devices
        connectedDevicesSubscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        // Stop refreshing
                        if (mdsConnectedDevice.getConnection() != null) {
                            ConnectingDialog.INSTANCE.dismissDialog();
                            Log.e(TAG, "Connected " + mdsConnectedDevice.toString());
                            Log.e(TAG, "List size(): " + MovesenseConnectedDevices.getConnectedDevices().size());

                            // Add connected device
                            if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                                MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();
                                Log.d(TAG, "instanceof MdsDeviceInfoNewSw: " + mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress()
                                        + " : " + mdsDeviceInfoNewSw.getDescription() + " : " + mdsDeviceInfoNewSw.getSerial()
                                        + " : " + mdsDeviceInfoNewSw.getSw());
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress(),
                                        mdsDeviceInfoNewSw.getDescription(),
                                        mdsDeviceInfoNewSw.getSerial(),
                                        mdsDeviceInfoNewSw.getSw()));
                            } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                                MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();
                                Log.d(TAG, "instanceof MdsDeviceInfoOldSw: " + mdsDeviceInfoOldSw.getAddressInfoOld()
                                        + " : " + mdsDeviceInfoOldSw.getDescription() + " : " + mdsDeviceInfoOldSw.getSerial()
                                        + " : " + mdsDeviceInfoOldSw.getSw());
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoOldSw.getAddressInfoOld(),
                                        mdsDeviceInfoOldSw.getDescription(),
                                        mdsDeviceInfoOldSw.getSerial(),
                                        mdsDeviceInfoOldSw.getSw()));
                            }

                            connectedDevicesSubscriptions.unsubscribe();

                            // We have a new SdsDevice
                            app.getMemoryTools().saveSerial(rxBleDevice.getMacAddress());
                            startMainActivity();
                        } else {
                            Log.e(TAG, "DISCONNECT");
                        }
                    }
                }, new ThrowableToastingAction(this)));
    }

    private void startMainActivity() {
        startActivity(new Intent(FyssaScanActivity.this, FyssaMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMovesensePresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        mMovesensePresenter.onDestroy();
        connectedDevicesSubscriptions.unsubscribe();
        scanningSubscriptions.unsubscribe();
    }
}
