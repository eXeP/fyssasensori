package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.ConnectionLostDialog;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.FormatHelper;
import com.movesense.mds.sampleapp.example_app_using_mds_api.csv.CsvLogger;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.EcgModel;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class EcgActivity extends AppCompatActivity implements BleManager.IBleConnectionMonitor {

    @BindView(R.id.switchSubscription) SwitchCompat mSwitchSubscription;
    @BindView(R.id.x_axis_textView) TextView mXAxisTextView;
    @BindView(R.id.ecg_lineChart) LineChart mChart;
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;

    private static final String TAG = EcgActivity.class.getSimpleName();

    private final String ECG_VELOCITY_PATH = "Meas/ECG/125";
    private final String ECG_VELOCITY_INFO_PATH = "/Meas/ECG/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";

    private MdsSubscription mdsSubscription;
    int xValue = 0;
    boolean graphReady = true;

    private CsvLogger mCsvLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        ButterKnife.bind(this);

        mCsvLogger = new CsvLogger();

        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        // Init Empty Chart
        mChart.setData(new LineData());
        mChart.getDescription().setText("Ecg");
        mChart.setTouchEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.invalidate();

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);

    }

    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            final LineData mLineData = mChart.getData();

            ILineDataSet xSet = mLineData.getDataSetByIndex(0);

            if (xSet == null) {
                xSet = createSet("Data x", getResources().getColor(android.R.color.holo_red_dark));
                mLineData.addDataSet(xSet);
            }

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

                                            Log.d(TAG, "onNotification: xvalue: " + xValue + " data: " + ecgModel.getBody().getData()[i]);
                                            mLineData.addEntry(new Entry(xValue++, ecgModel.getBody().getData()[i]), 0);

                                            mLineData.notifyDataChanged();

                                            // let the chart know it's data has changed
                                            mChart.notifyDataSetChanged();

                                            // limit the number of visible entries
                                            mChart.setVisibleXRangeMaximum(200);

                                            // move to the latest entry
                                            mChart.moveViewToX(xValue);
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
        }
    }

    private void unSubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }
        mCsvLogger.finishSavingLogs(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == LogsManager.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
//            // if request is cancelled grantResults array is empty
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//                }
//            }
//        }
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(TAG, "onDisconnect: " + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

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

    private LineDataSet createSet(String name, int color) {
        LineDataSet set = new LineDataSet(null, name);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(0f);

        return set;
    }
}
