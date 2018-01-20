package com.movesense.mds.sampleapp.example_app_using_mds_api;


/**
 * TODO: Add a class header comment!
 */

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);
}
