package com.pandora_escape.javier.pandora_escape;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;


public class Initializer extends Activity {

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

            intentToMain.putExtra(MainActivity.EXTRA_ADD_MESSAGE,message.index);
        }

        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intentToMain)
                .addNextIntentWithParentStack(messageIntent)
                .startActivities();
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Message.initialize(getApplicationContext());

        Intent intent = getIntent();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                Message message = null;
                for (Parcelable rawMsg : rawMsgs) {
                    NdefRecord[] records = ((NdefMessage) rawMsg).getRecords();

                    for (NdefRecord record : records) {
                        message = Message.parsePandoraUri(record.toUri());
                        if(message!=null){ break; }
                    }
                    if(message!=null){ break; }
                }

                if(message!=null){
                    sendMessage(message);
                }
            }

        }else{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

}
