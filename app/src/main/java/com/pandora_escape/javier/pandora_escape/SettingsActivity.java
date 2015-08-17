package com.pandora_escape.javier.pandora_escape;

import android.app.Activity;
import android.os.Bundle;


public class SettingsActivity extends Activity {

    public static final String EXTRA_ADMIN_LEVEL = "com.pandora_escape.javier.pandora_escape.ADMIN_LEVEL";
    public static final String EXTRA_SETTINGS_LEVEL = "com.pandora_escape.javier.pandora_escape.SETTINGS_LEVEL";
    public static final String ADMIN_LEVEL = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsFragment settingsFragment = new SettingsFragment();

        // Get the admin level from the intent
        String level = getIntent().getStringExtra(EXTRA_SETTINGS_LEVEL);
        //If it is admin level, add the admin mode extra as arguments to the fragment
        if(ADMIN_LEVEL.equals(level)) {
            Bundle args = new Bundle();
            args.putBoolean(EXTRA_ADMIN_LEVEL, true);
            settingsFragment.setArguments(args);
        } // If not, add no extras to keep the previous level

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment, settingsFragment)
                .commit();
    }

}
