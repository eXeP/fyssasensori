// Generated code from Butter Knife. Do not modify!
package com.pietu.fyssasensori;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class FyssaInfoActivity_ViewBinding implements Unbinder {
  private FyssaInfoActivity target;

  @UiThread
  public FyssaInfoActivity_ViewBinding(FyssaInfoActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public FyssaInfoActivity_ViewBinding(FyssaInfoActivity target, View source) {
    this.target = target;

    target.nameText = Utils.findRequiredViewAsType(source, R.id.fyssa_info_nameET, "field 'nameText'", EditText.class);
    target.doneButton = Utils.findRequiredViewAsType(source, R.id.fyssa_info_doneBT, "field 'doneButton'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FyssaInfoActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.nameText = null;
    target.doneButton = null;
  }
}
