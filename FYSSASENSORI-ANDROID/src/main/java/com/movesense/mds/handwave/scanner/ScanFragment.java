package com.movesense.mds.handwave.scanner;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.movesense.mds.handwave.R;
import com.movesense.mds.fyssabailu.ThrowableToastingAction;
import com.movesense.mds.handwave.fyssa_app.FyssaApp;
import com.movesense.mds.handwave.bluetooth.RxBle;
import com.movesense.mds.handwave.tool.MemoryTools;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment for scanning a ble device
 */
public class ScanFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;

    public interface DeviceSelectionListener {
        void onDeviceSelected(RxBleDevice device);
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private DeviceSelectionListener deviceSelectionListener;
    private RxBleClient rxBleClient;
    private ScannedDevicesAdapter scannedDevicesAdapter;
    private CompositeSubscription subscriptions;

    private final String LOG_TAG = ScanFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof DeviceSelectionListener) {
            deviceSelectionListener = (DeviceSelectionListener) activity;
        } else {
            throw new IllegalArgumentException("Containing Activity does not implement DeviceSelectionListener");
        }

        getActivity().registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Ask For Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable so run
            bluetoothAdapter.enable();
        }

        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_location_on)
                    .setMessage(R.string.text_location_on)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        getActivity().finishAndRemoveTask();
                    })
                    .create().show();

        }

        // Capture instance of RxBleClient to make code look cleaner
        rxBleClient = RxBle.Instance.getClient();

        // Create one composite subscription to hold everything
        subscriptions = new CompositeSubscription();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Set up list and adapter for scanned devices
        String mac = ((FyssaApp) getActivity().getApplication()).getMemoryTools().getSerial();
        if (mac.equals(MemoryTools.DEFAULT_STRING)) {
            mac = "";
        } else {
            toast( "Only showing your device.");
        }
        scannedDevicesAdapter = new ScannedDevicesAdapter(true, mac);
        RecyclerView deviceList = view.findViewById(R.id.device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceList.setAdapter(scannedDevicesAdapter);
        deviceList.setItemAnimator(null);

        // Listen for device selection
        Subscription selectionSubscription = scannedDevicesAdapter.deviceSelectionObservable()
                .subscribe(rxBleDevice -> deviceSelectionListener.onDeviceSelected(rxBleDevice), new ThrowableToastingAction(getContext()));
        subscriptions.add(selectionSubscription);

        // Start scanning immediately
        startScanning();

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear all subscriptions
        subscriptions.unsubscribe();

        // Unregister BtReceiver
        getActivity().unregisterReceiver(btReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    // Try starting scan again
                    startScanning();
                }
            }
        }
    }

    private void startScanning() {
        // Make sure we have location permission
        if (!checkLocationPermission()) {
            return;
        }

        Log.d(LOG_TAG, "START SCANNING !!!");
        // Start scanning
        subscriptions.add(rxBleClient.scanBleDevices()
                .subscribe(rxBleScanResult ->
                        scannedDevicesAdapter.handleScanResult(rxBleScanResult), new ThrowableToastingAction(getContext())));
    }


    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    // The user bluetooth is ready to use.

                    // start scanning again in case of ready Bluetooth
                    startScanning();
                    return;
                }

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    return;
                }

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                }

            }
        }
    };


    private void toast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}
