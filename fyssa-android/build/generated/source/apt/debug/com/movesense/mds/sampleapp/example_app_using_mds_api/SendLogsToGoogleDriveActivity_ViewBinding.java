// Generated code from Butter Knife. Do not modify!
package com.movesense.mds.sampleapp.example_app_using_mds_api;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.mds.sampleapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SendLogsToGoogleDriveActivity_ViewBinding implements Unbinder {
  private SendLogsToGoogleDriveActivity target;

  private View view2131624185;

  @UiThread
  public SendLogsToGoogleDriveActivity_ViewBinding(SendLogsToGoogleDriveActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SendLogsToGoogleDriveActivity_ViewBinding(final SendLogsToGoogleDriveActivity target,
      View source) {
    this.target = target;

    View view;
    target.resultTextView = Utils.findRequiredViewAsType(source, R.id.resultTextView, "field 'resultTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.logsFileListView, "field 'logsFileListView' and method 'onItemClick'");
    target.logsFileListView = Utils.castView(view, R.id.logsFileListView, "field 'logsFileListView'", ListView.class);
    view2131624185 = view;
    ((AdapterView<?>) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> p0, View p1, int p2, long p3) {
        target.onItemClick(p0, p1, p2, p3);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    SendLogsToGoogleDriveActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.resultTextView = null;
    target.logsFileListView = null;

    ((AdapterView<?>) view2131624185).setOnItemClickListener(null);
    view2131624185 = null;
  }
}
