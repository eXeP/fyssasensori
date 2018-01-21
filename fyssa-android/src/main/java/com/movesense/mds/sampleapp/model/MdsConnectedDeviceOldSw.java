package com.movesense.mds.sampleapp.model;


import com.google.gson.annotations.SerializedName;

public class MdsConnectedDeviceOldSw extends MdsConnectedDevice {

    @SerializedName("DeviceInfo")
    private MdsDeviceInfoOldSw deviceInfo;

    public MdsDeviceInfoOldSw getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(MdsDeviceInfoOldSw deviceInfo) {
        this.deviceInfo = deviceInfo;

    }
}
