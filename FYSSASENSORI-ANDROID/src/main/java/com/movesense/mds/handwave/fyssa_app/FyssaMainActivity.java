package com.movesense.mds.handwave.fyssa_app;


import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.movesense.mds.fyssabailu.update_app.model.MovesenseConnectedDevices;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;

import com.movesense.mds.handwave.app_using_mds_api.model.HandwaveConfigGson;
import com.movesense.mds.handwave.app_using_mds_api.model.HandwaveGetResponse;
import com.movesense.mds.handwave.app_using_mds_api.model.HandwaveResponse;

import com.movesense.mds.handwave.R;

import com.movesense.mds.handwave.app_using_mds_api.model.InfoAppResponse;
import com.movesense.mds.handwave.bluetooth.MdsRx;
import com.movesense.mds.handwave.update_app.FyssaSensorUpdateActivity;
import com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices;


import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;


public class FyssaMainActivity extends AppCompatActivity {

    @BindView(R.id.fyssa_conn_infoTV) TextView connectionInfoTv;
    @BindView(R.id.get_handwave_button) Button getButton;
    @BindView(R.id.subscription_switch) Switch subSwitch;

    @BindView(R.id.nimi_tv) TextView nimiTv;

    private final String TAG = FyssaMainActivity.class.getSimpleName();

    private CompositeSubscription subscriptions = null;
    private FyssaApp app;

    private final String HANDWAVING_PATH_GET = "/Fyssa/Handwaving/Data";
    private final String SERVER_URL = "http://82.130.33.5:5000/handwave";

    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";

    private MdsSubscription mdsSubscription = null;
    private MdsSubscription mHandwaveSubscription = null;

    private Integer currentScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fyssa_main);
        ButterKnife.bind(this);


        app = (FyssaApp) getApplication();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Fyssasensori");
        }

        if (app.getMemoryTools().getName().equals(com.movesense.mds.handwave.tool.MemoryTools.DEFAULT_STRING)) {
            startInfoActivity();
            finish();
        } else {
            nimiTv.setText("Heiluttelija: " + app.getMemoryTools().getName());

            checkSensorSoftware();
            subscriptions = new CompositeSubscription();
            currentScore = app.getMemoryTools().getScore();
            Log.d(TAG, "Score when opening the app:" + currentScore);
        }
    }

    private void checkSensorSoftware() {
        Log.d(TAG, "Checking software");
        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + "/Info/App",
                null, new MdsResponseListener() {

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "/Info/App onSuccess: " + s);
                        InfoAppResponse infoAppResponse = new Gson().fromJson(s, InfoAppResponse.class);
                        Log.d(TAG, "Company: " + infoAppResponse.getContent().getCompany());
                        if (infoAppResponse.getContent() != null) {
                            Log.d(TAG, "Name: " + infoAppResponse.getContent().getName());
                            Log.d(TAG, "Version: " + infoAppResponse.getContent().getVersion());
                            Log.d(TAG, "Company: " + infoAppResponse.getContent().getCompany());
                        }
                        if (!infoAppResponse.getContent().getVersion().equals(FyssaApp.deviceVersion)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FyssaMainActivity.this);
                            builder.setMessage("Update?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            updateSensorSoftware();
                                            break;
                                    }
                                }
                            }).show();
                        }
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "Info onError: ", e);
                        if (e.toString().contains("404")) {
                            updateSensorSoftware();
                        }
                    }
                });
    }

    public static void removeAndDisconnectFromDevices() {
        com.movesense.mds.handwave.bluetooth.BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = false;
        while (com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedDevices().size() > 0) {
            com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.removeConnectedDevice((com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedDevice(0)));
        }
        while (com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getRxMovesenseConnectedDevices().size() > 0) {
            com.movesense.mds.handwave.bluetooth.BleManager.INSTANCE.disconnect(com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedRxDevice(0));
            com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.removeRxConnectedDevice(com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedRxDevice(0));
        }
    }

    private void updateSensorSoftware() {
        //removeAndDisconnectFromDevice();
        startActivity(new Intent(FyssaMainActivity.this, FyssaSensorUpdateActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            toast("Serial: " + com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedDevice(0).getSerial());
            connectionInfoTv.setText("" + currentScore);
        } catch (Exception e) {
            Log.e(TAG, "Connection failed", e);
            startScanActivity();
            return;
        }

        checkSensorSoftware();

        getHandwave();
    }

    @OnClick({R.id.get_handwave_button, R.id.timed_start/*R.id.start_service_button, R.id.stop_service_button, R.id.post_button*/ })
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.get_handwave_button:
                getHandwave();
                break;
            case R.id.timed_start:
                measureTimed();
                break;
            /*case R.id.start_service_button:
                subscribeDebug();
                break;
            case R.id.stop_service_button:
                unsubscribeDebug();
                break;
            case R.id.post_button:
                sendData();
                break;*/
        }
    }
    @OnCheckedChanged({R.id.subscription_switch})
    public void onCheckedChanged(boolean isChecked) {
        if (isChecked) {
            subscribeHandwaves();
        }
        else unSubscribeHandwave();
    }

    private void measureTimed() {
        final Calendar c = Calendar.getInstance();
        final int mHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int nHour = c.get(Calendar.HOUR_OF_DAY);
                        int nMinute = c.get(Calendar.MINUTE);
                        int time = (hourOfDay-nHour)*60 + (minute-nMinute);
                        if (time > 0) startService(time);
                        else  if (time < 0) startService(24*60+time);
                        else toast("Invalid time.");

                    }


                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    private void startService(int time) {
        HandwaveConfigGson fbc = new HandwaveConfigGson(new HandwaveConfigGson.HandwaveConfig(time));
        Log.d(TAG, "Putting: " + new Gson().toJson(fbc));
        Mds.builder().build(this).put(MdsRx.SCHEME_PREFIX +
                        com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + HANDWAVING_PATH_GET,
                new Gson().toJson(fbc), new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "Starting successfully");
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    private void getHandwave() {
        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + HANDWAVING_PATH_GET,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "Found a value from: " + MdsRx.SCHEME_PREFIX +
                                MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + HANDWAVING_PATH_GET);
                        String value = ( new Gson().fromJson(s, HandwaveGetResponse.class)).getHandwaveClean();
                        if (currentScore < (int)Float.parseFloat(value)) {
                            connectionInfoTv.setText(value);
                            currentScore = (int)Float.parseFloat(value);
                            app.getMemoryTools().saveScore(currentScore);
                        }

                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }


    private void subscribeDebug() {
        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + "/System/Debug/Config",
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "Sensor debug config: " + s);
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "Error on debug:", e);
                    }
                });
        mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER, "{\"Uri\": \"" +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + "/System/Debug/4\"}",
                        new MdsNotificationListener() {
                    @Override
                    public void onNotification(String s) {
                        Log.d(TAG, s);
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "Error on subscribing debug:", e);
                    }
                });
    }


    private void unsubscribeDebug() {
        if (mdsSubscription != null) mdsSubscription.unsubscribe();

        }


    private void subscribeHandwaves() {
        Log.d(TAG, "Subscribing.");
        mHandwaveSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER, "{\"Uri\": \"" +
                        MovesenseConnectedDevices.getConnectedDevice(0).getSerial() + HANDWAVING_PATH_GET + "\"}",
                new MdsNotificationListener() {
                    @Override
                    public void onNotification(String s){
                        Log.d(TAG, s);
                        HandwaveResponse response = new Gson().fromJson(s, HandwaveResponse.class);

                        int tmp = (int)Float.parseFloat(response.getHandwave());
                        if (tmp > currentScore) {
                            app.getMemoryTools().saveScore(currentScore);
                            currentScore = tmp;
                            if (currentScore > 100) sendData();
                            if (currentScore > 400) {
                                sendData();
                                connectionInfoTv.setText(response.getHandwaveClean() + "\n\nEntäpä jos lopettaisit.. tai edes vähentäisit!");
                            } else connectionInfoTv.setText(response.getHandwaveClean());
                        }


                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "Error on subscribing handwaves:", e);
                    }
                });
    }

    private void unSubscribeHandwave() {
        if (mHandwaveSubscription != null) mHandwaveSubscription.unsubscribe();
    }

    private void sendData() {
        DataSender sender = new DataSender();

        sender.send(SERVER_URL + "?name=" + app.getMemoryTools().getName() + "&amount=" + currentScore);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fyssa_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.remove_device) {
            subscriptions.clear();
            app.getMemoryTools().saveSerial(app.getMemoryTools().DEFAULT_STRING);
            startScanActivity();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        unsubscribeDebug();
        unSubscribeHandwave();
        super.onDestroy();
        if (subscriptions != null) subscriptions.clear();
    }

    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        unsubscribeDebug();
        unSubscribeHandwave();
        startActivity(new Intent(FyssaMainActivity.this, SelectTestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void startScanActivity() {
        startActivity(new Intent(FyssaMainActivity.this, SelectTestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void startInfoActivity() {
        startActivity(new Intent(FyssaMainActivity.this, FyssaInfoActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }


    public class DataSender {
        RequestQueue mRequestQueue;
        Cache cache;

        DataSender() {
            // Instantiate the cache
            cache = new DiskBasedCache(FyssaMainActivity.this.getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);
            // Start the queue
            mRequestQueue.start();
        }


        void send(String url) {
            // Formulate the request and handle the response.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Fail!", error);
                        }
                    });


            // Add the request to the RequestQueue.
            mRequestQueue.add(stringRequest);
        }

    }
}
