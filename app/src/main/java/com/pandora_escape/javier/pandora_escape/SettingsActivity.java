package com.pandora_escape.javier.pandora_escape;


import android.os.Bundle;


public class SettingsActivity extends AdminActivity {

    public static final String ADMIN_LEVEL = "admin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment, settingsFragment)
                .commit();
    }

    @Override
    public void onResume(){
        super.onResume();

        // Avoid exiting Admin Mode while in the settings panel
        cancelAdminModeTimout();
    }

}
