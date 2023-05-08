package com.example.yourney;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Sesion {

    private static SharedPreferences prefs;

    public Sesion(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setUsername(String username) {
        prefs.edit().putString("username", username).commit();
    }

    public String getUsername() {
        return prefs.getString("username","");
    }

    public void deleteUsername() {
        prefs.edit().remove("username").commit();
    }
}
