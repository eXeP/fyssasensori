package com.movesense.mds.handwave.app_using_mds_api.model;

import com.google.gson.annotations.SerializedName;

public class HandwaveConfigGson {

    @SerializedName("handwaveConfig")
    private final HandwaveConfig handwaveConfig;

    public HandwaveConfigGson(HandwaveConfig handwaveConfig) {
        this.handwaveConfig = handwaveConfig;
    }

    public static class HandwaveConfig {
        @SerializedName("time")
        private final int time;

        public HandwaveConfig(int time){
            this.time = time;
        }
    }


}
