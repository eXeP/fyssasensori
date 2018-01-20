package com.movesense.mds.sampleapp.model;


import com.google.gson.annotations.SerializedName;

public class MdsResponse<T> {

    @SerializedName("Body")
    private T body;

    public T getBody() {
        return body;
    }
}
