package com.movesense.mds.handwave.app_using_mds_api.model;

import com.google.gson.annotations.SerializedName;

public class HandwaveResponse {
    @SerializedName("Body")
    private final String body;

    @SerializedName("Uri")
    private final String uri;

    @SerializedName("Method")
    private final String method;

    public HandwaveResponse(String body, String uri, String method){

        this.body = body;
        this.uri = uri;
        this.method = method;
    }

    public String getHandwave() {return this.body;}
    public String getHandwaveClean() {
        String s = getHandwave();
        int i = s.indexOf('.');
        if (i > 0) s = s.substring(0, i);
        return s;
    }
}
