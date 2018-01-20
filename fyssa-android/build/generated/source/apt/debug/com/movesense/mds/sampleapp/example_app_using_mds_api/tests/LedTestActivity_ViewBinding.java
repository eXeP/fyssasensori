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

public class LedTestActivity_ViewBinding implements Unbinder {
  private LedTestActivity target;

  private View view2131624110;

  @UiThread
  public LedTestActivity_ViewBinding(LedTestActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public LedTestActivity_ViewBinding(final LedTestActivity target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.led_on_off_switch, "field 'ledOnOffSwitch' and method 'onCheckedChanged'");
    target.ledOnOffSwitch = Utils.castView(view, R.id.led_on_off_switch, "field 'ledOnOffSwitch'", SwitchCompat.class);
    view2131624110 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChanged(p0, p1);
      }
    });
    target.responseTextView = Utils.findRequiredViewAsType(source, R.id.response_textView, "field 'responseTextView'", TextView.class);
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    LedTestActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.ledOnOffSwitch = null;
    target.responseTextView = null;
    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;

    ((CompoundButton) view2131624110).setOnCheckedChangeListener(null);
    view2131624110 = null;
  }
}
