package com.movesense.mds.handwave.fyssa_app;

import android.app.Application;
import android.content.Context;

import com.movesense.mds.handwave.R;
import com.movesense.mds.handwave.bluetooth.MdsRx;
import com.movesense.mds.handwave.bluetooth.RxBle;
import com.movesense.mds.handwave.tool.MemoryTools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Application for making all initializations
 */
public class FyssaApp extends Application {

    private MemoryTools memoryTools;
    public static String deviceVersion = "1.0.0.HW";
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize RxBleWrapper
        RxBle.Instance.initialize(this);

        // Copy necessary configuration file to proper place
        copyRawResourceToFile(R.raw.kompostisettings, "KompostiSettings.xml");

        // Initialize MDS
        MdsRx.Instance.initialize(this);

        memoryTools = new MemoryTools(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    public MemoryTools getMemoryTools() {
        return memoryTools;
    }

    /**
     * Copy raw resource file to file.
     *
     * @param resourceId Resource id.
     * @param fileName   Target file name.
     */
    private void copyRawResourceToFile(int resourceId, String fileName) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = getResources().openRawResource(resourceId);
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not copy configuration file to: " + fileName);
        } finally {
            com.movesense.mds.handwave.Util.safeClose(out);
            com.movesense.mds.handwave.Util.safeClose(in);
        }
    }
}
