package com.movesense.mds.sampleapp.example_app_using_mds_api.dfu;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.sampleapp.R;
import com.polidea.rxandroidble.RxBleDevice;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class DfuActivity2 extends AppCompatActivity implements DfuContract.View, LoaderManager.LoaderCallbacks<Cursor>, ScannerFragment.DeviceSelectionListener {

    @BindView(R.id.dfu_selectDevice_textView) TextView mDfuSelectDeviceTextView;
    @BindView(R.id.dfu_selectedDevice_nameTextView) TextView mDfuSelectedDeviceNameTextView;
    @BindView(R.id.dfu_selectedDevice_serialTextView) TextView mDfuSelectedDeviceSerialTextView;
    @BindView(R.id.dfu_selectedDevice_infoLayout) LinearLayout mDfuSelectedDeviceInfoLayout;
    @BindView(R.id.dfu_selectedDevice_containerLl) LinearLayout mDfuSelectedDeviceContainer;
    @BindView(R.id.dfu_selectedFile_textView) TextView mDfuSelectedFileTextView;
    @BindView(R.id.dfu_selectedFile_fileNameTextView) TextView mDfuSelectedFileFileNameTextView;
    @BindView(R.id.dfu_selectedFile_fileSizeTextView) TextView mDfuSelectedFileFileSizeTextView;
    @BindView(R.id.dfu_selectedFile_infoLayout) LinearLayout mDfuSelectedFileInfoLayout;
    @BindView(R.id.dfu_selectedFile_containerLl) LinearLayout mDfuSelectedFileContainer;
    @BindView(R.id.dfu_startUpload_btn) TextView mDfuStartUploadBtn;
    @BindView(R.id.dfu_status_Tv) TextView mDfuStatusTv;
    @BindView(R.id.dfu_percentProgress_Tv) TextView mDfuPercentProgressTv;

    private final String TAG = DfuActivity2.class.getSimpleName();
    @BindView(R.id.dfu_dfu_sw_version_tv) TextView mDfuDfuSwVersionTv;
    @BindView(R.id.dfu_movesense_sw_version_tv) TextView mDfuMovesenseSwVersionTv;

    private DfuPresenter mDfuPresenter;
    private ScannerFragment scannerFragment;
    private RxBleDevice selectedDevice;
    private CompositeSubscription mCompositeSubscription;
    private boolean isIncrementationNeeded = false;
    private boolean mResumed;
    private RxBleDevice mRxBleDevice;
    private boolean mDfuInProgress;
    private long mFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfu2);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("DFU");
        }

        mCompositeSubscription = new CompositeSubscription();

        mDfuPresenter = new DfuPresenter(this, this, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        mDfuPresenter.onCreate();

        mDfuStatusTv.setText(R.string.select_file_and_device);
        mDfuStartUploadBtn.setEnabled(false);

        mDfuPresenter.registerDfuServiceProgressListener(this);

        mDfuPresenter.registerConnectedDeviceObservable(this);



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
        mCompositeSubscription.unsubscribe();
    }

    @OnClick({R.id.dfu_selectedFile_containerLl, R.id.dfu_selectedDevice_containerLl, R.id.dfu_startUpload_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dfu_selectedFile_containerLl:
                mDfuPresenter.onSelectFileClick(this);
                break;
            case R.id.dfu_selectedDevice_containerLl:
                mDfuPresenter.onSelectDeviceClick(this);
                break;
            case R.id.dfu_startUpload_btn:

                mDfuPresenter.onStartUploadClick(this, this);

                blockUI();

                break;
        }
    }

    @Override
    public void setPresenter(DfuContract.Presenter presenter) {

    }

    @Override
    public void loadSelectedDeviceInfo() {

    }

    @Override
    public void loadSelectedFileInfo(String fileName, String fileSize, String fileType) {
        Log.d(TAG, "loadSelectedFileInfo: fileName: " + fileName + " fileSize: " + fileSize);
        mFileSize = Long.valueOf(fileSize);

        mDfuSelectedFileTextView.setVisibility(View.GONE);
        mDfuSelectedFileInfoLayout.setVisibility(View.VISIBLE);

        mDfuSelectedFileFileNameTextView.setText(fileName);
        mDfuSelectedFileFileSizeTextView.setText(mFileSize + " bytes");

        mDfuStatusTv.setText(R.string.select_file_and_device);

        validateFileAndDevice();
    }

    private void validateFileAndDevice() {
        if (mFileSize <= 0) {
            Log.e(TAG, "validateFileAndDevice: mFileSize <= 0");
            mDfuStatusTv.setText("File corrupted. Please select different file.");
            mDfuStartUploadBtn.setEnabled(false);
            mDfuStartUploadBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.inactive_background));
        } else if (mRxBleDevice != null) {
            Log.e(TAG, "validateFileAndDevice: mRxBleDevice != null");
            mDfuStatusTv.setText(R.string.select_file_and_device);
            mDfuStartUploadBtn.setEnabled(true);
            mDfuStartUploadBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.white_stroke));


        }
    }


    @Override
    public void restartLoader(int id, Bundle args) {
        Log.d(TAG, "restartLoader: id: " + id + " args: " + args);
        getLoaderManager().restartLoader(id, args, this);
    }

    @Override
    public void setDfuStatus(String status) {
        mDfuStatusTv.setText(status);
    }

    @Override
    public void setMovesenseSwVersion(String swVersion) {
        mDfuMovesenseSwVersionTv.setText(swVersion);
    }

    @Override
    public void setDfuSwVersion(String dfuSwVersion) {
        mDfuDfuSwVersionTv.setText(dfuSwVersion);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        mDfuPresenter.onActivityResult(DfuActivity2.this, requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: id: " + id);
        return mDfuPresenter.onCreateLoader(this, null, null, null, null, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished:");
        mDfuPresenter.onCursorLoadFinished(loader, data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset:");

        clearUI();
    }

    @Override
    public void onDeviceSelected(RxBleDevice device) {
        Log.e(TAG, "onDeviceSelected: " + device.getName());
        mRxBleDevice = device;

        mDfuPresenter.onDeviceSelected(device);
        mDfuPresenter.dismissScannerFragment();
        selectedDevice = device;

//        mDfuStatusTv.setText("Click Proceed Button");
//        mDfuStartUploadBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.black_stroke));

        mDfuSelectDeviceTextView.setVisibility(View.GONE);
        mDfuSelectedDeviceInfoLayout.setVisibility(View.VISIBLE);
        mDfuSelectedDeviceNameTextView.setText(device.getName());
        mDfuSelectedDeviceSerialTextView.setText(device.getMacAddress());

        validateFileAndDevice();
    }


    @Override
    public void onUploadCanceled() {
        clearUI();
        Toast.makeText(this, "Dfu Aborted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayError(String error) {
        Toast.makeText(DfuActivity2.this, "File corrupted", Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(final String message) {
        clearUI();
        Toast.makeText(this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransferCompleted() {
        clearUI();

        mDfuInProgress = false;

        mDfuStatusTv.setText("Updated successfully");
        mDfuPercentProgressTv.setText("0%");

        BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = true;
    }

    @Override
    public void blockUI() {

        mDfuSelectedFileContainer.setEnabled(false);
        mDfuSelectedFileContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.inactive_background));

        mDfuSelectedDeviceContainer.setEnabled(false);
        mDfuSelectedDeviceContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.inactive_background));

        mDfuStartUploadBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.inactive_background));
        mDfuStartUploadBtn.setEnabled(false);
    }

    @Override
    public void setDfuPercentValue(String value) {
        mDfuPercentProgressTv.setText(value);
    }

    @Override
    public void setVisibilityPercentUpdateValue(int visibility) {
        mDfuPercentProgressTv.setVisibility(visibility);
    }

    @Override
    public void clearUI() {
        mDfuStatusTv.setText(R.string.select_file_and_device);
        mDfuPercentProgressTv.setVisibility(View.INVISIBLE);

        mDfuSelectedFileContainer.setEnabled(true);
        mDfuSelectedFileContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.green_stroke));
        mDfuSelectedFileTextView.setVisibility(View.VISIBLE);
        mDfuSelectedFileInfoLayout.setVisibility(View.GONE);

        mDfuSelectDeviceTextView.setVisibility(View.VISIBLE);
        mDfuSelectedDeviceInfoLayout.setVisibility(View.GONE);
        mDfuSelectedDeviceContainer.setEnabled(true);
        mDfuSelectedDeviceContainer.setBackground(ContextCompat.getDrawable(this, R.drawable.red_stroke));

        mDfuStartUploadBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.inactive_background));
        mDfuStartUploadBtn.setEnabled(false);

        mDfuMovesenseSwVersionTv.setText(R.string.movesense_sw_version_device_must_be_connected);
    }

    @Override
    public void onBackPressed() {
        if (mDfuInProgress) {
            mDfuPresenter.showQuitDialog(this);
        } else {
            super.onBackPressed();
        }
    }
}
