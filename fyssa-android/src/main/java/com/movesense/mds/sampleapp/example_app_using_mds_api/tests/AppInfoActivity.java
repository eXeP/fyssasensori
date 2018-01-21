package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.InfoAppResponse;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppInfoActivity extends AppCompatActivity {

    private static final String TAG = AppInfoActivity.class.getSimpleName();

    @BindView(R.id.sensorList_appInfo_name_tv) TextView mSensorListAppInfoNameTv;
    @BindView(R.id.sensorList_appInfo_version_tv) TextView mSensorListAppInfoVersionTv;
    @BindView(R.id.sensorList_appInfo_company_tv) TextView mSensorListAppInfoCompanyTv;
    @BindView(R.id.buttonGet) Button mButtonGet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        ButterKnife.bind(this);

        mSensorListAppInfoNameTv.setText("Name: Loading");
        mSensorListAppInfoVersionTv.setText("Version: Loading");
        mSensorListAppInfoCompanyTv.setText("Company: Loading");

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + "/Info/App",
                null, new MdsResponseListener() {

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "/Info/App onSuccess: " + s);
                        InfoAppResponse infoAppResponse = new Gson().fromJson(s, InfoAppResponse.class);

                        if (infoAppResponse.getContent() != null) {
                            mSensorListAppInfoNameTv.setText("Name: " + infoAppResponse.getContent().getName());
                            mSensorListAppInfoVersionTv.setText("Version: " + infoAppResponse.getContent().getVersion());
                            mSensorListAppInfoCompanyTv.setText("Company: " + infoAppResponse.getContent().getCompany());
                        }
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "Info onError: ", e);

                    }
                });
    }

    @OnClick(R.id.buttonGet)
    public void onViewClicked() {

        mSensorListAppInfoNameTv.setText("Name: Loading");
        mSensorListAppInfoVersionTv.setText("Version: Loading");
        mSensorListAppInfoCompanyTv.setText("Company: Loading");

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + "/Info/App",
                null, new MdsResponseListener() {

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "/Info/App onSuccess: " + s);
                        InfoAppResponse infoAppResponse = new Gson().fromJson(s, InfoAppResponse.class);

                        if (infoAppResponse.getContent() != null) {
                            mSensorListAppInfoNameTv.setText("Name: " + infoAppResponse.getContent().getName());
                            mSensorListAppInfoVersionTv.setText("Version: " + infoAppResponse.getContent().getVersion());
                            mSensorListAppInfoCompanyTv.setText("Company: " + infoAppResponse.getContent().getCompany());
                        }
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "Info onError: ", e);

                    }
                });
    }
}
