package com.pandora_escape.javier.pandora_escape;

import com.pandora_escape.javier.pandora_escape.admin_mode.AdminMode;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesDBHelper;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Fragment containing the application settings.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Interfaces

    interface OnAdminModeActionListener{
        void setAdminMode(boolean adminMode);
    }


    // Constants

    private static final String LOG_TAG = "SettingsFragment";


    // Variables

    private OnAdminModeActionListener mAdminModeActionListener;

    private Preference mTimerPreference;


    // Constructor

    public SettingsFragment() {}


    // Listeners

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
            mAdminModeActionListener.setAdminMode(true);
            return true;
        }
    };

    Preference.OnPreferenceChangeListener adminModeStateListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean adminMode = (Boolean) newValue;
            Log.d(LOG_TAG,"Listener newValue is " + adminMode);

            if(adminMode){
                mAdminModeActionListener.setAdminMode(true);
            } else {
                mAdminModeActionListener.setAdminMode(false);
            }

            return true;
        }
    };


    // Dynamic functions

    private void updateChosenLanguageSummary(){
        ListPreference preference = (ListPreference) findPreference(
                                        getString(R.string.pref_key_lang_choose));
        preference.setSummary(preference.getEntry());
    }


    public void updateTimerDisplay(){
        String formattedTime = "00:00";

        long timeLeft = AdminMode.getRemainingAdminModeTime();

        if(timeLeft>0) {
            formattedTime = new SimpleDateFormat("mm':'ss",Locale.getDefault())
                    .format(new Date(timeLeft));
        }

        if(mTimerPreference!=null) {
            mTimerPreference.setTitle(getString(R.string.pref_admin_mode_timer_title) + formattedTime);
        }
    }


    public void updateTimerDisplay(long timeLeft){
        String formattedTime = "00:00";

        if(timeLeft>0) {
            formattedTime = new SimpleDateFormat("mm':'ss",Locale.getDefault())
                    .format(new Date(timeLeft));
        }

        if(mTimerPreference!=null) {
            mTimerPreference.setTitle(getString(R.string.pref_admin_mode_timer_title) + formattedTime);
        }
    }


    // Lifecycle callbacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mAdminModeActionListener = (OnAdminModeActionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnAdminModeActionListener");
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Set the system default language name as summary
        Preference defaultLangPref = findPreference(getString(R.string.pref_key_lang_sysdef));
        defaultLangPref.setSummary(Locale.getDefault().getDisplayLanguage());

        // If the admin mode is specified as arguments, update the preference value
        boolean isAdmin = AdminMode.isAdmin();

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

            mTimerPreference = findPreference(getString(R.string.pref_key_admin_mode_timer));
        }

        // Set the entries, values and summary of language chooser
        updateChosenLanguageSummary();
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

/*        if(AdminMode.isAdmin()) {
            adminModeTickerStart();
        }*/
    }


    @Override
    public void onPause() {
        super.onPause();
        //adminModeTickerStop();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
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
