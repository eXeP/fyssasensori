package com.pietu.fyssasensori;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.RxBle;
import com.movesense.mds.sampleapp.ThrowableToastingAction;
import com.movesense.mds.sampleapp.example_app_using_mds_api.ConnectingDialog;
import com.movesense.mds.sampleapp.example_app_using_mds_api.mainView.MainViewActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseAdapter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseContract;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesensePresenter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.sensors.sensors_list.SensorListActivity;
import com.movesense.mds.sampleapp.model.MdsConnectedDevice;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoNewSw;
import com.movesense.mds.sampleapp.model.MdsDeviceInfoOldSw;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class FyssaMainActivity extends AppCompatActivity {

    @BindView(R.id.fyssa_conn_infoTV) TextView connectionInfoTv;

    private final String TAG = FyssaMainActivity.class.getSimpleName();

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fyssa_main);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Fyssasensori");
        }

        subscriptions = new CompositeSubscription();

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            connectionInfoTv.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0).getSerial());
        } catch (Exception e) {
            startScanActivity();
        }

        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        if (mdsConnectedDevice.getConnection() == null) {
                            Log.d(TAG, "Disconnected");

                            if (MovesenseConnectedDevices.getConnectedDevices().size() == 1) {
                                MovesenseConnectedDevices.getConnectedDevices().remove(0);
                            } else {
                                Log.e(TAG, "ERROR: Wrong MovesenseConnectedDevices list size");
                            }

                            startScanActivity();

                        }
                    }
                }, new ThrowableToastingAction(this)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscriptions.clear();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit)
                .setMessage(R.string.disconnect_dialog_text)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Disconnecting...");

                        BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(0));
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void startScanActivity() {
        startActivity(new Intent(FyssaMainActivity.this, FyssaScanActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
