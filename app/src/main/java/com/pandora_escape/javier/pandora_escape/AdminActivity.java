
package com.pandora_escape.javier.pandora_escape;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import android.os.Handler;

/**
 *
 * Created by Javier on 23/08/15.
 */

public class AdminActivity extends Activity {

    // Constants

    /** Logging tag */
    private static final String LOG_TAG = "AdminActivity";

    /** Amount of time the admin mode is active */
    //public static final long ADMIN_TIMEOUT = 2*60*1000; // 2 minutes in milliseconds
    public static final long ADMIN_TIMEOUT = 10*1000; // 15 secs in millis for testing

    public static final String ADMIN_MODE_INTENT = "com.pandora_escape.javier.pandora_escape.admin_mode_status_change";


    // Variables

    private static LocalBroadcastManager sLocalBroadcastManager;

    /** Time in millis when the admin mode ends */
    private static long sAdminExpireTime = 0;

    /** Current user level (User = false, Admin = true) */
    private boolean mLastAdminMode = false;


    protected Handler mHandler;


    private Runnable mRunnableExitAdminMode = new Runnable() {
        @Override
        public void run() {
            LocalBroadcastManager.getInstance(getApplicationContext());
            //turnOffAdminMode();
        }
    };


    private BroadcastReceiver mAdminModeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            turnOffAdminMode();
        }
    };



    // Static Functions

    /** Activates the admin mode for the application for the amount of time defined by sAdminTimeout.
     * If the admin mode is already activated, resets the timeout back to sAdminTimeout. */
    public static void adminModeTimerStart(){
        Log.d(LOG_TAG,"adminModeTimerStart() called");
        sAdminExpireTime = SystemClock.uptimeMillis() + ADMIN_TIMEOUT;
    }

    /** Fortces the timer to expire the admin mode timeout */
    public static void adminModeTimerStop(){
        sAdminExpireTime = 0;
    }


    /** Returns the time in millis when the Admin Mode expires as measured by uptimeMillis()
     *  @return Milliseconds at which Admin Mode expires */
    public static long getAdminExpireTime(){
        return sAdminExpireTime;
    }


    /** Return the time left on admin mode or 0 if the admin mode already expired
     *
     * @return milliseconds left until the admin mode expires */
    public static long getRemainingAdminModeTime(){
        long timeLeft = sAdminExpireTime - SystemClock.uptimeMillis();
        return timeLeft > 0 ? timeLeft : 0;
    }

    /**
     * Returns whether the application is in Admin Mode
     *
     * @return True if in admin mode
     */
    public static boolean isAdmin(){
        return getRemainingAdminModeTime() > 0;
    }


    // Dynamic Functions

    public void onAdminModeExtend(){
        mHandler.removeCallbacks(mRunnableExitAdminMode);

        adminModeTimerStart();
        mHandler.postAtTime(mRunnableExitAdminMode,getAdminExpireTime());
    }

    public void onAdminModeStop(){
        mHandler.removeCallbacks(mRunnableExitAdminMode);
        adminModeTimerStop();
    }


    private void turnOffAdminMode(){
        Log.d(LOG_TAG,"Turning off admin mode option");

        // Make sure it doesn't return false positives and glitches
        onAdminModeStop();

        // Set the settings option to false
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences!=null &&
                sharedPreferences.getBoolean(getString(R.string.pref_key_admin_mode), false)){
            sharedPreferences.edit().putBoolean(getString(R.string.pref_key_admin_mode),false)
                    .apply();
        }

        // Recreate the activity so the theme changes are applied.
        this.recreate();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate Handler
        mHandler = new Handler(Looper.getMainLooper());
        // Instantiate LocalBroacastManager
        sLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
    }


    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onResume() {
        super.onResume();

        boolean currentAdminMode = isAdmin();

        // Starts the timeout
        if(currentAdminMode){
            Log.d(LOG_TAG,"Start timer with " + getRemainingAdminModeTime() + "ms");
            // Send a runnable to self to be executed when the Admin Mode time expires
            mHandler.postAtTime(mRunnableExitAdminMode,getAdminExpireTime());
        }

        if(mLastAdminMode != currentAdminMode) {
            mLastAdminMode = currentAdminMode;  // Update the current state
            if (currentAdminMode) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(getDrawable(R.color.AdminActionBarColor));
                    actionBar.setTitle(actionBar.getTitle() + getString(R.string.admin_mode_title_suffix));
                }
            } else {
                turnOffAdminMode();
            }
        }
    }


    @Override
    public void onPause(){
        super.onPause();

        // Remove exit admin mode runnables
        mHandler.removeCallbacks(mRunnableExitAdminMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove exit admin mode runnables
        mHandler.removeCallbacks(mRunnableExitAdminMode);
    }
}
