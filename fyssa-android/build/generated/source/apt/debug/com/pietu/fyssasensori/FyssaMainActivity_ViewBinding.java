// Generated code from Butter Knife. Do not modify!
package com.pietu.fyssasensori;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class FyssaMainActivity_ViewBinding implements Unbinder {
  private FyssaMainActivity target;

  @UiThread
  public FyssaMainActivity_ViewBinding(FyssaMainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public FyssaMainActivity_ViewBinding(FyssaMainActivity target, View source) {
    this.target = target;

    target.connectionInfoTv = Utils.findRequiredViewAsType(source, R.id.fyssa_conn_infoTV, "field 'connectionInfoTv'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FyssaMainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.connectionInfoTv = null;
  }
}
