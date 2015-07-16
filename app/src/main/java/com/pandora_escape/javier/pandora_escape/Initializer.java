package com.pandora_escape.javier.pandora_escape;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.pandora_escape.javier.pandora_escape.message_db.Message;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesContract;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesDBHelper;


public class Initializer extends Activity {

    private static MessagesDBHelper sMessagesDBHelper;

    public void sendMessage(Message message){
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


    /**
     * Extract code from URI
     *
     * @param uri Input URI
     * @return Code as a String
     */
    @Nullable
    public static String extractCode(Uri uri){
        try {
            return uri.getQueryParameter(MessagesContract.COLUMN_NAME_CODE);
        }catch (Exception e){
            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sMessagesDBHelper = MessagesDBHelper.getInstance(this);
        sMessagesDBHelper.initialize(this,getString(R.string.locale));    // Populate the db

        Intent intent = getIntent();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                Message message = null;
                for (Parcelable rawMsg : rawMsgs) {
                    NdefRecord[] records = ((NdefMessage) rawMsg).getRecords();

                    for (NdefRecord record : records) {
                        String code = extractCode(record.toUri());
                        message = MessagesDBHelper.getMessage(code);
                        if(message!=null){ break; }
                    }
                    if(message!=null){ break; }
                }

                if(message!=null){
                    sMessagesDBHelper.addDiscoveredMessage(message.getCode());
                    sendMessage(message);
                }
            }

        }else{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        sMessagesDBHelper.close();
    }

}
