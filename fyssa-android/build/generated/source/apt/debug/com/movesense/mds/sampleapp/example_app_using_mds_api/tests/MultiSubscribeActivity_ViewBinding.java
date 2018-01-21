// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MultiSubscribeActivity_ViewBinding implements Unbinder {
  private MultiSubscribeActivity target;

  private View view2131624173;

  private View view2131624177;

  private View view2131624181;

  @UiThread
  public MultiSubscribeActivity_ViewBinding(MultiSubscribeActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MultiSubscribeActivity_ViewBinding(final MultiSubscribeActivity target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.switchSubscriptionLinearAcc, "field 'switchSubscriptionLinearAcc' and method 'onCheckedChangedLinear'");
    target.switchSubscriptionLinearAcc = Utils.castView(view, R.id.switchSubscriptionLinearAcc, "field 'switchSubscriptionLinearAcc'", SwitchCompat.class);
    view2131624173 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChangedLinear(p0, p1);
      }
    });
    target.xAxisLinearAccTextView = Utils.findRequiredViewAsType(source, R.id.x_axis_linearAcc_textView, "field 'xAxisLinearAccTextView'", TextView.class);
    target.yAxisLinearAccTextView = Utils.findRequiredViewAsType(source, R.id.y_axis_linearAcc_textView, "field 'yAxisLinearAccTextView'", TextView.class);
    target.zAxisLinearAccTextView = Utils.findRequiredViewAsType(source, R.id.z_axis_linearAcc_textView, "field 'zAxisLinearAccTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.switchSubscriptionMagneticField, "field 'switchSubscriptionMagneticField' and method 'onCheckedChangedMagnetic'");
    target.switchSubscriptionMagneticField = Utils.castView(view, R.id.switchSubscriptionMagneticField, "field 'switchSubscriptionMagneticField'", SwitchCompat.class);
    view2131624177 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChangedMagnetic(p0, p1);
      }
    });
    target.xAxisMagneticFieldTextView = Utils.findRequiredViewAsType(source, R.id.x_axis_magneticField_textView, "field 'xAxisMagneticFieldTextView'", TextView.class);
    target.yAxisMagneticFieldTextView = Utils.findRequiredViewAsType(source, R.id.y_axis_magneticField_textView, "field 'yAxisMagneticFieldTextView'", TextView.class);
    target.zAxisMagneticFieldTextView = Utils.findRequiredViewAsType(source, R.id.z_axis_magneticField_textView, "field 'zAxisMagneticFieldTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.switchSubscriptionAngularVelocity, "field 'switchSubscriptionAngularVelocity' and method 'onCheckedChangedAngularVielocity'");
    target.switchSubscriptionAngularVelocity = Utils.castView(view, R.id.switchSubscriptionAngularVelocity, "field 'switchSubscriptionAngularVelocity'", SwitchCompat.class);
    view2131624181 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChangedAngularVielocity(p0, p1);
      }
    });
    target.xAxisAngularVelocityTextView = Utils.findRequiredViewAsType(source, R.id.x_axis_angularVelocity_textView, "field 'xAxisAngularVelocityTextView'", TextView.class);
    target.yAxisAngularVelocityTextView = Utils.findRequiredViewAsType(source, R.id.y_axis_angularVelocity_textView, "field 'yAxisAngularVelocityTextView'", TextView.class);
    target.zAxisAngularVelocityTextView = Utils.findRequiredViewAsType(source, R.id.z_axis_angularVelocity_textView, "field 'zAxisAngularVelocityTextView'", TextView.class);
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MultiSubscribeActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.switchSubscriptionLinearAcc = null;
    target.xAxisLinearAccTextView = null;
    target.yAxisLinearAccTextView = null;
    target.zAxisLinearAccTextView = null;
    target.switchSubscriptionMagneticField = null;
    target.xAxisMagneticFieldTextView = null;
    target.yAxisMagneticFieldTextView = null;
    target.zAxisMagneticFieldTextView = null;
    target.switchSubscriptionAngularVelocity = null;
    target.xAxisAngularVelocityTextView = null;
    target.yAxisAngularVelocityTextView = null;
    target.zAxisAngularVelocityTextView = null;
    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;

    ((CompoundButton) view2131624173).setOnCheckedChangeListener(null);
    view2131624173 = null;
    ((CompoundButton) view2131624177).setOnCheckedChangeListener(null);
    view2131624177 = null;
    ((CompoundButton) view2131624181).setOnCheckedChangeListener(null);
    view2131624181 = null;
  }
}
