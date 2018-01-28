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

public class FyssaSensorUpdateActivity_ViewBinding implements Unbinder {
  private FyssaSensorUpdateActivity target;

  @UiThread
  public FyssaSensorUpdateActivity_ViewBinding(FyssaSensorUpdateActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public FyssaSensorUpdateActivity_ViewBinding(FyssaSensorUpdateActivity target, View source) {
    this.target = target;

    target.statusTV = Utils.findRequiredViewAsType(source, R.id.fyssa_update_infoTV, "field 'statusTV'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FyssaSensorUpdateActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.statusTV = null;
  }
}
