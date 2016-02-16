package com.pandora_escape.javier.pandora_escape;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.pandora_escape.javier.pandora_escape.admin_mode.AdminModeService;
import com.pandora_escape.javier.pandora_escape.message_db.Message;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesContract;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesDBHelper;


public class Initializer extends Activity {

    public static final String NFC_URI_PATH_MESSAGE  = "/messages";
    public static final String NFC_URI_PATH_SETTINGS = "/settings";


    private static MessagesDBHelper sMessagesDBHelper;


    /**
     * Extract code from URI
     *
     * @param uri Input URI
     * @return Code as a String
     */
    @Nullable
    private static Message processMessageUri(Uri uri){
        try {
            // If it's a valid URI, generate the corresponding message
            String code = uri.getQueryParameter(MessagesContract.COLUMN_NAME_CODE);
            return MessagesDBHelper.getMessage(code);
        }catch (Exception e){
            // If the uri cannot be processed, return null
            return null;
        }
    }



    private String processSettingsUri(Uri uri){
        try {
            // If it's a valid URI, generate the corresponding intent
            return uri.getQueryParameter(
                    getString(R.string.pandora_uri_query_level));
        }catch (Exception e){
            // If the uri cannot be processed, return null
            return null;
        }
    }



    private void sendMessage(Message message){
        Intent messageIntent = new Intent(this,QR_Display.class);
        Intent intentToMain = new Intent(this,MainActivity.class);

        if(message == null){
            messageIntent.putExtra(MainActivity.EXTRA_MESSAGE_TITLE,
                    getString(R.string.scan_error_title));
            messageIntent.putExtra(MainActivity.EXTRA_MESSAGE_BODY,
                    getString(R.string.scan_error_body));
        } else {
            messageIntent.putExtra(MainActivity.EXTRA_MESSAGE_TITLE, message.getTitle());
            messageIntent.putExtra(MainActivity.EXTRA_MESSAGE_BODY, message.getBody());
        }

        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intentToMain)
                .addNextIntentWithParentStack(messageIntent)
                .startActivities();
        finish();
    }



    private void startSettingsInAdminMode(String level){
        // If the parsed value is Admin
        if(SettingsActivity.ADMIN_LEVEL.equals(level)) {
            // Set the admin mode settings option to true
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPreferences != null) {
                sharedPreferences.edit().putBoolean(getString(R.string.pref_key_admin_mode), true)
                        .apply();
            }
            // Start the admin mode timeout
            //AdminActivity.adminModeTimerStart();
            Intent startAdminModeIntent = new Intent(this,AdminModeService.class);
            startService(startAdminModeIntent);
        }

        Toast toast = Toast.makeText(this,R.string.admin_mode_toast,Toast.LENGTH_LONG);
        toast.show();


        Intent settingsIntent = new Intent(this,SettingsActivity.class);
        Intent intentToMain = new Intent(this,MainActivity.class);

        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intentToMain)
                .addNextIntentWithParentStack(settingsIntent)
                .startActivities();
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set preference values to default. Since false is used it only is set once.
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        PreferenceManager.setDefaultValues(this,R.xml.preferences_admin,false);

        sMessagesDBHelper = MessagesDBHelper.getInstance(this);    // Populate the db
        //sMessagesDBHelper.initialize(this,getString(R.string.locale));

        Intent intent = getIntent();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                boolean finished = false;
                Message message = null;
                String settingsLevel = null;
                for (Parcelable rawMsg : rawMsgs) {
                    NdefRecord[] records = ((NdefMessage) rawMsg).getRecords();

                    for (NdefRecord record : records) {
                        // Check the record has a valid URI path or skip to next
                        Uri uri = record.toUri();
                        String path = uri.getPath();
                        if(path==null){ continue; }
                        // Depending on the path perform an action or another.
                        switch (path){
                            case NFC_URI_PATH_MESSAGE:
                                // Process the message URI
                                message = processMessageUri(uri);
                                // and if it's valid, exit the loop to send the message
                                finished = message!=null;
                                break;
                            case NFC_URI_PATH_SETTINGS:
                                // Process the settings URI
                                settingsLevel = processSettingsUri(uri);
                                // and if it's valid, exit the loop to start the settings
                                finished = settingsLevel!=null;
                                break;
                        }
                        if(finished){ break; }
                    }
                    if(finished){ break; }
                }

                if(message!=null){
                    sMessagesDBHelper.addDiscoveredMessage(message.getCode());
                    sendMessage(message);
                }else if(settingsLevel!=null){
                    startSettingsInAdminMode(settingsLevel);
                }else{
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();

        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        sMessagesDBHelper.close();
    }

}
