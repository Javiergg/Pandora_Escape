package com.pandora_escape.javier.pandora_escape;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.pandora_escape.javier.pandora_escape.message_db.Message;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesContract;
import com.pandora_escape.javier.pandora_escape.message_db.MessagesDBHelper;


public class MainActivity extends Activity {

    public static final String EXTRA_MESSAGE_TITLE = "com.pandora_escape.javier.pandora_escape.MESSAGE_TITLE";
    public static final String EXTRA_MESSAGE_BODY = "com.pandora_escape.javier.pandora_escape.MESSAGE_BODY";

    public static final String QR_SCAN_ADDRESS = "com.google.zxing.client.android.SCAN";
    public static final int QR_SCAN_REQUEST_CODE = 1;

    private Cursor mCursor;
    private static SimpleCursorAdapter mMessageCursorAdapter;

    private static MessagesDBHelper sMessagesDBHelper;

    // Functions
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

    public void sendMessage(Message message){

        Intent MessageIntent = new Intent(this,QR_Display.class);

        if(message==null || message.getTitle()==null){
            MessageIntent.putExtra(EXTRA_MESSAGE_TITLE, getString(R.string.scan_error_title));
            MessageIntent.putExtra(EXTRA_MESSAGE_BODY, getString(R.string.scan_error_body));
        } else {
            MessageIntent.putExtra(EXTRA_MESSAGE_TITLE, message.getTitle());
            MessageIntent.putExtra(EXTRA_MESSAGE_BODY, message.getBody());
        }

        startActivity(MessageIntent);
    }


    /**
     * Displays a message on screen by sending an intent to the QR_Display activity
     *
     * @param title Title of the message
     * @param body Body of the message
     */
    public void displayMessage(String title, String body){
        Intent MessageIntent = new Intent(this,QR_Display.class);

        MessageIntent.putExtra(EXTRA_MESSAGE_TITLE, title);
        MessageIntent.putExtra(EXTRA_MESSAGE_BODY, body);

        startActivity(MessageIntent);
    }


    // Activity Lifecycle
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(
                        "preferences_admin",MODE_PRIVATE);
        boolean adminMode = sharedPreferences.getBoolean(
                                getString(R.string.pref_key_admin_mode), false);
        if(adminMode){
            ActionBar actionBar = getActionBar();
            if(actionBar!=null) {
                actionBar.setBackgroundDrawable(getDrawable(android.R.color.holo_blue_dark));
                actionBar.setTitle(getString(R.string.app_name) + " - Admin");
            }
        }

        sMessagesDBHelper = MessagesDBHelper.getInstance(this);

        // Build ListView
        ListView messageList = (ListView) findViewById(R.id.clueListView);
        //messageArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,messages_raw);
        mCursor = sMessagesDBHelper.getDiscoveredMessages();
        mMessageCursorAdapter = new SimpleCursorAdapter(this,
                                    android.R.layout.simple_list_item_1,
                                    mCursor,
                                    new String[]{MessagesContract.COLUMN_NAME_TITLE},
                                    new int[]{android.R.id.text1},
                                    0);
        messageList.setAdapter(mMessageCursorAdapter);

        messageList.setOnItemClickListener(mMessageClickedHandler);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        // Liberate resources
        mCursor.close();
        sMessagesDBHelper.close();
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

        Cursor cursor;

        switch (id) {
            case R.id.populate:
                // Database
                sMessagesDBHelper.addAllMessages();

                cursor = sMessagesDBHelper.getDiscoveredMessages();
                mMessageCursorAdapter.changeCursor(cursor);
                mMessageCursorAdapter.notifyDataSetChanged();
                mCursor = cursor;
                break;
            case R.id.delete_msgs:
                // Database
                sMessagesDBHelper.removeAllMessages();

                cursor = sMessagesDBHelper.getDiscoveredMessages();
                mMessageCursorAdapter.changeCursor(cursor);
                mMessageCursorAdapter.notifyDataSetChanged();
                mCursor = cursor;
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
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

                sMessagesDBHelper.addDiscoveredMessage(contents);

                Cursor cursor = sMessagesDBHelper.getDiscoveredMessages();
                mMessageCursorAdapter.changeCursor(cursor);
                mMessageCursorAdapter.notifyDataSetChanged();
                mCursor = cursor;

                Message message = MessagesDBHelper.getMessage(contents);

                sendMessage(message);
            }
/*            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }*/
        }
    }


    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            mCursor.moveToPosition(position);

            int titleIndex = mCursor.getColumnIndex(MessagesContract.MessagesAll.COLUMN_NAME_TITLE);
            int bodyIndex = mCursor.getColumnIndex(MessagesContract.MessagesAll.COLUMN_NAME_BODY);

            displayMessage(mCursor.getString(titleIndex),mCursor.getString(bodyIndex));
        }
    };

}


