package com.movesense.mds.handwave.update_app;


import com.movesense.mds.handwave.bluetooth.MdsRx;

/**
 * Helper class for formatting Contract and Path
 */
public class FormatHelper {

    private FormatHelper() {}

    public static String formatContractToJson(String serial, String uri) {
        return "{\"Uri\": \"" + serial + "/" + uri + "\"}";
    }

    public static String pathFormatHelper(String serial, String path) {
        return MdsRx.SCHEME_PREFIX + serial + "/" + path;
    }
}
