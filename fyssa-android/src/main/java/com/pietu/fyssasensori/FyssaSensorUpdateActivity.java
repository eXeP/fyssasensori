package com.pietu.fyssasensori;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuContract;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuPresenter;
import com.pietu.fyssasensori.tool.MemoryTools;

import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class FyssaSensorUpdateActivity extends AppCompatActivity implements DfuContract.View, LoaderManager.LoaderCallbacks<Cursor> {
    private final String TAG = FyssaSensorUpdateActivity.class.getSimpleName();

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

    }

    @Override
    public void restartLoader(int id, Bundle args) {
        Log.d(TAG, "restartLoader: id: " + id + " args: " + args);
        getLoaderManager().restartLoader(id, args, this);
    }

    @Override
    public void setDfuStatus(String status) {

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

    }

    @Override
    public void displayError(String error) {

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
