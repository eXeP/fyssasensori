package com.fyssasensori;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.mds.sampleapp.BluetoothStatusMonitor;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.RxBle;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuActivity2;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuUtil;
import com.movesense.mds.sampleapp.model.MdsAddressModel;
import com.movesense.mds.sampleapp.model.MdsConnectedDevice;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoNewSw;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoOldSw;
import com.movesense.mds.sampleapp.model.MdsInfo;
import com.fyssasensori.tool.MemoryTools;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FyssaSensorUpdateActivity extends AppCompatActivity {
    private final String TAG = FyssaSensorUpdateActivity.class.getSimpleName();

    @BindView(R.id.fyssa_update_infoTV) TextView statusTV;
    private CompositeSubscription mCompositeSubscription;
    private CompositeSubscription scanningSubscription;
    private FyssaApp app;

    private File updateFile;
    private List<MdsAddressModel> mMdsAddressModelList;
    private Boolean isDfuInProgress = false;
    private RxBleDevice updateThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fyssa_update);
        ButterKnife.bind(this);

        app = (FyssaApp) getApplication();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sensorin koodin päivitys");
        }

        mCompositeSubscription = new CompositeSubscription();
        scanningSubscription = new CompositeSubscription();

        updateThis = MovesenseConnectedDevices.getConnectedRxDevice(0);

        setupListeners();
        loadUpdateFile();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProcess();
    }

    private void updateProcess() {
        // Get DFU address from /Info
        DfuUtil.getDfuAddress(this, new MdsResponseListener() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "getDfuAddress() onSuccess: " + s);

                MdsInfo mdsInfo = new Gson().fromJson(s, MdsInfo.class);

                if (mdsInfo != null && mdsInfo.getContent().getAddressInfoNew() != null) {
                    mMdsAddressModelList = mdsInfo.getContent().getAddressInfoNew();
                    // Run Dfu and use address from AddressInfoList
                    runDfuMode();
                } else if (mdsInfo != null) {
                    // Run Dfu and use device address
                    runDfuMode();
                } else {
                    Log.e(TAG, "getDfuAddress() Parsing MdsDeviceInfoNewSw Error");
                }
            }

            @Override
            public void onError(MdsException e) {

            }
        });


    }

    private void runDfuMode(){
        DfuUtil.runDfuModeOnConnectedDevice(this, new MdsResponseListener() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "runDfuModeOnConnectedDevice() onSuccess(): " + s);

                statusTV.setText("Dfu Mode enabled. Starting update.");

                BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(0));
                BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = false;
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, "onError(): ", e);
                statusTV.setText("DFU failed. Please try again");
            }
        });
    }

    private void loadUpdateFile() {
        updateFile = app.getMemoryTools().getAssetsFile(getAssets(), "movesense_dfu.zip");
        Log.d(TAG, "Update file length " + updateFile.length() + " exists: " + updateFile.exists());
        if (updateFile.length() == 0 || !updateFile.exists()) {
            startMainActivity();
            finish();
        }
    }

    private void setupListeners() {
        BluetoothStatusMonitor.INSTANCE.bluetoothStatusSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        if (integer == BluetoothAdapter.STATE_ON) {
                            Log.d(TAG, "call: BluetoothAdapter.STATE_ON");

                        } else if (integer == BluetoothAdapter.STATE_OFF) {
                            Log.d(TAG, "call: BluetoothAdapter.STATE_OFF");

                            statusTV.setText("ERROR: Bluetooth Disabled. Please try again when Bluetooth will be enabled");

                            Toast.makeText(FyssaSensorUpdateActivity.this, "Error: Blouetooth Turned Off", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "call bluetoothStatusSubject: ", throwable );
                    }
                });

        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(final MdsConnectedDevice mdsConnectedDevice) {
                        Log.d(TAG, "MdsConnectedDevice: " + mdsConnectedDevice.toString());
                        if (mdsConnectedDevice.getConnection() != null) {
                            Log.d(TAG, "Connected");
                            statusTV.setText("Connected");

                            // Display Movesense version and ask for Update
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

                                statusTV.setText(app.getString(R.string.movesense_sw_version) + " " + mdsDeviceInfoNewSw.getSwVersion());

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

                                statusTV.setText(app.getString(R.string.movesense_sw_version) + " " + mdsDeviceInfoOldSw.getSwVersion());
                            }

                            // Check battery level is fine for update
                            //getBatteryStatus(context);

                        } else {
                            Log.e(TAG, "Disconnected");

                            // Start DFU update after disconnect(DFU mode)
                            startFileUpdate();
                        }
                    }
                }));
        DfuServiceListenerHelper.registerProgressListener(this, new DfuProgressListener() {
            @Override
            public void onDeviceConnecting(String deviceAddress) {
                setStatus("Device connecting " + deviceAddress);
            }

            @Override
            public void onDeviceConnected(String deviceAddress) {
                setStatus("Device connected " + deviceAddress);
            }

            @Override
            public void onDfuProcessStarting(String deviceAddress) {
                setStatus("Dfu process starting " + deviceAddress);
            }

            @Override
            public void onDfuProcessStarted(String deviceAddress) {
                setStatus("Dfu process started " + deviceAddress);
            }

            @Override
            public void onEnablingDfuMode(String deviceAddress) {
                setStatus("Enabling dfu " + deviceAddress);
            }

            @Override
            public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
                setStatus("Progress changed " + deviceAddress + " " + percent + " " + speed + " " + currentPart + " " + partsTotal);
            }

            @Override
            public void onFirmwareValidating(String deviceAddress) {
                setStatus("Firmware validating " + deviceAddress);
            }

            @Override
            public void onDeviceDisconnecting(String deviceAddress) {
                setStatus("Device disconnecting " + deviceAddress);
            }

            @Override
            public void onDeviceDisconnected(String deviceAddress) {
                setStatus("Device disconnected " + deviceAddress);
            }

            @Override
            public void onDfuCompleted(String deviceAddress) {
                setStatus("Dfu completed " + deviceAddress);
                startMainActivity();
            }

            @Override
            public void onDfuAborted(String deviceAddress) {
                setStatus("Dfu aborted " + deviceAddress);
            }

            @Override
            public void onError(String deviceAddress, int error, int errorType, String message) {
                setStatus("Dfu error " + deviceAddress + " " + error + " " + errorType + " " + message);
            }
        });
    }

    private void setStatus(String status) {
        Log.d(TAG, "DFU STATUS CHANGE: " + status);
        statusTV.setText(status);
    }

    private void startFileUpdate(){
        Log.e(TAG, "onStartUploadClick: isIncrementationNeeded: TRUE");

        if (MovesenseConnectedDevices.getConnectedDevices().size() == 0) {
            Log.e(TAG, "startUpdatingProcess: NO CONNECTED DEVICES");
            return;
        }

        if (mMdsAddressModelList != null && mMdsAddressModelList.size() == 2) {
            // Use mac address from BLE-DFU
            String dfuAddressFromWb = mMdsAddressModelList.get(1).getAddress();
            final String dfuAddressForConnection = dfuAddressFromWb.replace("-", ":");
            String dfuAddressName = mMdsAddressModelList.get(1).getName();
            Log.e(TAG, "startUpdatingProcess: MANUFACTURE DATA: NAME" + dfuAddressName + " Wb Dfu Address " + dfuAddressFromWb
                    + " Changed DFU Address: " + dfuAddressForConnection);

            // FIXME: There is additional scanning in case of problem with BT DFU ?
            scanningSubscription.add(RxBle.Instance.getClient().scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<RxBleScanResult>() {
                        @Override
                        public void call(RxBleScanResult scanResult) {
                            Log.d(TAG, "scanResult: " + scanResult.getBleDevice().getName() + " : " +
                                    scanResult.getBleDevice().getMacAddress() + " vs" + dfuAddressForConnection);
                            if (!isDfuInProgress && dfuAddressForConnection.equals(scanResult.getBleDevice().getMacAddress())) {
                                Log.e(TAG, "scanResult: FOUND DEVICE FROM INTENT Connecting..." + scanResult.getBleDevice().getName() + " : " +
                                        scanResult.getBleDevice().getMacAddress());

                                scanningSubscription.unsubscribe();

                                isDfuInProgress = true;

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        DfuUtil.runDfuServiceUpdate(FyssaSensorUpdateActivity.this, dfuAddressForConnection, updateThis.getBluetoothDevice().getName(),
                                                Uri.fromFile(updateFile), updateFile.getPath());

                                        if (MovesenseConnectedDevices.getConnectedDevices().size() == 1) {
                                            MovesenseConnectedDevices.getConnectedDevices().remove(0);
                                        } else {
                                            Log.e(TAG, "ERROR: Wrong MovesenseConnectedDevices list size");
                                        }
                                    }
                                }, 2000);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "BEFORE CONNECT YOU NEED GRANT LOCATION PERMISSION !!!");
                            Log.e(TAG, "Connect Error: ", throwable);
                        }
                    }));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }

    private void startMainActivity() {
        startActivity(new Intent(FyssaSensorUpdateActivity.this, FyssaMainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

}
