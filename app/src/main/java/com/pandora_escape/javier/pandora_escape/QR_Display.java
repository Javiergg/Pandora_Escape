package com.pandora_escape.javier.pandora_escape;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.pandora_escape.javier.pandora_escape.admin_mode.AdminActivity;


public class QR_Display extends AdminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        // Locate the Title and Body views
        TextView titleView = (TextView) findViewById(R.id.titleTextView);
        TextView bodyView = (TextView) findViewById(R.id.bodyTextView);

        // Update their content to the values from the intent
        String title = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE_TITLE);
        String body = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE_BODY);

        titleView.setText(title);
        bodyView.setText(body);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_qr_display, menu);
        return true;
    }

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
