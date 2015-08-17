package com.pandora_escape.javier.pandora_escape;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;

import com.pandora_escape.javier.pandora_escape.message_db.MessagesDBHelper;

import java.util.Locale;


/**
 * Fragment containing the application settings.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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


    private void updateChosenLanguageSummary(){
        ListPreference preference = (ListPreference) findPreference(
                                        getString(R.string.pref_key_lang_choose));
        preference.setSummary(preference.getEntry());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Set the system default language name as summary
        Preference defaulLangPref = findPreference(getString(R.string.pref_key_lang_sysdef));
        defaulLangPref.setSummary(Locale.getDefault().getDisplayLanguage());

        // If the admin mode is specified as arguments, update the preference value
        boolean adminMode = getPreferenceManager().getSharedPreferences().getBoolean(
                                getString(R.string.pref_key_admin_mode), false);
        Bundle args = getArguments();
        if(args!=null) {
            adminMode = args.getBoolean(SettingsActivity.EXTRA_ADMIN_LEVEL, false);
            getPreferenceManager().getSharedPreferences().edit().putBoolean(
                    getString(R.string.pref_key_admin_mode), adminMode).apply();
        }

        // If admin level is active, activate additional message preferences
        if(adminMode){
            addPreferencesFromResource(R.xml.preferences_admin);
            // Activate listeners
            findPreference(getString(R.string.pref_key_msgs_populate))
                    .setOnPreferenceClickListener(populateListener);
            findPreference(getString(R.string.pref_key_msgs_delete))
                    .setOnPreferenceClickListener(deleteListener);
        }

        // Set the entries, values and summary of language chooser
        updateChosenLanguageSummary();
    }



    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
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
