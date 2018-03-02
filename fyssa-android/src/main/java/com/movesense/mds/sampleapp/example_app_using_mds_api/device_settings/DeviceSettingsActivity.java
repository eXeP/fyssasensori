package com.movesense.mds.sampleapp.example_app_using_mds_api.device_settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.sampleapp.MdsRx;
import com.movesense.mds.sampleapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class DeviceSettingsActivity extends AppCompatActivity {

    private final String TAG = DeviceSettingsActivity.class.getSimpleName();

    private final String UART_PATH = "/System/Settings/UartOn";
    private final String POWER_OFF_AFTER_RESET_PATH = "/System/Settings/PowerOffAfterReset";
    private boolean uartSwitchState = true;
    private boolean powerOffSwitchState = true;

    @BindView(R.id.device_settings_uart_status_tv) TextView mDeviceSettingsUartStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.device_settings_uart_get_btn, R.id.device_settings_uart_set_btn, R.id.device_settings_powerOffAfterReset_set_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.device_settings_uart_get_btn:
                Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX + MovesenseConnectedDevices.getConnectedDevice(0).getSerial()
                        + UART_PATH, null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "onSuccess: " + s);

                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            boolean status = jsonObject.getBoolean("Content");
                            mDeviceSettingsUartStatusTv.setText("Enable: " + status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
                break;

            case R.id.device_settings_uart_set_btn:
                Mds.builder().build(this).put(MdsRx.SCHEME_PREFIX + MovesenseConnectedDevices.getConnectedDevice(0).getSerial()
                        + UART_PATH, "{\"State\":" + uartSwitchState + "}", new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "onSuccess: " + s);
                        Toast.makeText(DeviceSettingsActivity.this, "Status changed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
                break;

            case R.id.device_settings_powerOffAfterReset_set_btn:
                Mds.builder().build(this).put(MdsRx.SCHEME_PREFIX + MovesenseConnectedDevices.getConnectedDevice(0).getSerial()
                        + POWER_OFF_AFTER_RESET_PATH, "{\"State\":" + powerOffSwitchState + "}", new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "onSuccess: " + s);
                        Toast.makeText(DeviceSettingsActivity.this, "Status changed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
                break;
        }
    }

    @OnCheckedChanged(R.id.device_settings_uart_switch)
    public void onUartCheckedChange(CompoundButton compoundButton, boolean isChecked) {
        uartSwitchState = !isChecked;
    }

    @OnCheckedChanged(R.id.device_settings_powerOffAfterReset_switch)
    public void onPowerOffCheckedChange(CompoundButton compoundButton, boolean isChecked) {
        powerOffSwitchState = !isChecked;
    }
}
