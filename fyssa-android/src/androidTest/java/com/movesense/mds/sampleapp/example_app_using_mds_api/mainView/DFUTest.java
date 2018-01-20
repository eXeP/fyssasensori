package com.movesense.mds.sampleapp.example_app_using_mds_api.mainView;


import android.Manifest;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;

import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.custom_matchers.RecyclerViewByTextNameMacher;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DFUTest {

    @Rule
    public ActivityTestRule<MainViewActivity> mActivityTestRule = new ActivityTestRule<>(MainViewActivity.class);
    private UiDevice mUiDevice;

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);

    private final static String DFU_FILE_NAME = "Movesense-accelerometer-dfu.zip";
    private final static String DFU_MOVESENSE_DEVICE_NAME = "Movesense ECKICFD7F724";
    private CountingIdlingResource mConnectingIdlingResource;
    private CountingIdlingResource mDfuUploadIdlingResource;

    @Before
    public void setUp() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

//        mConnectingIdlingResource = DfuPresenter.getConnectionIdlingResource();
//        Espresso.registerIdlingResources(mConnectingIdlingResource);
//
//        mDfuUploadIdlingResource = DfuPresenter.getmDfuUploadIdlingResource();
//        Espresso.registerIdlingResources(mDfuUploadIdlingResource);

        IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.MINUTES);
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES);
    }


    @After
    public void cleanUp() {

        if (mConnectingIdlingResource != null) {
            Espresso.unregisterIdlingResources(mConnectingIdlingResource);
        }

        if (mDfuUploadIdlingResource != null) {
            Espresso.unregisterIdlingResources(mDfuUploadIdlingResource);
        }
    }

    private void dfuTestLogic() throws UiObjectNotFoundException {
        // Click on DFU from main view
        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.mainView_dfu_Ll), isDisplayed()));
        linearLayout.perform(click());

        // Click on fileSelection container
        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.dfu_selectedFile_containerLl), isDisplayed()));
        linearLayout2.perform(click());

        // Click on DrawerIcon


        // Select file by UIAutomator
        UiScrollable fileList = new UiScrollable(new UiSelector().scrollable(true));
        fileList.scrollTextIntoView(DFU_FILE_NAME);
        mUiDevice.findObject(By.text(DFU_FILE_NAME)).click();

        // Click on deviceSelection container
        ViewInteraction linearLayout3 = onView(
                allOf(withId(R.id.dfu_selectedDevice_containerLl), isDisplayed()));
        linearLayout3.perform(click());

        // Wait for scan results
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Select device by name using custom matcher
        Matcher<RecyclerView.ViewHolder> matcher = RecyclerViewByTextNameMacher.withDfuHolderTimeView(DFU_MOVESENSE_DEVICE_NAME);
        onView((withId(R.id.device_list))).perform(scrollToHolder(matcher), actionOnHolderItem(matcher, click()));

        // Click on Proceed button
        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.dfu_startUpload_btn), withText("Proceed"), isDisplayed()));
        appCompatTextView.perform(click());

        // Click Yes, Update on popup dialog for confirmation
        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("Yes, Update")));
        appCompatButton.perform(click());

    }

    @Test
    public void dFUTest1() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest2() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest3() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest4() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest5() throws UiObjectNotFoundException {
        dfuTestLogic();
    }
}
