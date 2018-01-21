package com.pietu.fyssasensori;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuContract;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuPresenter;
import com.pietu.fyssasensori.tool.MemoryTools;

import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class FyssaSensorUpdateActivity extends AppCompatActivity implements DfuContract.View {
    private final String TAG = FyssaSensorUpdateActivity.class.getSimpleName();

    private CompositeSubscription subscriptions;
    private FyssaApp app;
    private DfuPresenter mDfuPresenter;

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

    }

    @Override
    public void onUploadCanceled() {

    }

    @Override
    public void displayError(String error) {

    }
}
