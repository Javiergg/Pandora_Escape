package com.pandora_escape.javier.pandora_escape;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.util.Log;

import com.pandora_escape.javier.pandora_escape.message_db.MessagesDBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Fragment containing the application settings.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "SettingsFragment";

    private CountDownTimer mAdminModeTicker;


    public SettingsFragment() {}


    Preference.OnPreferenceClickListener populateListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            MessagesDBHelper dbHelper = MessagesDBHelper.getInstance(getActivity());
            dbHelper.addAllMessages();
            return true;
        }
    };

    Preference.OnPreferenceClickListener deleteListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            MessagesDBHelper dbHelper = MessagesDBHelper.getInstance(getActivity());
            dbHelper.removeAllMessages();
            return true;
        }
    };

    Preference.OnPreferenceClickListener adminModeClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            AdminActivity.adminModeTimerStart();
            adminTickerStart(AdminActivity.getRemainingAdminModeTime());
            return true;
        }
    };

    Preference.OnPreferenceChangeListener adminModeStateListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean adminMode = (Boolean) newValue;
            Log.d(LOG_TAG,"Listener newValue is " + adminMode);

            if(adminMode){
                AdminActivity.adminModeTimerStart();
                adminTickerStart(AdminActivity.getRemainingAdminModeTime());
            } else {
                AdminActivity.adminModeTimerStop();
                adminTickerStop();
            }

            return true;
        }
    };


    private void updateChosenLanguageSummary(){
        ListPreference preference = (ListPreference) findPreference(
                                        getString(R.string.pref_key_lang_choose));
        preference.setSummary(preference.getEntry());
    }


    private void adminTickerStart(long timeLeft){

        if(AdminActivity.isAdmin()){
            if(mAdminModeTicker!=null){
                mAdminModeTicker.cancel();
            }

            mAdminModeTicker = new CountDownTimer(timeLeft,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimerDisplay(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    updateTimerDisplay(0);
                    SwitchPreference switchPreference = (SwitchPreference) getPreferenceManager()
                            .findPreference(getString(R.string.pref_key_admin_mode));
                    switchPreference.setChecked(false);
                }
            }.start();
        }

        updateTimerDisplay(timeLeft);
    }


    private void adminTickerStop(){
        if(mAdminModeTicker!=null){
            mAdminModeTicker.cancel();
        }

        updateTimerDisplay(0);
    }


    private void updateTimerDisplay(long timeLeft){
        String formattedTime = "00:00";

        if(timeLeft>0) {
            formattedTime = new SimpleDateFormat("mm':'ss",Locale.getDefault())
                                .format(new Date(timeLeft));
        }

        Preference timer = findPreference(getString(R.string.pref_key_admin_mode_timer));
        if(timer!=null) {
            timer.setTitle(getString(R.string.pref_admin_mode_timer_title) + formattedTime);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Set the system default language name as summary
        Preference defaultLangPref = findPreference(getString(R.string.pref_key_lang_sysdef));
        defaultLangPref.setSummary(Locale.getDefault().getDisplayLanguage());

        // If the admin mode is specified as arguments, update the preference value
        Log.d(LOG_TAG, "Admin time left: " + (AdminActivity.getRemainingAdminModeTime()/1000));
        boolean isAdmin = AdminActivity.isAdmin();

        // If admin level is active, activate additional message preferences
        if(isAdmin){
            addPreferencesFromResource(R.xml.preferences_admin);
            // Activate listeners
            findPreference(getString(R.string.pref_key_msgs_populate))
                    .setOnPreferenceClickListener(populateListener);
            findPreference(getString(R.string.pref_key_msgs_delete))
                    .setOnPreferenceClickListener(deleteListener);
            findPreference(getString(R.string.pref_key_admin_mode))
                    .setOnPreferenceChangeListener(adminModeStateListener);
            findPreference(getString(R.string.pref_key_admin_mode_timer))
                    .setOnPreferenceClickListener(adminModeClickListener);
        }

        // Set the entries, values and summary of language chooser
        updateChosenLanguageSummary();
    }



    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        if(AdminActivity.isAdmin()) {
            adminTickerStart(AdminActivity.getRemainingAdminModeTime());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        adminTickerStop();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key==null){ return; }

        // If the chosen language has changed, update the summary
        if(key.equals(getString(R.string.pref_key_lang_choose))){
            updateChosenLanguageSummary();
        }
    }
}
