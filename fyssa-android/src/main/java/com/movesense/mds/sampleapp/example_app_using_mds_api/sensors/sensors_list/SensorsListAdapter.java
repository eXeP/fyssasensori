package com.movesense.mds.sampleapp.example_app_using_mds_api.sensors.sensors_list;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.movesense.mds.sampleapp.R;

import java.util.ArrayList;

public class SensorsListAdapter extends RecyclerView.Adapter<SensorsListAdapter.ViewHolder> {

    private ArrayList<SensorListItemModel> mSensorListItemModels;
    private View.OnClickListener mOnClickListener;

    public SensorsListAdapter(ArrayList<SensorListItemModel> sensorListItemModels, View.OnClickListener onClickListener) {
        mSensorListItemModels = sensorListItemModels;
        mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SensorListItemModel sensorListItemModel = mSensorListItemModels.get(position);

        holder.mTextView.setText(sensorListItemModel.getName());
        holder.itemView.setTag(sensorListItemModel.getName());
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mSensorListItemModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_sensorList_textView);
        }
    }
}
