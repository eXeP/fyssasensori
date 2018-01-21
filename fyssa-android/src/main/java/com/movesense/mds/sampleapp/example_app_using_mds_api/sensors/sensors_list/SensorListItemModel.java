package com.movesense.mds.sampleapp.example_app_using_mds_api.sensors.sensors_list;


public class SensorListItemModel {

    private String name;
    private int image;

    public SensorListItemModel(String name, int image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }
}
