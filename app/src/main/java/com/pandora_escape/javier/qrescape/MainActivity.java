package com.pandora_escape.javier.qrescape;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;


public class MainActivity extends Activity {

    public static final String MESSAGE_INSTANCE_KEY = "messages";

    public static final String EXTRA_MESSAGE_TITLE = "com.pandora_escape.javier.qrescape.MESSAGE_TITLE";
    public static final String EXTRA_MESSAGE_BODY = "com.pandora_escape.javier.qrescape.MESSAGE_BODY";

    public static final String QR_SCAN_ADDRESS = "com.google.zxing.client.android.SCAN";
    public static final int QR_SCAN_REQUEST_CODE = 1;


    private static ArrayList<Clue> messages;
    private static ArrayAdapter<Clue> messageArrayAdapter;

    private static SharedPreferences discoveredClues;
    private static String DISCOVERED_CLUES = "com.pandora_escape.javier.qrescape.DISCOVERED_CLUES";
    private static String DISCOVERED_CLUES_AMOUNT = "D_C_total";
    private static String DISCOVERED_CLUES_TEMPLATE = "D_C_possition_";


    // Functions
    protected void saveDiscoveredClues(){
        SharedPreferences.Editor editor = discoveredClues.edit();

        int total = messages.size();
        editor.putInt(DISCOVERED_CLUES_AMOUNT, total);
        for(int i=0;i<total;i++){
            int index = messages.get(i).index;
            editor.putInt(DISCOVERED_CLUES_TEMPLATE+i,index);
        }

        editor.apply();
    }

    protected void loadDiscoveredClues(){
        messages = new ArrayList<>();

        int total = discoveredClues.getInt(DISCOVERED_CLUES_AMOUNT,0);
        for(int i=0;i<total;i++){
            int index = discoveredClues.getInt(DISCOVERED_CLUES_TEMPLATE+i,-1);
            if(index>=0) {
                messages.add(new Clue(index));
            }
        }

        if(messageArrayAdapter!=null){
            messageArrayAdapter.notifyDataSetChanged();
        }
    }


    public void scanQR(View view){
        try {
            Intent intent = new Intent(QR_SCAN_ADDRESS);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, QR_SCAN_REQUEST_CODE);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id="+QR_SCAN_ADDRESS);
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }


    public void parsePandoraUri(Uri uri){
        if(uri==null) {
            return;
        }

        String code = uri.getQueryParameter(getString(R.string.pandora_uri_query_code));
        if(code!=null){
            sendMessage(new Clue(code));
        }
    }


    public void sendMessage(Clue clue){

        Intent MessageIntent = new Intent(this,QR_Display.class);

        if(clue ==null){
            MessageIntent.putExtra(EXTRA_MESSAGE_TITLE, getString(R.string.scan_error_title));
            MessageIntent.putExtra(EXTRA_MESSAGE_BODY, getString(R.string.scan_error_body));
        } else {
            MessageIntent.putExtra(EXTRA_MESSAGE_TITLE, clue.getTitle());
            MessageIntent.putExtra(EXTRA_MESSAGE_BODY, clue.getBody());
        }

        if(!messages.contains(clue)){
            messages.add(clue);
            messageArrayAdapter.notifyDataSetChanged();
        }

        startActivity(MessageIntent);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discoveredClues = getSharedPreferences(DISCOVERED_CLUES, Context.MODE_PRIVATE);

        if(savedInstanceState==null||!savedInstanceState.containsKey(MESSAGE_INSTANCE_KEY)) {
            loadDiscoveredClues();
        }else{
            messages = savedInstanceState.getParcelableArrayList(MESSAGE_INSTANCE_KEY);
        }

        if(messages==null) { messages = new ArrayList<>(); }
        Clue.initialize(this);

        // Build ListView
        ListView messageList = (ListView) findViewById(R.id.clueListView);
        messageArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,messages);
        messageList.setAdapter(messageArrayAdapter);

        messageList.setOnItemClickListener(mMessageClickedHandler);

    }


    @Override
    public void onResume() {
        super.onResume();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {

            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {

                for (int i = 0; i < rawMsgs.length; i++) {

                    NdefRecord[] records = ((NdefMessage) rawMsgs[i]).getRecords();
                    for (int j = 0; j < records.length; j++) {
                        parsePandoraUri(records[j].toUri());
                    }
                }
            }
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveDiscoveredClues();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.populate:
                ArrayList<Clue> allClues = Clue.getAllClues();
                messages.clear();
                for (int i = 0; i < allClues.size(); i++) {
                    messages.add(allClues.get(i));
                }
                messageArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.delete_msgs:
                messages.clear();
                messageArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.action_settings:
                break;
        }

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_SCAN_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");

                Clue clue = new Clue(contents);

                sendMessage(clue);
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }


    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {

            Clue clue = (Clue) parent.getAdapter().getItem(position);
            sendMessage(clue);
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(MESSAGE_INSTANCE_KEY,messages);
    }

}


