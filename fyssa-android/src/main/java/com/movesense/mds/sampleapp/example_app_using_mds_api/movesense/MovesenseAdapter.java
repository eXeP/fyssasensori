package com.movesense.mds.sampleapp.example_app_using_mds_api.movesense;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.movesense.mds.sampleapp.R;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;

public class MovesenseAdapter extends RecyclerView.Adapter<MovesenseAdapter.ViewHolder> {

    private ArrayList<RxBleDevice> mMovesenseModelArrayList;
    private View.OnClickListener mOnClickListener;

    public MovesenseAdapter(ArrayList<RxBleDevice> movesenseModelArrayList, View.OnClickListener onClickListener) {
        mMovesenseModelArrayList = movesenseModelArrayList;
        mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movesense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RxBleDevice rxBleDevice = mMovesenseModelArrayList.get(position);

        holder.name.setText(rxBleDevice.getName());
        holder.address.setText(rxBleDevice.getMacAddress());

        // holder.rsid.setText(rxBleDevice.getRssi());

        holder.itemView.setTag(rxBleDevice);
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mMovesenseModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView address;
        private TextView rsid;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.movesense_name);
            address = (TextView) itemView.findViewById(R.id.movesense_address);
            rsid = (TextView) itemView.findViewById(R.id.movesense_rsid);
        }
    }
}
