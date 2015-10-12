package com.pandora_escape.javier.pandora_escape;


import android.os.Bundle;


public class SettingsActivity extends AdminActivity
        implements SettingsFragment.OnAdminModeActionListener {

    public static final String ADMIN_LEVEL = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        SettingsFragment mSettingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment, mSettingsFragment)
                .commit();
    }

}
