// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.sensors.sensors_list;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SensorListActivity_ViewBinding implements Unbinder {
  private SensorListActivity target;

  @UiThread
  public SensorListActivity_ViewBinding(SensorListActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SensorListActivity_ViewBinding(SensorListActivity target, View source) {
    this.target = target;

    target.mSensorListRecyclerView = Utils.findRequiredViewAsType(source, R.id.sensorList_recyclerView, "field 'mSensorListRecyclerView'", RecyclerView.class);
    target.mSensorListDeviceInfoTitleTv = Utils.findRequiredViewAsType(source, R.id.sensorList_deviceInfo_title_tv, "field 'mSensorListDeviceInfoTitleTv'", TextView.class);
    target.mSensorListDeviceInfoSerialTv = Utils.findRequiredViewAsType(source, R.id.sensorList_deviceInfo_serial_tv, "field 'mSensorListDeviceInfoSerialTv'", TextView.class);
    target.mSensorListDeviceInfoSwTv = Utils.findRequiredViewAsType(source, R.id.sensorList_deviceInfo_sw_tv, "field 'mSensorListDeviceInfoSwTv'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SensorListActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mSensorListRecyclerView = null;
    target.mSensorListDeviceInfoTitleTv = null;
    target.mSensorListDeviceInfoSerialTv = null;
    target.mSensorListDeviceInfoSwTv = null;
  }
}
