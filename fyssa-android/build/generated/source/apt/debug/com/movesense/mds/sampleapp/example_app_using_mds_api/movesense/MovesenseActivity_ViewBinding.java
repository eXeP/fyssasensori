// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api.movesense;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MovesenseActivity_ViewBinding implements Unbinder {
  private MovesenseActivity target;

  private View view2131624120;

  private View view2131624121;

  @UiThread
  public MovesenseActivity_ViewBinding(MovesenseActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MovesenseActivity_ViewBinding(final MovesenseActivity target, View source) {
    this.target = target;

    View view;
    target.mMovesenseRecyclerView = Utils.findRequiredViewAsType(source, R.id.movesense_recyclerView, "field 'mMovesenseRecyclerView'", RecyclerView.class);
    view = Utils.findRequiredView(source, R.id.startScanningBtn, "field 'mStartScanningBtn' and method 'onStartViewClicked'");
    target.mStartScanningBtn = Utils.castView(view, R.id.startScanningBtn, "field 'mStartScanningBtn'", TextView.class);
    view2131624120 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onStartViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.stopScanningBtn, "field 'mStopScanningBtn' and method 'onStopViewClicked'");
    target.mStopScanningBtn = Utils.castView(view, R.id.stopScanningBtn, "field 'mStopScanningBtn'", TextView.class);
    view2131624121 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onStopViewClicked(p0);
      }
    });
    target.mMovesenseInfoTv = Utils.findRequiredViewAsType(source, R.id.movesense_infoTv, "field 'mMovesenseInfoTv'", TextView.class);
    target.mMovesenseProgressBar = Utils.findRequiredViewAsType(source, R.id.movesense_progressBar, "field 'mMovesenseProgressBar'", ProgressBar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MovesenseActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mMovesenseRecyclerView = null;
    target.mStartScanningBtn = null;
    target.mStopScanningBtn = null;
    target.mMovesenseInfoTv = null;
    target.mMovesenseProgressBar = null;

    view2131624120.setOnClickListener(null);
    view2131624120 = null;
    view2131624121.setOnClickListener(null);
    view2131624121 = null;
  }
}
