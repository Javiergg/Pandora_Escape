
package com.pandora_escape.javier.pandora_escape.admin_mode;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pandora_escape.javier.pandora_escape.R;

/**
 * Created by Javier on 23/08/15.
 *
 * Extension of the Activity class to include the tools that handle the UI change that reflect
 * that the application is currently in Admin Mode. The admin mode state is checked every time
 * the activity becomes visible (onReasume) and by means of a broadcast receiver for changes
 * while the application is in the foreground.
 *
 * The broadcast receiver method is mainly used to return the activity UI back to User Mode when
 * the admin mode period expires. This is signaled by the Admin Mode Server with a broadcast
 * message that sends the updated user mode as an extra.
 */

public class AdminActivity extends Activity {

    // Constants

    /** Logging tag */
    private static final String LOG_TAG = "AdminActivity";


    // Variables

    /** Current user level for this activity in particular */
    private boolean mIsInAdminUI = false;

    /** Local pointer to the LocalBroadcastManager */
    private static LocalBroadcastManager sLocalBroadcastManager;

    /** Broadcast Receiver to handle the end of Admin Mode. Turns on and off the Admin Mode UI */
    private final BroadcastReceiver mAdminModeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getBooleanExtra(AdminMode.EXTRA_ADMIN_MODE,false)){
                turnOnAdminUI();
            }else{
                turnOffAdminUI();
            }
        }
    };


    /** Intent filter for Admin Mode broadcast messages */
    private final IntentFilter mAdminModeIntentFilter =
                        new IntentFilter(AdminMode.ADMIN_MODE_INTENT);



    // Dynamic Functions

    /** Turns on the Admin Mode UI in this application. This is done by changing the colour of
     * the action bar to blue and adding " - Admin" to the end of the title. It checks the local
     * admin mode status and only applies the changes when the local status needs updating */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void turnOnAdminUI(){
        // If the activity was in user mode, then the UI changes are applied
        if(!mIsInAdminUI) {
            Log.d(LOG_TAG, "Turning on admin mode UI");

            // Update the action bar by changing the colour and adding a suffix to the title
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(getDrawable(R.color.AdminActionBarColor));
                actionBar.setTitle(actionBar.getTitle() + getString(R.string.admin_mode_title_suffix));
            }
        }

        mIsInAdminUI = true;
    }


    /** Returns the application's UI back to normal by recreating it. It checks the local admin
     * mode status and only applies the changes when the local status needs updating */
    private void turnOffAdminUI(){
        // If the activity was in admin mode and finished
        if(mIsInAdminUI) {
            mIsInAdminUI = false;
            Log.d(LOG_TAG, "Turning off admin mode UI");

            // Recreate the activity so the theme changes are applied.
            this.recreate();
        }
        // Update the local variable
        mIsInAdminUI = false;
    }


    // Lifecycle Functions

    /** During creating, a local pointer to the LocalBroadcastManager is generated */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
    }


    /** Every time the activity regains visibility it checks the current Admin Mode status and
     * updates the UI according to the current status */
    @Override
    public void onResume() {
        super.onResume();

        // Updates the UI to match the current admin mode state
        if(AdminMode.isAdmin()){
            turnOnAdminUI();
        } else {
            turnOffAdminUI();
        }

        // Registers the broadcast receiver
        sLocalBroadcastManager.registerReceiver(mAdminModeBroadcastReceiver, mAdminModeIntentFilter);
    }


    /** When the activity is paused the broadcast receiver is unregistered so it is
     * only called when the activity is visible */
    @Override
    public void onPause(){
        super.onPause();

        // Unregisters the broadcast
        sLocalBroadcastManager.unregisterReceiver(mAdminModeBroadcastReceiver);
    }
}
