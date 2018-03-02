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
import com.github.mikephil.charting.charts.LineChart;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class EcgActivity_ViewBinding implements Unbinder {
  private EcgActivity target;

  private View view2131231000;

  @UiThread
  public EcgActivity_ViewBinding(EcgActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public EcgActivity_ViewBinding(final EcgActivity target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.switchSubscription, "field 'mSwitchSubscription' and method 'onCheckedChanged'");
    target.mSwitchSubscription = Utils.castView(view, R.id.switchSubscription, "field 'mSwitchSubscription'", SwitchCompat.class);
    view2131231000 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChanged(p0, p1);
      }
    });
    target.mXAxisTextView = Utils.findRequiredViewAsType(source, R.id.x_axis_textView, "field 'mXAxisTextView'", TextView.class);
    target.mChart = Utils.findRequiredViewAsType(source, R.id.ecg_lineChart, "field 'mChart'", LineChart.class);
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    EcgActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mSwitchSubscription = null;
    target.mXAxisTextView = null;
    target.mChart = null;
    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;

    ((CompoundButton) view2131231000).setOnCheckedChangeListener(null);
    view2131231000 = null;
  }
}
