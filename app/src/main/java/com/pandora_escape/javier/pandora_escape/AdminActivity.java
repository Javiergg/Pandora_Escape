
package com.pandora_escape.javier.pandora_escape;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *
 * Created by Javier on 23/08/15.
 */
public class AdminActivity extends Activity {

    // Constants

    /** Logging tag */
    private static final String LOG_TAG = "AdminActivity";

    /** Amount of time the admin mode is active */
    public static final long ADMIN_TIMEOUT = 2*60*1000; // 2 minutes in milliseconds
    //public static final long ADMIN_TIMEOUT = 15*1000; // 15 secs in millis for testing


    // Variables

    /** Time in millis when the admin mode ends */
    private static long sAdminExpireTime = 0;

    /** Current user level (User = false, Admin = true) */
    private boolean mLastAdminMode = false;

    /** CountDownTimer used to disable the admin mode when the time expires */
    private CountDownTimer mAdminModeCountDownTimer;



    // Static Functions

    /** Activates the admin mode for the application for the amount of time defined by sAdminTimeout.
     * If the admin mode is already activated, resets the timeout back to sAdminTimeout. */
    public static void adminModeTimerStart(){
        Log.d(LOG_TAG,"adminModeTimerStart() called");
        sAdminExpireTime = System.currentTimeMillis() + ADMIN_TIMEOUT;
    }

    /** Fortces the timer to expire the admin mode timeout */
    public static void adminModeTimerStop(){
        sAdminExpireTime = 0;
    }

    /** Return the time left on admin mode or 0 if the admin mode already expired
     *
     * @return milliseconds left until the admin mode expires */
    public static long getRemainingAdminModeTime(){
        long timeLeft = sAdminExpireTime - System.currentTimeMillis();
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

    public void cancelAdminModeTimout(){
        if(mAdminModeCountDownTimer!=null){
            mAdminModeCountDownTimer.cancel();
        }
    }

    private void turnOffAdminModeOption(){
        Log.d(LOG_TAG,"Turning off admin mode option");

        // Make sure it doesn't return false positives and glitches
        adminModeTimerStop();

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
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onResume() {
        super.onResume();

        boolean currentAdminMode = isAdmin();

        // Starts the timeout
        if(currentAdminMode){
            Log.d(LOG_TAG,"Start timer with " + getRemainingAdminModeTime() + "ms");
            mAdminModeCountDownTimer = new CountDownTimer(getRemainingAdminModeTime(),Long.MAX_VALUE) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.d(LOG_TAG,(millisUntilFinished/1000)+"s left of admin.");
                }

                @Override
                public void onFinish() {
                    turnOffAdminModeOption();
                }
            }.start();
        }

        if(mLastAdminMode != currentAdminMode) {
            mLastAdminMode = currentAdminMode;  // Update the current state
            if (currentAdminMode) {
                ActionBar actionBar = getActionBar();
                actionBar.setBackgroundDrawable(getDrawable(R.color.AdminActionBarColor));
                actionBar.setTitle(actionBar.getTitle() + getString(R.string.admin_mode_title_suffix));
            } else {
                turnOffAdminModeOption();
            }
        }
    }


    @Override
    public void onPause(){
        super.onPause();

        // Cancel the timer so it doesn't call onFinish while the activity is not visible
        if(mAdminModeCountDownTimer != null){
            Log.d(LOG_TAG,"Counter deactivated");
            mAdminModeCountDownTimer.cancel();
            mAdminModeCountDownTimer = null;
        }
    }


}
