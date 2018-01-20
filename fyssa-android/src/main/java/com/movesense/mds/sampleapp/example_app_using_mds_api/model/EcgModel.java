package com.movesense.mds.sampleapp.example_app_using_mds_api.model;


import com.google.gson.annotations.SerializedName;

public class EcgModel {

    @SerializedName("Body")
    public final Body mBody;

    public EcgModel(Body body) {
        mBody = body;
    }

    public class Body {

        @SerializedName("Samples")
        public final int[] data;

        public Body(int[] data) {
            this.data = data;
        }

        public int[] getData() {
            return data;
        }
    }

    public Body getBody() {
        return mBody;
    }
}
