package com.movesense.mds.handwave.app_using_mds_api.model;

import com.google.gson.annotations.SerializedName;

public class HandwaveResponse {

    @SerializedName("Content")
    public final Content content;

    public HandwaveResponse(Content content) {
        this.content = content;
    }

    public static class Content {
        @SerializedName("Body")
        public final String body;

        @SerializedName("Uri")
        public final String uri;

        @SerializedName("Method")
        public final String method;

        public Content(String body, String uri, String method) {
            this.body = body;
            this.uri = uri;
            this.method = method;

        }
        public String getHandwave() {return this.body;}
    }

}
