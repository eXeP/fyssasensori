// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.multi_connection.sensor_usage;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MultiSensorUsageActivity_ViewBinding implements Unbinder {
  private MultiSensorUsageActivity target;

  private View view2131624145;

  private View view2131624154;

  private View view2131624163;

  private View view2131624172;

  @UiThread
  public MultiSensorUsageActivity_ViewBinding(MultiSensorUsageActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MultiSensorUsageActivity_ViewBinding(final MultiSensorUsageActivity target, View source) {
    this.target = target;

    View view;
    target.mSelectedDeviceNameTv1 = Utils.findRequiredViewAsType(source, R.id.selectedDeviceName_Tv_1, "field 'mSelectedDeviceNameTv1'", TextView.class);
    target.mSelectedDeviceInfoLl1 = Utils.findRequiredViewAsType(source, R.id.selectedDeviceInfo_Ll_1, "field 'mSelectedDeviceInfoLl1'", LinearLayout.class);
    target.mSelectedDeviceNameTv2 = Utils.findRequiredViewAsType(source, R.id.selectedDeviceName_Tv_2, "field 'mSelectedDeviceNameTv2'", TextView.class);
    target.mSelectedDeviceInfoLl2 = Utils.findRequiredViewAsType(source, R.id.selectedDeviceInfo_Ll_2, "field 'mSelectedDeviceInfoLl2'", LinearLayout.class);
    target.mMultiSensorUsageSelectedDeviceMovesense1Ll = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_selectedDevice_movesense1Ll, "field 'mMultiSensorUsageSelectedDeviceMovesense1Ll'", LinearLayout.class);
    target.mMultiSensorUsageSelectedDeviceMovesense2Ll = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_selectedDevice_movesense2Ll, "field 'mMultiSensorUsageSelectedDeviceMovesense2Ll'", LinearLayout.class);
    target.mMultiSensorUsageLinearAccTextView = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_textView, "field 'mMultiSensorUsageLinearAccTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.multiSensorUsage_linearAcc_switch, "field 'mMultiSensorUsageLinearAccSwitch' and method 'onLinearAccCheckedChange'");
    target.mMultiSensorUsageLinearAccSwitch = Utils.castView(view, R.id.multiSensorUsage_linearAcc_switch, "field 'mMultiSensorUsageLinearAccSwitch'", SwitchCompat.class);
    view2131624145 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onLinearAccCheckedChange(p0, p1);
      }
    });
    target.mMultiSensorUsageLinearAccDevice1XTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_device1_x_tv, "field 'mMultiSensorUsageLinearAccDevice1XTv'", TextView.class);
    target.mMultiSensorUsageLinearAccDevice1YTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_device1_y_tv, "field 'mMultiSensorUsageLinearAccDevice1YTv'", TextView.class);
    target.mMultiSensorUsageLinearAccDevice1ZTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_device1_z_tv, "field 'mMultiSensorUsageLinearAccDevice1ZTv'", TextView.class);
    target.mMultiSensorUsageLinearAccDevice2XTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_device2_x_tv, "field 'mMultiSensorUsageLinearAccDevice2XTv'", TextView.class);
    target.mMultiSensorUsageLinearAccDevice2YTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_device2_y_tv, "field 'mMultiSensorUsageLinearAccDevice2YTv'", TextView.class);
    target.mMultiSensorUsageLinearAccDevice2ZTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_device2_z_tv, "field 'mMultiSensorUsageLinearAccDevice2ZTv'", TextView.class);
    target.mMultiSensorUsageLinearAccContainerLl = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_linearAcc_containerLl, "field 'mMultiSensorUsageLinearAccContainerLl'", LinearLayout.class);
    target.mMultiSensorUsageAngularVelocityTextView = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_textView, "field 'mMultiSensorUsageAngularVelocityTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.multiSensorUsage_angularVelocity_switch, "field 'mMultiSensorUsageAngularVelocitySwitch' and method 'onAngularVelocityCheckedChange'");
    target.mMultiSensorUsageAngularVelocitySwitch = Utils.castView(view, R.id.multiSensorUsage_angularVelocity_switch, "field 'mMultiSensorUsageAngularVelocitySwitch'", SwitchCompat.class);
    view2131624154 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onAngularVelocityCheckedChange(p0, p1);
      }
    });
    target.mMultiSensorUsageAngularVelocityDevice1XTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_device1_x_tv, "field 'mMultiSensorUsageAngularVelocityDevice1XTv'", TextView.class);
    target.mMultiSensorUsageAngularVelocityDevice1YTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_device1_y_tv, "field 'mMultiSensorUsageAngularVelocityDevice1YTv'", TextView.class);
    target.mMultiSensorUsageAngularVelocityDevice1ZTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_device1_z_tv, "field 'mMultiSensorUsageAngularVelocityDevice1ZTv'", TextView.class);
    target.mMultiSensorUsageAngularVelocityDevice2XTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_device2_x_tv, "field 'mMultiSensorUsageAngularVelocityDevice2XTv'", TextView.class);
    target.mMultiSensorUsageAngularVelocityDevice2YTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_device2_y_tv, "field 'mMultiSensorUsageAngularVelocityDevice2YTv'", TextView.class);
    target.mMultiSensorUsageAngularVelocityDevice2ZTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_device2_z_tv, "field 'mMultiSensorUsageAngularVelocityDevice2ZTv'", TextView.class);
    target.mMultiSensorUsageAngularVelocityContainerLl = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_angularVelocity_containerLl, "field 'mMultiSensorUsageAngularVelocityContainerLl'", LinearLayout.class);
    target.mMultiSensorUsageMagneticFieldTextView = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_textView, "field 'mMultiSensorUsageMagneticFieldTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.multiSensorUsage_magneticField_switch, "field 'mMultiSensorUsageMagneticFieldSwitch' and method 'onMagneticFieldCheckedChange'");
    target.mMultiSensorUsageMagneticFieldSwitch = Utils.castView(view, R.id.multiSensorUsage_magneticField_switch, "field 'mMultiSensorUsageMagneticFieldSwitch'", SwitchCompat.class);
    view2131624163 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onMagneticFieldCheckedChange(p0, p1);
      }
    });
    target.mMultiSensorUsageMagneticFieldDevice1XTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_device1_x_tv, "field 'mMultiSensorUsageMagneticFieldDevice1XTv'", TextView.class);
    target.mMultiSensorUsageMagneticFieldDevice1YTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_device1_y_tv, "field 'mMultiSensorUsageMagneticFieldDevice1YTv'", TextView.class);
    target.mMultiSensorUsageMagneticFieldDevice1ZTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_device1_z_tv, "field 'mMultiSensorUsageMagneticFieldDevice1ZTv'", TextView.class);
    target.mMultiSensorUsageMagneticFieldDevice2XTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_device2_x_tv, "field 'mMultiSensorUsageMagneticFieldDevice2XTv'", TextView.class);
    target.mMultiSensorUsageMagneticFieldDevice2YTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_device2_y_tv, "field 'mMultiSensorUsageMagneticFieldDevice2YTv'", TextView.class);
    target.mMultiSensorUsageMagneticFieldDevice2ZTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_device2_z_tv, "field 'mMultiSensorUsageMagneticFieldDevice2ZTv'", TextView.class);
    target.mMultiSensorUsageMagneticFieldContainerLl = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_magneticField_containerLl, "field 'mMultiSensorUsageMagneticFieldContainerLl'", LinearLayout.class);
    target.mMultiSensorUsageTemperatureTextView = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_temperature_textView, "field 'mMultiSensorUsageTemperatureTextView'", TextView.class);
    target.mMultiSensorUsageTemperatureDevice1ValueTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_temperature_device1_value_tv, "field 'mMultiSensorUsageTemperatureDevice1ValueTv'", TextView.class);
    target.mMultiSensorUsageTemperatureContainerLl = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_temperature_containerLl, "field 'mMultiSensorUsageTemperatureContainerLl'", LinearLayout.class);
    target.mMultiSensorUsageTemperatureDevice2ValueTv = Utils.findRequiredViewAsType(source, R.id.multiSensorUsage_temperature_device2_value_tv, "field 'mMultiSensorUsageTemperatureDevice2ValueTv'", TextView.class);
    view = Utils.findRequiredView(source, R.id.multiSensorUsage_temperature_switch, "field 'mMultiSensorUsageTemperatureSwitch' and method 'onTemperatureCheckedChange'");
    target.mMultiSensorUsageTemperatureSwitch = Utils.castView(view, R.id.multiSensorUsage_temperature_switch, "field 'mMultiSensorUsageTemperatureSwitch'", SwitchCompat.class);
    view2131624172 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onTemperatureCheckedChange(p0, p1);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    MultiSensorUsageActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mSelectedDeviceNameTv1 = null;
    target.mSelectedDeviceInfoLl1 = null;
    target.mSelectedDeviceNameTv2 = null;
    target.mSelectedDeviceInfoLl2 = null;
    target.mMultiSensorUsageSelectedDeviceMovesense1Ll = null;
    target.mMultiSensorUsageSelectedDeviceMovesense2Ll = null;
    target.mMultiSensorUsageLinearAccTextView = null;
    target.mMultiSensorUsageLinearAccSwitch = null;
    target.mMultiSensorUsageLinearAccDevice1XTv = null;
    target.mMultiSensorUsageLinearAccDevice1YTv = null;
    target.mMultiSensorUsageLinearAccDevice1ZTv = null;
    target.mMultiSensorUsageLinearAccDevice2XTv = null;
    target.mMultiSensorUsageLinearAccDevice2YTv = null;
    target.mMultiSensorUsageLinearAccDevice2ZTv = null;
    target.mMultiSensorUsageLinearAccContainerLl = null;
    target.mMultiSensorUsageAngularVelocityTextView = null;
    target.mMultiSensorUsageAngularVelocitySwitch = null;
    target.mMultiSensorUsageAngularVelocityDevice1XTv = null;
    target.mMultiSensorUsageAngularVelocityDevice1YTv = null;
    target.mMultiSensorUsageAngularVelocityDevice1ZTv = null;
    target.mMultiSensorUsageAngularVelocityDevice2XTv = null;
    target.mMultiSensorUsageAngularVelocityDevice2YTv = null;
    target.mMultiSensorUsageAngularVelocityDevice2ZTv = null;
    target.mMultiSensorUsageAngularVelocityContainerLl = null;
    target.mMultiSensorUsageMagneticFieldTextView = null;
    target.mMultiSensorUsageMagneticFieldSwitch = null;
    target.mMultiSensorUsageMagneticFieldDevice1XTv = null;
    target.mMultiSensorUsageMagneticFieldDevice1YTv = null;
    target.mMultiSensorUsageMagneticFieldDevice1ZTv = null;
    target.mMultiSensorUsageMagneticFieldDevice2XTv = null;
    target.mMultiSensorUsageMagneticFieldDevice2YTv = null;
    target.mMultiSensorUsageMagneticFieldDevice2ZTv = null;
    target.mMultiSensorUsageMagneticFieldContainerLl = null;
    target.mMultiSensorUsageTemperatureTextView = null;
    target.mMultiSensorUsageTemperatureDevice1ValueTv = null;
    target.mMultiSensorUsageTemperatureContainerLl = null;
    target.mMultiSensorUsageTemperatureDevice2ValueTv = null;
    target.mMultiSensorUsageTemperatureSwitch = null;

    ((CompoundButton) view2131624145).setOnCheckedChangeListener(null);
    view2131624145 = null;
    ((CompoundButton) view2131624154).setOnCheckedChangeListener(null);
    view2131624154 = null;
    ((CompoundButton) view2131624163).setOnCheckedChangeListener(null);
    view2131624163 = null;
    ((CompoundButton) view2131624172).setOnCheckedChangeListener(null);
    view2131624172 = null;
  }
}
