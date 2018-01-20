package com.movesense.mds.sampleapp.example_app_using_mds_api.saved_data;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.movesense.mds.sampleapp.R;

public class SavedDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_data);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Data");
        }
    }
}
