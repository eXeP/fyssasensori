package com.movesense.mds.sampleapp.example_app_using_mds_api.dfu;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.RxBle;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.EnergyGetModel;
import com.movesense.mds.sampleapp.model.MdsAddressModel;
import com.movesense.mds.sampleapp.model.MdsConnectedDevice;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoNewSw;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoOldSw;
import com.movesense.mds.sampleapp.model.MdsInfo;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.io.File;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.app.Activity.RESULT_OK;

/**
 * DfuPresenter
 */

public class DfuPresenter implements DfuContract.Presenter {

    private final String TAG = DfuPresenter.class.getSimpleName();

    private static final String EXTRA_URI = "uri";
    private static final int SELECT_FILE_REQUEST_CODE = 99;
    private static final int SELECT_FILE_REQ = 100;
    private Context mContext;
    private DfuContract.View mView;
    private File mFile;
    private String mFilePath;
    private Uri mFileStreamUri;
    private CompositeSubscription mCompositeSubscription;
    private boolean isIncrementationNeeded;
    private ScannerFragment scannerFragment;
    private RxBleDevice mRxBleDevice;
    private int mFileSize;
    private boolean mResumed;
    private boolean mPaused;
    private final NotificationManager mNotificationManager;
    private boolean mDfuCompleted;
    private String mDfuError;
    private boolean tryAgainOnError = true;
    private DfuServiceInitiator mServiceInitiator;
    private String usedMacAdress;
    private CompositeSubscription scanningSubcription;
    private boolean isDfuInProgress = false;
    private List<MdsAddressModel> mMdsAddressModelList;

    public DfuPresenter(Context context, DfuContract.View view, NotificationManager notificationManager) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);

        mNotificationManager = notificationManager;

        mCompositeSubscription = new CompositeSubscription();
        scanningSubcription = new CompositeSubscription();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onResume() {
        mResumed = true;

        if (mDfuCompleted)
            mView.onTransferCompleted();
        if (mDfuError != null)
            mView.displayError(mDfuError);
        if (mDfuCompleted || mDfuError != null) {
            // if this activity is still open and upload process was completed, cancel the notification
            mNotificationManager.cancel(DfuService.NOTIFICATION_ID);
            mDfuCompleted = false;
            mDfuError = null;
        }
    }

    @Override
    public void onPause() {
        mResumed = false;
        mPaused = true;
    }

    @Override
    public void onDestroy() {
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onSelectDeviceClick(DfuActivity2 activity) {
        scannerFragment = new ScannerFragment();
        scannerFragment.show(activity.getSupportFragmentManager(), ScannerFragment.class.getName());
    }

    @Override
    public void onSelectFileClick(final Context context) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            // file browser has been found on the device
            Log.d(TAG, "onSelectFileClick: IF");
            mView.startActivityForResult(Intent.createChooser(intent, "Select File Browser"), SELECT_FILE_REQUEST_CODE);
            // context.startActivityForResult(Intent.createChooser(intent, "Select File Browser"), SELECT_FILE_REQUEST_CODE);
        } else {
            Log.d(TAG, "onSelectFileClick: ELSE");
            // there is no any file browser app, let's try to download one
            new AlertDialog.Builder(context).setTitle("File Browser not found")
                    .setItems(new CharSequence[]{"Find File Browser in Google Play", "Cancel"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=file manager")));
                                            break;
                                        case 1:
                                            dialog.dismiss();
                                            break;
                                    }
                                }
                            }).show();
        }
    }

    @Override
    public void onStartUploadClick(Activity activity, final Context context) {
        Log.e(TAG, "onStartUploadClick: mFilePath: " + mFilePath);
        Log.e(TAG, "onStartUploadClick: mFileStreamUri: " + mFileStreamUri);
        Log.e(TAG, "onStartUploadClick: mFile: " + mFile);

        mView.setDfuStatus("Connecting");
        Log.d(TAG, "device name " + mRxBleDevice.getName());
        //if (mRxBleDevice.getName().contains(context.getString(R.string.movesense_device_name))) {
        //    // Connect to the device -> Run DFU -> Update
        //    Mds.builder().build(mContext).connect(mRxBleDevice.getMacAddress(), null);
        //    isIncrementationNeeded = true;

         //else if (mRxBleDevice.getName().contains(context.getString(R.string.dfu_device_name))) {
            // Update
            new AlertDialog.Builder(context)
                    .setTitle("Update")
                    .setMessage("Are You sure You want update new software?")
                    .setPositiveButton("Yes, Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startUpdatingProcess(context, false);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mView.clearUI();
                    dialog.dismiss();
                }
            }).show();
        //}
    }

    private void startUpdatingProcess(final Context context, boolean isIncrementationNeeded) {
        if (mFileStreamUri == null && mFilePath == null) {
            Log.e(TAG, "onStartUploadClick: Select file before update");
            Toast.makeText(context, "Select file before update", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isIncrementationNeeded) {
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
                scanningSubcription.add(RxBle.Instance.getClient().scanBleDevices()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<RxBleScanResult>() {
                            @Override
                            public void call(RxBleScanResult scanResult) {
                                Log.d(TAG, "scanResult: " + scanResult.getBleDevice().getName() + " : " +
                                        scanResult.getBleDevice().getMacAddress());
                                if (!isDfuInProgress && dfuAddressForConnection.equals(scanResult.getBleDevice().getMacAddress())) {
                                    Log.e(TAG, "scanResult: FOUND DEVICE FROM INTENT Connecting..." + scanResult.getBleDevice().getName() + " : " +
                                            scanResult.getBleDevice().getMacAddress());

                                    scanningSubcription.unsubscribe();

                                    isDfuInProgress = true;

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            DfuUtil.runDfuServiceUpdate(context, dfuAddressForConnection, mRxBleDevice.getBluetoothDevice().getName(),
                                                    mFileStreamUri, mFilePath);

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
            } else {
                // Standard incrementation by one
                Log.e(TAG, "startUpdatingProcess: MANUFACTURE DATA ADDRESS EMPTY: ");
                usedMacAdress = DfuUtil.incrementMacAddress(mRxBleDevice.getMacAddress());

                // FIXME: There is additional scanning in case of problem with BT DFU ?
                scanningSubcription.add(RxBle.Instance.getClient().scanBleDevices()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<RxBleScanResult>() {
                            @Override
                            public void call(RxBleScanResult scanResult) {
                                Log.d(TAG, "scanResult: " + scanResult.getBleDevice().getName() + " : " +
                                        scanResult.getBleDevice().getMacAddress());
                                if (!isDfuInProgress && usedMacAdress.equals(scanResult.getBleDevice().getMacAddress())) {
                                    Log.e(TAG, "scanResult: FOUND DEVICE FROM INTENT Connecting..." + scanResult.getBleDevice().getName() + " : " +
                                            scanResult.getBleDevice().getMacAddress());

                                    scanningSubcription.unsubscribe();

                                    isDfuInProgress = true;

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            DfuUtil.runDfuServiceUpdate(context, usedMacAdress, mRxBleDevice.getBluetoothDevice().getName(),
                                                    mFileStreamUri, mFilePath);

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
        } else {
            Log.e(TAG, "onStartUploadClick: isIncrementationNeeded: FALSE");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    usedMacAdress = mRxBleDevice.getBluetoothDevice().getAddress();
                    DfuUtil.runDfuServiceUpdate(context, usedMacAdress, mRxBleDevice.getBluetoothDevice().getName(),
                            mFileStreamUri, mFilePath);
                }
            }, 5000);
        }
    }

    @Override
    public void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE_REQUEST_CODE) {
                setUploadFile(data.getData(), null);
            }
        }
    }

    public void setUploadFile(Uri uri, Uri mFileStreamUri2) {
        // clear previous data
        // mFileType = mFileTypeTmp;
        mFilePath = null;
        mFileStreamUri = null;

        mPaused = false;
        // and read new one
        /*
             * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
			 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
			 */
        if (uri.getScheme().equals("file")) {
            // the direct path to the file has been returned
            final String path = uri.getPath();
            mFile = new File(path);
            mFilePath = path;

            Log.d(TAG, "File " + mFile + " " + mFile.length());

            Log.e(TAG, "loadSelectedFileInfo: 1");
            loadSelectedFileInfo(mFile.getName(), mFile.length());
        } else if (uri.getScheme().equals("content")) {
            // an Uri has been returned
            mFileStreamUri = uri;
            // if application returned Uri for streaming, let's us it. Does it works?
            // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
            if (mFileStreamUri2 != null)
                mFileStreamUri = mFileStreamUri2;

            // file name and size must be obtained from Content Provider
            final Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_URI, uri);
            mView.restartLoader(SELECT_FILE_REQ, bundle);
        }
    }

    @Override
    public void onCursorLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onCursorLoadFinished: ");
        if (data != null && data.moveToNext()) {
            /*
             * Here we have to check the column indexes by name as we have requested for all. The order may be different.
			 */
            final String fileName = data.getString(data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)/* 0 DISPLAY_NAME */);
            /* 1 SIZE */
            mFileSize = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.SIZE) /* 1 SIZE */);
            String filePath = null;
            final int dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA);
            if (dataIndex != -1)
                filePath = data.getString(dataIndex /* 2 DATA */);
            if (!TextUtils.isEmpty(filePath))
                mFilePath = filePath;

            Log.e(TAG, "loadSelectedFileInfo: 2");
            loadSelectedFileInfo(fileName, mFileSize);
        } else {
//            dfuFileNameValueTv.setText(null);
//            dfuFileTypeValueTv.setText(null);
//            dfuFileSizeValueTv.setText(null);
//            mFilePath = null;
//            mFileStreamUri = null;
//            dfuFileStatusValueTv.setText("Error");
//            mStatusOk = false;
        }
    }

    @Override
    public CursorLoader onCreateLoader(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder, Bundle args) {
        final Uri uri = args.getParcelable(EXTRA_URI);

        return new CursorLoader(context, uri, null, null, null, null);
    }

    @Override
    public void showQuitDialog(final Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Dfu update in progress")
                .setMessage("Leaving while DFU update is in process may produce problems with Your Movesense")
                .setCancelable(false)
                .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.onBackPressed();
                    }
                })
                .setNegativeButton("Wait for finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void registerConnectedDeviceObservable(final Context context) {
        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(final MdsConnectedDevice mdsConnectedDevice) {
                        Log.d(TAG, "MdsConnectedDevice: " + mdsConnectedDevice.toString());
                        if (mdsConnectedDevice.getConnection() != null) {
                            Log.d(TAG, "Connected");
                            mView.setDfuStatus("Connected");

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

                                mView.setMovesenseSwVersion(context.getString(R.string.movesense_sw_version) + " " + mdsDeviceInfoNewSw.getSwVersion());

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

                                mView.setMovesenseSwVersion(context.getString(R.string.movesense_sw_version) + " " + mdsDeviceInfoOldSw.getSwVersion());
                            }

                            // Check battery level is fine for update
                            getBatteryStatus(context);

                        } else {
                            Log.e(TAG, "Disconnected");

                            // Start DFU update after disconnect(DFU mode)
                            startUpdatingProcess(context, isIncrementationNeeded);
                        }
                    }
                }));
    }

    @Override
    public void registerDfuServiceProgressListener(Context context) {
        DfuServiceListenerHelper.registerProgressListener(context, dfuProgressListener);
    }

    @Override
    public void onDeviceSelected(RxBleDevice rxBleDevice) {
        Log.d(TAG, "onDeviceSelected: " + rxBleDevice.getName() + " : " + rxBleDevice.getMacAddress());
        mRxBleDevice = rxBleDevice;
    }

    @Override
    public void dismissScannerFragment() {
        scannerFragment.dismiss();
    }

    DfuProgressListener dfuProgressListener = new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDeviceConnecting");
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDeviceConnected");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDfuProcessStarting");
            mView.setDfuStatus("Uploading");
            mView.setVisibilityPercentUpdateValue(View.VISIBLE);
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDfuProcessStarted");
        }

        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            Log.d(TAG, "DfuProgress onEnablingDfuMode");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            Log.d(TAG, "DfuProgress onProgressChanged percent: " + percent);
            mView.setDfuPercentValue(percent + "%");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            Log.d(TAG, "DfuProgress onFirmwareValidating");
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDeviceDisconnecting");
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDeviceDisconnected");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDfuCompleted");
            mView.setDfuStatus("Application has been sent successfully");
            mView.onTransferCompleted();

            isDfuInProgress = false;

            if (mResumed) {
                // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if this activity is still open and upload process was completed, cancel the notification
                        mNotificationManager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 200);
            } else {
                //  Save that the DFU process has finished
                mDfuCompleted = true;
            }
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            Log.d(TAG, "DfuProgress onDfuAborted");
            mView.setDfuStatus("Aborted");
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mView.onUploadCanceled();

                    // if this activity is still open and upload process was completed, cancel the notification
                    mNotificationManager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onError(final String deviceAddress, int error, int errorType, String message) {
            Log.e(TAG, "DfuProgress onError: " + message + " address: " + deviceAddress
                    + " error: " + error + " errorType: " + errorType);

            if (tryAgainOnError) {
                Log.e(TAG, "onError: tryAgainOnError");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "onError: run");
                        if (mRxBleDevice != null) {
                            DfuUtil.runDfuServiceUpdate(mContext, deviceAddress, mRxBleDevice.getBluetoothDevice().getName(),
                                    mFileStreamUri, mFilePath);

                            if (MovesenseConnectedDevices.getConnectedDevices().size() == 1) {
                                MovesenseConnectedDevices.getConnectedDevices().remove(0);
                            } else {
                                Log.e(TAG, "ERROR: Wrong MovesenseConnectedDevices list size");
                            }
                        }
                    }
                }, 4000);

                tryAgainOnError = false;
                return;
            }

            mView.clearUI();
            mView.setDfuStatus("ERROR: Something went wrong. Please try again.");

            if (mResumed) {
                mView.displayError(message);

                // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if this activity is still open and upload process was completed, cancel the notification
                        mNotificationManager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 200);
            } else {
                mDfuError = message;
            }
        }
    };

    private void loadSelectedFileInfo(String fileName, long fileSize) {
        mView.loadSelectedFileInfo(fileName, String.valueOf(fileSize), "");
    }

    private void getBatteryStatus(final Context context) {
        DfuUtil.getBatteryStatus(context, new MdsResponseListener() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "getBatteryStatus() onSuccess: " + s);

                EnergyGetModel energyModel = new Gson().fromJson(s, EnergyGetModel.class);

                if (energyModel.content > 20) {

                    // If battery level is fine for update show confirmation dialog
                    showConfirmationDialog(context);

                } else {
                    Toast.makeText(context, R.string.energy_value_to_low_for_update, Toast.LENGTH_SHORT).show();

                    mView.clearUI();

                    mView.setDfuStatus(context.getString(R.string.energy_value_to_low_for_update));
                }
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, "onError(): ", e);
            }
        });
    }

    private void runDfuMode(Context context) {
        DfuUtil.runDfuModeOnConnectedDevice(context, new MdsResponseListener() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "runDfuModeOnConnectedDevice() onSuccess(): " + s);

                mView.setDfuStatus("Dfu Mode enabled. Starting update.");

                BleManager.INSTANCE.disconnect(mRxBleDevice);
                BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = false;
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, "onError(): ", e);
                mView.setDfuStatus("DFU failed. Please try again");

                mView.clearUI();
            }
        });
    }

    private void showConfirmationDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Update")
                .setMessage("Are You sure You want update new software?")
                .setPositiveButton("Yes, Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        getDfuAddress(context);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mView.clearUI();
                dialog.dismiss();
            }
        }).show();
    }

    private void getDfuAddress(final Context context) {
        // Get DFU address from /Info
        DfuUtil.getDfuAddress(context, new MdsResponseListener() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "getDfuAddress() onSuccess: " + s);

                MdsInfo mdsInfo = new Gson().fromJson(s, MdsInfo.class);

                if (mdsInfo != null && mdsInfo.getContent().getAddressInfoNew() != null) {
                    mMdsAddressModelList = mdsInfo.getContent().getAddressInfoNew();
                    // Run Dfu and use address from AddressInfoList
                    runDfuMode(context);
                } else if (mdsInfo != null) {
                    // Run Dfu and use device address
                    runDfuMode(context);
                } else {
                    Log.e(TAG, "getDfuAddress() Parsing MdsDeviceInfoNewSw Error");
                }
            }

            @Override
            public void onError(MdsException e) {

            }
        });
    }

}
