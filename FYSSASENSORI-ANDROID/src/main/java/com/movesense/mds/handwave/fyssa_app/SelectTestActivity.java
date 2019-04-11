package com.movesense.mds.handwave.fyssa_app;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageButton;
import android.widget.Toast;

import com.movesense.mds.handwave.bluetooth.BleManager;

import com.movesense.mds.handwave.R;


import com.movesense.mds.handwave.scanner.MainScanActivity;
import com.movesense.mds.handwave.tool.MemoryTools;
import com.movesense.mds.handwave.update_app.FyssaSensorUpdateActivity;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;

import rx.subscriptions.CompositeSubscription;

import static com.movesense.mds.handwave.fyssa_app.FyssaMainActivity.removeAndDisconnectFromDevices;

public class SelectTestActivity extends AppCompatActivity {

    private AlertDialog alertDialog;
    private CompositeSubscription subscriptions;
    private boolean closeApp = false;
    private boolean disconnect = false;
    private ImageButton startButton;

    private final String TAG = SelectTestActivity.class.getSimpleName();

    FyssaApp app;
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

        app = (FyssaApp) getApplication();

        startButton.setOnClickListener(v -> {
            Log.d("ONCLICK", "Start the app");
                startActivity(new Intent(SelectTestActivity.this, MainScanActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        });




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
            case R.id.reset_name:
                app.getMemoryTools().saveName(MemoryTools.DEFAULT_STRING);
                toast("Your username has been reset.");
                return true;

            case R.id.reset_serial:
                app.getMemoryTools().saveSerial(MemoryTools.DEFAULT_STRING);
                removeAndDisconnectFromDevices();
                toast("Known macs forgotten and disconnected.");
                return true;
            case R.id.update_sensor:
                startActivity(new Intent(SelectTestActivity.this, FyssaSensorUpdateActivity.class)
                );
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

