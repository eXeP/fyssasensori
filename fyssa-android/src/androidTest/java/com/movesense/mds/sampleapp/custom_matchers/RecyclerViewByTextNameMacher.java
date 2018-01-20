package com.movesense.mds.sampleapp.custom_matchers;


import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.ScannedDevicesAdapter;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseAdapter;

import org.hamcrest.Matcher;

public class RecyclerViewByTextNameMacher {

    /**
     * Custom matcher for RecyclerView text
     *
     * @param text
     * @return
     */
    public static Matcher<RecyclerView.ViewHolder> withHolderTimeView(final String text) {
        return new BoundedMatcher<RecyclerView.ViewHolder, MovesenseAdapter.ViewHolder>(MovesenseAdapter.ViewHolder.class) {

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("No ViewHolder found with text: " + text);
            }

            @Override
            protected boolean matchesSafely(MovesenseAdapter.ViewHolder viewHolder) {
                TextView timeViewText = (TextView) viewHolder.itemView.findViewById(R.id.movesense_name);
                if (timeViewText == null) {
                    return false;
                }
                return timeViewText.getText().toString().equalsIgnoreCase(text);
            }

        };
    }


    /**
     * Custom matcher for RecyclerView text
     *
     * @param text
     * @return
     */
    public static Matcher<RecyclerView.ViewHolder> withDfuHolderTimeView(final String text) {
        return new BoundedMatcher<RecyclerView.ViewHolder, ScannedDevicesAdapter.DeviceViewHolder>(ScannedDevicesAdapter.DeviceViewHolder.class) {

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("No ViewHolder found with text: " + text);
            }

            @Override
            protected boolean matchesSafely(ScannedDevicesAdapter.DeviceViewHolder viewHolder) {
                TextView timeViewText = (TextView) viewHolder.itemView.findViewById(R.id.movesense_name);
                if (timeViewText == null) {
                    return false;
                }
                return timeViewText.getText().toString().equalsIgnoreCase(text);
            }

        };
    }
}
