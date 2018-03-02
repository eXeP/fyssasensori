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

public class HeartRateTestActivity_ViewBinding implements Unbinder {
  private HeartRateTestActivity target;

  private View view2131230840;

  @UiThread
  public HeartRateTestActivity_ViewBinding(HeartRateTestActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public HeartRateTestActivity_ViewBinding(final HeartRateTestActivity target, View source) {
    this.target = target;

    View view;
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.heart_rate_switch, "field 'heartRateSwitch' and method 'onCheckedChange'");
    target.heartRateSwitch = Utils.castView(view, R.id.heart_rate_switch, "field 'heartRateSwitch'", SwitchCompat.class);
    view2131230840 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChange(p0, p1);
      }
    });
    target.heartRateValueTextView = Utils.findRequiredViewAsType(source, R.id.heart_rate_value_textView, "field 'heartRateValueTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    HeartRateTestActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;
    target.heartRateSwitch = null;
    target.heartRateValueTextView = null;

    ((CompoundButton) view2131230840).setOnCheckedChangeListener(null);
    view2131230840 = null;
  }
}
