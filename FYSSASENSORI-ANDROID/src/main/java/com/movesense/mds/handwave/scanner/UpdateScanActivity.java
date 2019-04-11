package com.movesense.mds.handwave.scanner;

import android.content.Intent;
import android.util.Log;

import com.movesense.mds.handwave.scanner.ScanActivity;


public class UpdateScanActivity extends ScanActivity {
    @Override
    protected void continueToActivity() {
        Log.d("UpdateScanActivity:", "Continuing back to earlier activity");
        setResult(RESULT_OK,new Intent());
        finishAndRemoveTask();
    }
}
