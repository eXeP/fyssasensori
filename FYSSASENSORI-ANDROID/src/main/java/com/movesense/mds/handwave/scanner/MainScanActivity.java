package com.movesense.mds.handwave.scanner;

import android.content.Intent;
import android.util.Log;

import com.movesense.mds.handwave.fyssa_app.FyssaMainActivity;

public class MainScanActivity extends ScanActivity {
    @Override
    protected void continueToActivity() {
        Log.d("MainScanActivity/", "Going to FyssaMainActivity.");
        startActivity(new Intent(this, FyssaMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
