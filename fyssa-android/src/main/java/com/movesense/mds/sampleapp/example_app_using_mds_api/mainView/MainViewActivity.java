package com.movesense.mds.sampleapp.example_app_using_mds_api.mainView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.sampleapp.BuildConfig;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.SendLogsToGoogleDriveActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.dfu.DfuActivity2;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.connection.MultiConnectionActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainViewActivity extends AppCompatActivity {

    private final String TAG = MainViewActivity.class.getSimpleName();

    @BindView(R.id.mainView_movesense_Ll) LinearLayout mMainViewMovesenseLl;
    @BindView(R.id.mainView_multiConnection_Ll) LinearLayout mMainViewMultiConnectionLl;
    @BindView(R.id.mainView_dfu_Ll) LinearLayout mMainViewDfuLl;
    @BindView(R.id.mainView_savedData_Ll) LinearLayout mMainViewSavedDataLl;
    @BindView(R.id.mainView_tests_Ll) LinearLayout mMainViewTestsLl;
    @BindView(R.id.mainView_about_Ll) LinearLayout mMainViewAboutLl;
    @BindView(R.id.mainView_appVersion_tv) TextView mMainViewAppVersionTv;
    @BindView(R.id.mainView_libraryVersion_tv) TextView mMainViewLibraryVersionTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        ButterKnife.bind(this);

        String versionName = BuildConfig.VERSION_NAME;
        String libraryVersion = BuildConfig.MDS_VERSION;

        mMainViewAppVersionTv.setText(getString(R.string.application_version, versionName));
        mMainViewLibraryVersionTv.setText(getString(R.string.library_version, libraryVersion));

    }

    @OnClick({R.id.mainView_movesense_Ll, R.id.mainView_multiConnection_Ll, R.id.mainView_dfu_Ll, R.id.mainView_savedData_Ll, R.id.mainView_tests_Ll, R.id.mainView_about_Ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mainView_movesense_Ll:
                startActivity(new Intent(MainViewActivity.this, MovesenseActivity.class));
                break;
            case R.id.mainView_multiConnection_Ll:
                startActivity(new Intent(MainViewActivity.this, MultiConnectionActivity.class));
                break;
            case R.id.mainView_dfu_Ll:
                startActivity(new Intent(MainViewActivity.this, DfuActivity2.class));
                break;
            case R.id.mainView_savedData_Ll:
                startActivity(new Intent(MainViewActivity.this, SendLogsToGoogleDriveActivity.class));
                break;
            case R.id.mainView_tests_Ll:
                Toast.makeText(this, "We are working on it", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mainView_about_Ll:
                Toast.makeText(this, "We are working on it", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
