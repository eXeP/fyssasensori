package com.movesense.mds.sampleapp.example_app_using_mds_api.mainView;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.custom_matchers.RecyclerViewByTextNameMacher;
import com.movesense.mds.sampleapp.example_app_using_mds_api.movesense.MovesenseActivity;
import com.movesense.mds.sampleapp.example_app_using_mds_api.sensors.sensors_list.SensorListActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MovesenseConnectionTest {

    @Rule
    public ActivityTestRule<MainViewActivity> mMainViewActivityActivityTestRule = new ActivityTestRule<>(MainViewActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);


    // Device to test
    private final static String MOVESENSE_NAME_TEST = "Movesense ECKICFD7F724";

    private static final String TAG = MovesenseConnectionTest.class.getSimpleName();
    private CountingIdlingResource mConnectCountingIdlingResource;
    private CountingIdlingResource mDisconnectCountingIdlingResource;

    @Before
    public void setUp() {

        // Run Bluetooth if disabled
        BluetoothManager bluetoothManager = (BluetoothManager) mMainViewActivityActivityTestRule.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    @After
    public void afterTest() {
        try {
            Log.e(TAG, "TEST disconnect sleep: ");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void connectionTestLogic() {
        // Register idling resource for connection
        mConnectCountingIdlingResource = MovesenseActivity.getmConnectCountingIdlingResource();
        Espresso.registerIdlingResources(mConnectCountingIdlingResource);

        // Register idling resource for disconnect
        mDisconnectCountingIdlingResource = SensorListActivity.getmDisconnectCountingIdlingResource();
        Espresso.registerIdlingResources(mDisconnectCountingIdlingResource);

        // Click on Movesense item in main view
        onView(allOf(withId(R.id.mainView_movesense_Ll), isDisplayed())).perform(click());

        // Click start scanning button - start scanning BLE devices
        onView(withId(R.id.startScanningBtn)).perform(click());

        // Wait for scanning devices
        try {
            Log.e(TAG, "TEST scanning sleep: ");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Find in RecyclerView specific Movesense name and click for connection
        Matcher<RecyclerView.ViewHolder> matcher = RecyclerViewByTextNameMacher.withHolderTimeView(MOVESENSE_NAME_TEST);
        onView((withId(R.id.movesense_recyclerView))).perform(scrollToHolder(matcher), actionOnHolderItem(matcher, click()));

        // Backpress will show dialog with disconnect question
        pressBack();

        // Press YES for disconnect with Movesense on dialog popup
        onView(withId(android.R.id.button1)).perform(click());

        // Click on Movesense item in main view to be sure disconnect was done
        onView(allOf(withId(R.id.mainView_movesense_Ll), isDisplayed())).perform(click());

        if (mConnectCountingIdlingResource != null) {
            Espresso.unregisterIdlingResources(mConnectCountingIdlingResource);
        }

        if (mDisconnectCountingIdlingResource != null) {
            Espresso.unregisterIdlingResources(mDisconnectCountingIdlingResource);
        }
    }

    @Test
    public void MovesenseConnectionTest() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest2() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest3() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest4() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest5() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest6() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest7() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest8() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest9() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest10() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest11() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest12() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest13() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest14() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest15() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest16() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest17() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest18() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest19() {
        connectionTestLogic();
    }

    @Test
    public void MovesenseConnectionTest20() {
        connectionTestLogic();
    }
}
