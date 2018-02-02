package com.pietu.fyssasensori;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuContract;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuPresenter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuUtil;
import com.movesense.mds.sampleapp.model.MdsAddressModel;
import com.movesense.mds.sampleapp.model.MdsInfo;
import com.pietu.fyssasensori.tool.MemoryTools;
import com.polidea.rxandroidble.RxBleDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import rx.subscriptions.CompositeSubscription;

public class FyssaSensorUpdateActivity extends AppCompatActivity implements MdsResponseListener, DfuProgressListener {
    private final String TAG = FyssaSensorUpdateActivity.class.getSimpleName();

    @BindView(R.id.fyssa_update_infoTV) TextView statusTV;
    private CompositeSubscription subscriptions;
    private FyssaApp app;
    private boolean mDfuInProgress;

    private Uri filePath;
    private List<MdsAddressModel> mMdsAddressModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fyssa_update);
        ButterKnife.bind(this);

        app = (FyssaApp) getApplication();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sensorin koodin päivitys");
        }

        subscriptions = new CompositeSubscription();

        DfuServiceListenerHelper.registerProgressListener(this, this);

        getDfuAddress(this);
        try {
            // Initialize streams
            InputStream in = getAssets().open("movesense_dfu.zip");
            OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/movesense_dfu.zip");
            copyAssetFiles(in, out);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "change fail");
        }
        filePath = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/movesense_dfu.zip"));
    }

    private final static int BUFFER_SIZE = 1024;

    private static void copyAssetFiles(InputStream in, OutputStream out) {
        try {

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        subscriptions.unsubscribe();
    }

    public void setDfuStatus(String status) {
        Log.d(TAG, "Status changed: " + status);
        statusTV.setText(status);
    }

    @Override
    public void onBackPressed() {
        if (mDfuInProgress) {

        } else {
            super.onBackPressed();
        }
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

    private void runDfuMode() {
        DfuUtil.runDfuModeOnConnectedDevice(this, this);
    }

    @Override
    public void onSuccess(String s) {
        RxBleDevice updateThis = MovesenseConnectedDevices.getConnectedRxDevice(0);
        BleManager.INSTANCE.disconnect(updateThis);
        BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = false;

        final String dfuAddressForConnection = mMdsAddressModelList.get(1).getAddress().replace("-", ":");
        DfuUtil.runDfuServiceUpdate(this, dfuAddressForConnection, updateThis.getName(), null, filePath.getPath());
    }

    @Override
    public void onError(MdsException e) {
        setDfuStatus("DFU mode failed" + e.toString());
    }

    @Override
    public void onDeviceConnecting(String s) {
        setDfuStatus("Connecting " +s);
    }

    @Override
    public void onDeviceConnected(String s) {
        setDfuStatus("CONNCENC " +s);
    }

    @Override
    public void onDfuProcessStarting(String s) {
        setDfuStatus("PROC STATING " +s);
    }

    @Override
    public void onDfuProcessStarted(String s) {
        setDfuStatus("PROC START " +s);
    }

    @Override
    public void onEnablingDfuMode(String s) {
        setDfuStatus("ENABLING DFU " +s);
    }

    @Override
    public void onProgressChanged(String s, int i, float v, float v1, int i1, int i2) {
        setDfuStatus(s + " " + i + " " + v + " " + v1 + " " + i1 + " " + i2);
    }

    @Override
    public void onFirmwareValidating(String s) {
        setDfuStatus("FIRM VAL " +s);
    }

    @Override
    public void onDeviceDisconnecting(String s) {
        setDfuStatus("DISCONNECTIUNG " +s);
    }

    @Override
    public void onDeviceDisconnected(String s) {
        setDfuStatus("DISC " +s);
    }

    @Override
    public void onDfuCompleted(String s) {
        Log.d(TAG, "completed " +s );
        setDfuStatus("completed " +s);
    }

    @Override
    public void onDfuAborted(String s) {
        Log.d(TAG, "ABORTED " +s );
        setDfuStatus("ABorted" + s);
    }

    @Override
    public void onError(String s, int i, int i1, String s1) {
        Log.d(TAG, "ERROR " +s + i +" " +  i1  + s1);
        setDfuStatus("ERROR " +s + i +" " +  i1  + s1);
    }
}
