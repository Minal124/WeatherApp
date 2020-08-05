package com.minal.weatherapp;

import android.app.Activity;
import android.content.SharedPreferences;

public class AppPreferences {

    private SharedPreferences prefs;

    private static final String CITY_PREF = "city";
    private static final String DEFAULT_CITY = "Ahmedabad";

    public AppPreferences(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getCity() {
        return prefs.getString(CITY_PREF, DEFAULT_CITY);
    }

    void setCity(String city) {
        prefs.edit().putString(CITY_PREF, city).commit();
    }
}