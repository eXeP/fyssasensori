package com.movesense.mds.handwave.fyssa_app;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageButton;
import android.widget.Toast;

import com.movesense.mds.handwave.bluetooth.BleManager;

import com.movesense.mds.handwave.R;

import com.movesense.mds.handwave.bluetooth.MdsRx;

import com.movesense.mds.handwave.scanner.MainScanActivity;


import butterknife.ButterKnife;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class SelectTestActivity extends AppCompatActivity {

    private AlertDialog alertDialog;
    private CompositeSubscription subscriptions;
    private boolean closeApp = false;
    private boolean disconnect = false;
    private ImageButton startButton;

    private final String TAG = SelectTestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_test);
        ButterKnife.bind(this);

        subscriptions = new CompositeSubscription();

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.close_app)
                .setMessage(R.string.do_you_want_to_close_application)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    closeApp = true;
                    BleManager.INSTANCE.disconnect(com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedRxDevice(0));
                })
                .setNegativeButton(R.string.no, (dialog, which) -> alertDialog.dismiss())
                .create();

        startButton = (ImageButton) findViewById(R.id.start_button);


        startButton.setOnClickListener(v -> {
            Log.d("ONCLICK", "Start the app");
                startActivity(new Intent(SelectTestActivity.this, MainScanActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        });

        subscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mdsConnectedDevice -> {
                    // Stop refreshing
                    if (mdsConnectedDevice.getConnection() == null) {
                        Log.e(TAG, "call: Rx Disconnect");
                        if (closeApp) {
                            if (Build.VERSION.SDK_INT >= 21) {
                                finishAndRemoveTask();
                            } else {
                                finish();
                            }
                        } else if (disconnect) {
                        } else {
                            com.movesense.mds.fyssabailu.ConnectionLostDialog.INSTANCE.showDialog(SelectTestActivity.this);
                        }
                    } else {
                        com.movesense.mds.fyssabailu.ConnectionLostDialog.INSTANCE.dismissDialog();
                        Log.e(TAG, "call: Rx Connect");
                    }
                }, new com.movesense.mds.fyssabailu.ThrowableToastingAction(this)));


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case R.id.update:
                startActivity(new Intent(SelectTestActivity.this, com.movesense.mds.handwave.update_app.FyssaSensorUpdateActivity.class));
                return true;

            case R.id.disconnect:
                BleManager.INSTANCE.disconnect(com.movesense.mds.handwave.update_app.model.MovesenseConnectedDevices.getConnectedRxDevice(0));
                disconnect = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscriptions.unsubscribe();
    }

    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

