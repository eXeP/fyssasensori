package com.movesense.mds.sampleapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Adapter for scanned devices
 */
public class ScannedDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = ScannedDevicesAdapter.class.getSimpleName();
    private Context mContext;
    private final boolean showOnlyMovesense;

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView addressTextView;

        DeviceViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.movesense_name);
            addressTextView = (TextView) itemView.findViewById(R.id.movesense_address);
        }
    }

    private final List<RxBleDevice> devices;
    private final PublishSubject<RxBleDevice> deviceSelectionSubject;

    public ScannedDevicesAdapter(Context context, boolean showOnlyMovesense) {
        mContext = context;
        this.showOnlyMovesense = showOnlyMovesense;
        devices = new ArrayList<>();
        deviceSelectionSubject = PublishSubject.create();

        setHasStableIds(true);
    }

    /**
     * Provide this adapter with a new RxBleScanResult from which the user may select
     * a device. Calling this does not always change the underlying model as
     * no duplicates are kept.
     *
     * @param scanResult New scan result to add to this adapter
     */
    public void handleScanResult(RxBleScanResult scanResult) {
        RxBleDevice device = scanResult.getBleDevice();
        Log.d(TAG, "Scanned Device Name : " + device.getName() + " Address: " + device.getMacAddress());

        // Show only Movesense devices on the list
        if (showOnlyMovesense) {
            if (device.getName() != null && device.getName().contains(mContext.getString(R.string.movesense_device_name))) {

                // Check for duplicates
                for (RxBleDevice d : devices) {
                    if (d.getMacAddress().equals(device.getMacAddress())) {
                        return;
                    }
                }

                // It was not a duplicate
                Log.d(TAG, "handleScanResult: Add device: " + device.getName());
                devices.add(device);
                notifyDataSetChanged();
            }
        } else if (device.getName() != null && device.getName().contains(mContext.getString(R.string.movesense_device_name)) ||
                device.getName() != null && device.getName().contains(mContext.getString(R.string.dfu_device_name))) {
            // Check for duplicates
            for (RxBleDevice d : devices) {
                if (d.getMacAddress().equals(device.getMacAddress())) {
                    return;
                }
            }

            Log.d(TAG, "handleScanResult: Add device: " + device.getName());
            // It was not a duplicate
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    /**
     * Gets an Observable that emits all clicks on devices in the list
     *
     * @return Observable emitting selected devices
     */
    public Observable<RxBleDevice> deviceSelectionObservable() {
        return deviceSelectionSubject.asObservable();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movesense, parent, false);
        final DeviceViewHolder viewHolder = new DeviceViewHolder(view);

        // Listen for clicks
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    deviceSelectionSubject.onNext(devices.get(pos));
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        RxBleDevice device = devices.get(position);

        deviceViewHolder.nameTextView.setText(device.getName());
        deviceViewHolder.addressTextView.setText(device.getMacAddress());
    }

    @Override
    public long getItemId(int position) {
        return devices.get(position).getMacAddress().hashCode();
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
