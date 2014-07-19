package com.blubb.alubb.blubbbasics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.blubb.alubb.R;

/**
 * Activity to display the settings for the BlubbApplication.
 * Now it's just the pull interval of the MessagePullService but can be extended.
 * <p/>
 * Created by Benjamin Richter
 */
public class ActivitySettings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        getActionBar().hide();
    }

}
