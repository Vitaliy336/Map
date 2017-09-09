package com.example.vitaliy.map.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.vitaliy.map.R;

/**
 * Created by Vitaliy on 9/9/2017.
 */

public class PrefsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
