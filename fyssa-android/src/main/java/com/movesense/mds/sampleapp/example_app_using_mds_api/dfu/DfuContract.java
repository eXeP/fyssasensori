package com.movesense.mds.sampleapp.example_app_using_mds_api.dfu;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.movesense.mds.sampleapp.example_app_using_mds_api.BasePresenter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.BaseView;
import com.polidea.rxandroidble.RxBleDevice;

/**
 * MVP Contract interface for DfuPresenter
 */

public interface DfuContract {

    interface Presenter extends BasePresenter {
        void onSelectDeviceClick(DfuActivity2 activity);
        void onSelectFileClick(Context context);
        void onStartUploadClick(Activity activity,Context context);
        void onActivityResult(Context context, int requestCode, int resultCode, Intent data);
        void onCursorLoadFinished(Loader<Cursor> loader, Cursor data);
        CursorLoader onCreateLoader(Context context, String[] projection, String selection,
                                    String[] selectionArgs, String sortOrder, Bundle args);
        void showQuitDialog(Activity activity);

        void registerConnectedDeviceObservable(Context context);
        void registerDfuServiceProgressListener(Context context);
        void onDeviceSelected(RxBleDevice rxBleDevice);

        void dismissScannerFragment();
    }

    interface View extends BaseView<DfuContract.Presenter> {
        void loadSelectedDeviceInfo();
        void loadSelectedFileInfo(String fileName, String fileSize, String fileType);
        void startActivityForResult(Intent intent, int requestCode);
        void restartLoader(int id, Bundle args);
        void setDfuStatus(String status);
        void setMovesenseSwVersion(String swVersion);
        void setDfuSwVersion(String dfuSwVersion);
        void clearUI();
        void blockUI();
        void setDfuPercentValue(String value);
        void setVisibilityPercentUpdateValue(int visibility);
        void onTransferCompleted();
        void onUploadCanceled();
        void displayError(String error);
    }

}
