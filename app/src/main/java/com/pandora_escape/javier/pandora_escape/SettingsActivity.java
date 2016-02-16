package com.pandora_escape.javier.pandora_escape;

import com.pandora_escape.javier.pandora_escape.admin_mode.AdminActivity;
import com.pandora_escape.javier.pandora_escape.admin_mode.AdminMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;


public class SettingsActivity extends AdminActivity
                                implements SettingsFragment.OnAdminModeActionListener{

    public static final String ADMIN_LEVEL = "admin";


    private SettingsFragment mSettingsFragment;

    private static LocalBroadcastManager sLocalBroadcastManager;

    private final BroadcastReceiver mAdminCountdownBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mSettingsFragment!=null) {
                long secondsLeft = intent.getLongExtra(AdminMode.EXTRA_ADMIN_COUNTDOWN,0);
                mSettingsFragment.updateTimerDisplay(secondsLeft);
            }
        }
    };

    private final IntentFilter mAdminCountdownIntentFilter =
            new IntentFilter(AdminMode.ADMIN_COUNTDOWN_INTENT);


    @Override
    public void setAdminMode(boolean adminMode) {
        AdminMode.setAdminMode(getApplicationContext(), adminMode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        // Display the fragment as the main content.
        mSettingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment, mSettingsFragment)
                .commit();
    }


    @Override
    public void onResume() {
        super.onResume();

        sLocalBroadcastManager.registerReceiver(
                mAdminCountdownBroadcastReceiver, mAdminCountdownIntentFilter);
    }


    @Override
    public void onPause() {
        super.onPause();

        sLocalBroadcastManager.unregisterReceiver(mAdminCountdownBroadcastReceiver);
    }
}
