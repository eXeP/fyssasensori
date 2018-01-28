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

import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuContract;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuPresenter;
import com.pietu.fyssasensori.tool.MemoryTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class FyssaSensorUpdateActivity extends AppCompatActivity implements DfuContract.View, LoaderManager.LoaderCallbacks<Cursor> {
    private final String TAG = FyssaSensorUpdateActivity.class.getSimpleName();

    @BindView(R.id.fyssa_update_infoTV) TextView statusTV;
    private CompositeSubscription subscriptions;
    private FyssaApp app;
    private DfuPresenter mDfuPresenter;
    private boolean mDfuInProgress;

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

        mDfuPresenter = new DfuPresenter(this, this, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        mDfuPresenter.onCreate();

        mDfuPresenter.registerDfuServiceProgressListener(this);
        mDfuPresenter.registerConnectedDeviceObservable(this);
        mDfuPresenter.onDeviceSelected(MovesenseConnectedDevices.getRxMovesenseConnectedDevices().get(0));
        try {
            // Initialize streams
            InputStream in = getAssets().open("movesense_dfu.zip");
            OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/movesense_dfu.zip");
            copyAssetFiles(in, out);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "change fail");
        }
        mDfuPresenter.setUploadFile(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/movesense_dfu.zip")), null);
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
        mDfuPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDfuPresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        mDfuPresenter.onDestroy();
        subscriptions.unsubscribe();
    }

    @Override
    public void setPresenter(DfuContract.Presenter presenter) {

    }

    @Override
    public void loadSelectedDeviceInfo() {

    }

    @Override
    public void loadSelectedFileInfo(String fileName, String fileSize, String fileType) {
        if (Long.parseLong(fileSize) <= 0) {
            Log.e(TAG, "validateFileAndDevice: mFileSize <= 0");
        } else if (MovesenseConnectedDevices.getRxMovesenseConnectedDevices().get(0) != null) {
            Log.e(TAG, "validateFileAndDevice: mRxBleDevice != null");

            mDfuPresenter.onStartUploadClick(this, this);
        }
    }

    @Override
    public void restartLoader(int id, Bundle args) {
        Log.d(TAG, "restartLoader: id: " + id + " args: " + args);
        getLoaderManager().restartLoader(id, args, this);
    }

    @Override
    public void setDfuStatus(String status) {
        statusTV.setText(status);
    }

    @Override
    public void setMovesenseSwVersion(String swVersion) {

    }

    @Override
    public void setDfuSwVersion(String dfuSwVersion) {

    }

    @Override
    public void clearUI() {

    }

    @Override
    public void blockUI() {

    }

    @Override
    public void setDfuPercentValue(String value) {
        statusTV.setText(statusTV.getText() + " " + value);
    }

    @Override
    public void setVisibilityPercentUpdateValue(int visibility) {

    }

    @Override
    public void onTransferCompleted() {
        mDfuInProgress = false;

        BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = true;
    }

    @Override
    public void onUploadCanceled() {
        statusTV.setText("Canceled");
    }

    @Override
    public void displayError(String error) {
        statusTV.setText(error);
    }

    @Override
    public void onBackPressed() {
        if (mDfuInProgress) {
            mDfuPresenter.showQuitDialog(this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader: id: " + i);
        return mDfuPresenter.onCreateLoader(this, null, null, null, null, bundle);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished:");
        mDfuPresenter.onCursorLoadFinished(loader, cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset:");
        clearUI();
    }
}
