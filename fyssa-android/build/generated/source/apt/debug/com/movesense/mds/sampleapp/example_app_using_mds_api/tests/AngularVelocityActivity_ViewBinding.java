// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AngularVelocityActivity_ViewBinding implements Unbinder {
  private AngularVelocityActivity target;

  private View view2131231000;

  private View view2131230988;

  @UiThread
  public AngularVelocityActivity_ViewBinding(AngularVelocityActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AngularVelocityActivity_ViewBinding(final AngularVelocityActivity target, View source) {
    this.target = target;

    View view;
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.switchSubscription, "field 'switchSubscription' and method 'onCheckedChanged'");
    target.switchSubscription = Utils.castView(view, R.id.switchSubscription, "field 'switchSubscription'", SwitchCompat.class);
    view2131231000 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChanged(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.spinner, "field 'spinner' and method 'onItemSelected'");
    target.spinner = Utils.castView(view, R.id.spinner, "field 'spinner'", Spinner.class);
    view2131230988 = view;
    ((AdapterView<?>) view).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p0, View p1, int p2, long p3) {
        target.onItemSelected(p0, p1, p2, p3);
      }

      @Override
      public void onNothingSelected(AdapterView<?> p0) {
      }
    });
    target.xAxisTextView = Utils.findRequiredViewAsType(source, R.id.x_axis_textView, "field 'xAxisTextView'", TextView.class);
    target.yAxisTextView = Utils.findRequiredViewAsType(source, R.id.y_axis_textView, "field 'yAxisTextView'", TextView.class);
    target.zAxisTextView = Utils.findRequiredViewAsType(source, R.id.z_axis_textView, "field 'zAxisTextView'", TextView.class);
    target.mChart = Utils.findRequiredViewAsType(source, R.id.angularVelocity_lineChart, "field 'mChart'", LineChart.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    AngularVelocityActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;
    target.switchSubscription = null;
    target.spinner = null;
    target.xAxisTextView = null;
    target.yAxisTextView = null;
    target.zAxisTextView = null;
    target.mChart = null;

    ((CompoundButton) view2131231000).setOnCheckedChangeListener(null);
    view2131231000 = null;
    ((AdapterView<?>) view2131230988).setOnItemSelectedListener(null);
    view2131230988 = null;
  }
}
