package com.movesense.mds.handwave.app_using_mds_api.model;

import com.google.gson.annotations.SerializedName;

public class HandwaveGetResponse {
    @SerializedName("Content")
    private final String content;


    public HandwaveGetResponse(String content){

        this.content = content;
    }

    public String getHandwave() {return this.content;}
    public String getHandwaveClean() {
        String s = getHandwave();
        int i = s.indexOf('.');
        if (i > 0) s = s.substring(0, i);
        return s;
    }
}
