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
import android.view.Menu;
import android.view.MenuItem;


public class SortingHat extends Activity {

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

        if(!Message.messageList.contains(message)){
            Message.messageList.add(message);
        }

        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intentToMain)
                .addNextIntentWithParentStack(messageIntent)
                .startActivities();

        //startActivity(messageIntent);
        finish();
    }


    public void parsePandoraUri(Uri uri){
        if(uri==null) {
            return;
        }

        String code = uri.getQueryParameter(getString(R.string.pandora_uri_query_code));
        if(code!=null){
            sendMessage(new Message(code));
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sorting_hat);

        Message.initialize(getApplicationContext());

        Intent intent = getIntent();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {

                for (int i = 0; i < rawMsgs.length; i++) {

                    NdefRecord[] records = ((NdefMessage) rawMsgs[i]).getRecords();
                    for (int j = 0; j < records.length; j++) {
                        parsePandoraUri(records[j].toUri());
                    }
                }
            }
        }else{
            Intent nextIntent = new Intent(this,MainActivity.class);

            startActivity(nextIntent);
            finish();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sorting_hat, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
