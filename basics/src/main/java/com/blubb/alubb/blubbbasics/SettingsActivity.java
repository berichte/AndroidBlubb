package com.blubb.alubb.blubbbasics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.blubb.alubb.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        getActionBar().hide();
    }

}
