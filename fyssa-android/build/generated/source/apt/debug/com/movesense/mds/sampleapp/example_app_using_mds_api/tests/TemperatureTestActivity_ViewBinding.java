// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class TemperatureTestActivity_ViewBinding implements Unbinder {
  private TemperatureTestActivity target;

  private View view2131231009;

  private View view2131231007;

  @UiThread
  public TemperatureTestActivity_ViewBinding(TemperatureTestActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TemperatureTestActivity_ViewBinding(final TemperatureTestActivity target, View source) {
    this.target = target;

    View view;
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.temperature_switch, "field 'temperatureSwitch' and method 'onCheckedChange'");
    target.temperatureSwitch = Utils.castView(view, R.id.temperature_switch, "field 'temperatureSwitch'", SwitchCompat.class);
    view2131231009 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChange(p0, p1);
      }
    });
    target.temperatureSwitchLayout = Utils.findRequiredViewAsType(source, R.id.temperature_switch_layout, "field 'temperatureSwitchLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.temperature_get_button, "field 'temperatureGetButton' and method 'onClick'");
    target.temperatureGetButton = Utils.castView(view, R.id.temperature_get_button, "field 'temperatureGetButton'", Button.class);
    view2131231007 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick();
      }
    });
    target.temperatureKelvinTextView = Utils.findRequiredViewAsType(source, R.id.temperature_kelvin_textView, "field 'temperatureKelvinTextView'", TextView.class);
    target.temperatureCelsiusTextView = Utils.findRequiredViewAsType(source, R.id.temperature_celsius_textView, "field 'temperatureCelsiusTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    TemperatureTestActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;
    target.temperatureSwitch = null;
    target.temperatureSwitchLayout = null;
    target.temperatureGetButton = null;
    target.temperatureKelvinTextView = null;
    target.temperatureCelsiusTextView = null;

    ((CompoundButton) view2131231009).setOnCheckedChangeListener(null);
    view2131231009 = null;
    view2131231007.setOnClickListener(null);
    view2131231007 = null;
  }
}
