// Generated code from Butter Knife. Do not modify!
package com.pietu.fyssasensori;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
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

    target.mMovesenseRecyclerView = Utils.findRequiredViewAsType(source, R.id.movesense_recyclerView, "field 'mMovesenseRecyclerView'", RecyclerView.class);
    target.mMovesenseInfoTv = Utils.findRequiredViewAsType(source, R.id.movesense_infoTv, "field 'mMovesenseInfoTv'", TextView.class);
    target.mMovesenseProgressBar = Utils.findRequiredViewAsType(source, R.id.movesense_progressBar, "field 'mMovesenseProgressBar'", ProgressBar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    FyssaMainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mMovesenseRecyclerView = null;
    target.mMovesenseInfoTv = null;
    target.mMovesenseProgressBar = null;
  }
}
