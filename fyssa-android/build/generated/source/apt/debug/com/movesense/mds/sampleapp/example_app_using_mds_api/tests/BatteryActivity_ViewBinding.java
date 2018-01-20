// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.tests;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BatteryActivity_ViewBinding implements Unbinder {
  private BatteryActivity target;

  private View view2131624068;

  @UiThread
  public BatteryActivity_ViewBinding(BatteryActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public BatteryActivity_ViewBinding(final BatteryActivity target, View source) {
    this.target = target;

    View view;
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.temperature_get_button, "field 'mTemperatureGetButton' and method 'onViewClicked'");
    target.mTemperatureGetButton = Utils.castView(view, R.id.temperature_get_button, "field 'mTemperatureGetButton'", Button.class);
    view2131624068 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
    target.mValueTextView = Utils.findRequiredViewAsType(source, R.id.value_textView, "field 'mValueTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BatteryActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;
    target.mTemperatureGetButton = null;
    target.mValueTextView = null;

    view2131624068.setOnClickListener(null);
    view2131624068 = null;
  }
}
