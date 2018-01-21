package com.pietu.fyssasensori.tool;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MemoryTools {
    public static final String DEFAULT_STRING = "AKSDJISAJDIOAS !UIOPHISFHIAOSIO_!@#232j";

    private static final String SHARED_PREF_NAME = "SHARED_PREFS";
    private static final String NAME_SHARED_PREF = "NAME_PREFS";
    private static final String SERIAL_SHARED_PREF = "SERIAL_PREFS";


    private Application app;
    private SharedPreferences sharedPref;

    public MemoryTools(Application app) {
        this.app = app;
        sharedPref = app.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveName(String name) {
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(NAME_SHARED_PREF, name);
        edit.commit();
    }

    public String getName() {
        return sharedPref.getString(NAME_SHARED_PREF, DEFAULT_STRING);
    }

    public void saveSerial(String name) {
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(SERIAL_SHARED_PREF, name);
        edit.commit();
    }

    public String getSerial() {
        return sharedPref.getString(SERIAL_SHARED_PREF, DEFAULT_STRING);
    }

}
