package com.movesense.mds.sampleapp.example_app_using_mds_api.movesense;


public class MovesenseModel {

    private String serial;
    private String address;
    private String rssi;

    public MovesenseModel(String serial, String address, String rssi) {
        this.serial = serial;
        this.address = address;
        this.rssi = rssi;
    }

    public String getSerial() {
        return serial;
    }

    public String getAddress() {
        return address;
    }

    public String getRssi() {
        return rssi;
    }
}
