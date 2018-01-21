package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.ConnectionLostDialog;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.FormatHelper;
import com.movesense.mds.sampleapp.example_app_using_mds_api.logs.LogsManager;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.EcgModel;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class EcgActivity extends AppCompatActivity implements BleManager.IBleConnectionMonitor {

    @BindView(R.id.switchSubscription) SwitchCompat mSwitchSubscription;
    @BindView(R.id.spinner) Spinner mSpinner;
    @BindView(R.id.x_axis_textView) TextView mXAxisTextView;
    @BindView(R.id.graphView) GraphView mGraphView;
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;

    private static final String TAG = EcgActivity.class.getSimpleName();

    private final String ECG_VELOCITY_PATH = "Meas/ECG/125";
    private final String ECG_VELOCITY_INFO_PATH = "/Meas/ECG/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";

    private LineGraphSeries seriesX = new LineGraphSeries();
    private MdsSubscription mdsSubscription;
    int xValue = 0;
    boolean graphReady = true;

    private LogsManager logsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        ButterKnife.bind(this);

        logsManager = new LogsManager(this);

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        setUpGraphView();

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

    }

    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {

            // Clear Logcat
            logsManager.clearAdbLogcat();

            mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), ECG_VELOCITY_PATH), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(TAG, "onSuccess(): " + data);

                            final EcgModel ecgModel = new Gson().fromJson(
                                    data, EcgModel.class);

                            if (ecgModel.getBody() != null) {

                                if (graphReady) {
                                    graphReady = false;
                                    mXAxisTextView.setText(Arrays.toString(ecgModel.getBody().getData()));

                                    for (int i = 0; i < ecgModel.getBody().getData().length; i++) {
                                        double cloneY = ecgModel.getBody().getData()[i];

                                        if (cloneY <= 320 && cloneY >= -320) {
                                            try {
                                                Log.d(TAG, "onNotification: xvalue: " + xValue + " data: " + ecgModel.getBody().getData()[i]);
                                                seriesX.appendData(
                                                        new DataPoint(xValue++, ecgModel.getBody().getData()[i]), true,
                                                        Integer.MAX_VALUE);

                                            } catch (IllegalArgumentException e) {
                                                Log.e(TAG, "GraphView error ", e);
                                            }
                                        }
                                    }

                                    graphReady = true;
                                }
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(TAG, "onError(): ", error);

                            Toast.makeText(EcgActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            buttonView.setChecked(false);
                        }
                    });
        } else {
            unSubscribe();

            // Save logs
            saveAdbLogsToFile(TAG);
        }
    }

    private void unSubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    private void saveAdbLogsToFile(String logTag) {
        if (!logsManager.checkRuntimeWriteExternalStoragePermission(this, this)) {
            return;
        }
        logsManager.saveLogsToSdCard(logTag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LogsManager.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            // if request is cancelled grantResults array is empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    // Save logs
                    saveAdbLogsToFile(TAG);
                }
            }
        }
    }

    private void setUpGraphView() {
        mGraphView.addSeries(seriesX);
        seriesX.setDrawAsPath(true);
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(300);

        setSeriesColor(android.R.color.holo_red_dark, seriesX);
    }

    private void setSeriesColor(@ColorRes int colorRes, LineGraphSeries series) {
        int color = getResources().getColor(colorRes);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(color);
        series.setCustomPaint(paint);
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(TAG, "onDisconnect: " + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUpGraphView();
                ConnectionLostDialog.INSTANCE.showDialog(EcgActivity.this);
            }
        });
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }

    @Override
    public void onConnectError(String s, Throwable throwable) {

    }
}
