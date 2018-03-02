package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
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
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.ConnectionLostDialog;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.FormatHelper;
import com.movesense.mds.sampleapp.example_app_using_mds_api.csv.CsvLogger;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.AngularVelocity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.model.InfoResponse;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnItemSelected;

public class AngularVelocityActivity extends AppCompatActivity implements BleManager.IBleConnectionMonitor {

    private final String LOG_TAG = AngularVelocityActivity.class.getSimpleName();
    private final String ANGULAR_VELOCITY_PATH = "Meas/Gyro/";
    private final String ANGULAR_VELOCITY_INFO_PATH = "/Meas/Gyro/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final List<String> spinnerRates = new ArrayList<>();
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    private String rate;
    private MdsSubscription mdsSubscription;


    @BindView(R.id.switchSubscription) SwitchCompat switchSubscription;
    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.x_axis_textView) TextView xAxisTextView;
    @BindView(R.id.y_axis_textView) TextView yAxisTextView;
    @BindView(R.id.z_axis_textView) TextView zAxisTextView;
    @BindView(R.id.angularVelocity_lineChart) LineChart mChart;
    private Dialog alertDialog;
    private CsvLogger mCsvLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_angular_velocity);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Angular Velocity");
        }

        mCsvLogger = new CsvLogger();

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.loading_information)
                .create();


        mConnectedDeviceNameTextView.setText("Serial: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSerial());

        mConnectedDeviceSwVersionTextView.setText("Sw version: " + MovesenseConnectedDevices.getConnectedDevice(0)
                .getSwVersion());

        xAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        yAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        zAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Init Empty Chart
        mChart.setData(new LineData());
        mChart.getDescription().setText("Angular Velocity");
        mChart.setTouchEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.invalidate();

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, spinnerRates);

        spinner.setAdapter(spinnerAdapter);

        // Display dialog
        alertDialog.show();

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX
                        + MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + ANGULAR_VELOCITY_INFO_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.d(LOG_TAG, "onSuccess(): " + data);

                        // Hide dialog
                        alertDialog.dismiss();

                        InfoResponse infoResponse = new Gson().fromJson(data, InfoResponse.class);

                        for (Integer inforate : infoResponse.content.sampleRates) {
                            spinnerRates.add(String.valueOf(inforate));

                            // Set first rate as default
                            if (rate == null) {
                                rate = String.valueOf(inforate);
                            }
                        }

                        spinnerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(LOG_TAG, "onError(): ", error);

                        // Hide dialog
                        alertDialog.dismiss();
                    }
                });

        BleManager.INSTANCE.addBleConnectionMonitorListener(this);
    }

    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            disableSpinner();

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);

            final LineData mLineData = mChart.getData();

            ILineDataSet xSet = mLineData.getDataSetByIndex(0);
            ILineDataSet ySet = mLineData.getDataSetByIndex(1);
            ILineDataSet zSet = mLineData.getDataSetByIndex(2);

            if (xSet == null) {
                xSet = createSet("Data x", getResources().getColor(android.R.color.holo_red_dark));
                ySet = createSet("Data y", getResources().getColor(android.R.color.holo_green_dark));
                zSet = createSet("Data z", getResources().getColor(android.R.color.holo_blue_dark));
                mLineData.addDataSet(xSet);
                mLineData.addDataSet(ySet);
                mLineData.addDataSet(zSet);
            }

            mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                    FormatHelper.formatContractToJson(MovesenseConnectedDevices.getConnectedDevice(0)
                            .getSerial(), ANGULAR_VELOCITY_PATH + rate), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String data) {
                            Log.d(LOG_TAG, "onSuccess(): " + data);

                            AngularVelocity angularVelocity = new Gson().fromJson(
                                    data, AngularVelocity.class);

                            if (angularVelocity != null) {

                                AngularVelocity.Array arrayData = angularVelocity.body.array[0];

                                mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                        "%.6f,%.6f,%.6f, ", arrayData.x, arrayData.y, arrayData.z));

                                xAxisTextView.setText(String.format(Locale.getDefault(),
                                        "x: %.6f", arrayData.x));
                                yAxisTextView.setText(String.format(Locale.getDefault(),
                                        "y: %.6f", arrayData.y));
                                zAxisTextView.setText(String.format(Locale.getDefault(),
                                        "z: %.6f", arrayData.z));

                                mLineData.addEntry(new Entry(angularVelocity.body.timestamp / 100, (float) arrayData.x), 0);
                                mLineData.addEntry(new Entry(angularVelocity.body.timestamp / 100, (float) arrayData.y), 1);
                                mLineData.addEntry(new Entry(angularVelocity.body.timestamp / 100, (float) arrayData.z), 2);
                                mLineData.notifyDataChanged();

                                // let the chart know it's data has changed
                                mChart.notifyDataSetChanged();

                                // limit the number of visible entries
                                mChart.setVisibleXRangeMaximum(50);

                                // move to the latest entry
                                mChart.moveViewToX(angularVelocity.body.timestamp / 100);
                            }
                        }

                        @Override
                        public void onError(MdsException error) {
                            Log.e(LOG_TAG, "onError(): ", error);

                            Toast.makeText(AngularVelocityActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            buttonView.setChecked(false);
                        }
                    });
        } else {
            enableSpinner();
            unSubscribe();
        }
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rate = spinnerRates.get(position);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

        BleManager.INSTANCE.removeBleConnectionMonitorListener(this);
    }

    private void unSubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }
        mCsvLogger.finishSavingLogs(LOG_TAG);
    }

    private void disableSpinner() {
        spinner.setEnabled(false);
    }

    private void enableSpinner() {
        spinner.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        if (requestCode == LogsManager.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
//            // if request is cancelled grantResults array is empty
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//
//                }
//            }
//        }
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(LOG_TAG, "onDisconnect: " + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConnectionLostDialog.INSTANCE.showDialog(AngularVelocityActivity.this);
            }
        });
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(LOG_TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
        mChart.getData().clearValues();
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
