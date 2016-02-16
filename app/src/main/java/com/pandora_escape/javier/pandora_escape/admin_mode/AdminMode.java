package com.pandora_escape.javier.pandora_escape.admin_mode;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Javier on 12/10/2015.
 *
 * Contract class including tools to handle Admin Mode in different activities. Also stores the
 * static variables to keep track of the Admin Mode state.
 *
 */
public abstract class AdminMode {

    // Constants

    /** Logging tag */
    public static final String LOG_TAG = "AdminMode";

    /** Amount of time the admin mode is active */
    //public static final long ADMIN_TIMEOUT = 2*60*1000; // 2 minutes in milliseconds
    public static final long ADMIN_TIMEOUT = 15*1000; // 15 secs in millis for testing

    /** Admin Mode intent identifiers */
    public static final String ADMIN_MODE_INTENT = "com.pandora_escape.javier.pandora_escape.admin_mode_status_change";
    public static final String EXTRA_ADMIN_MODE = "com.pandora_escape.javier.pandora_escape.EXTRA_ADMIN_MODE";

    public static final String ADMIN_COUNTDOWN_INTENT = "com.pandora_escape.javier.pandora_escape.admin_mode_countdown";
    public static final String EXTRA_ADMIN_COUNTDOWN = "com.pandora_escape.javier.pandora_escape.EXTRA_ADMIN_COUNTDOWN";


    // Variables

    /** Current status of Admin Mode throughout the application */
    private static boolean sAdminMode = false;

    /** Time in millis when the admin mode ends */
    private static long sAdminExpireTime = 0;


    // Static Functions

    /**
     * Returns whether the application is in Admin Mode
     *
     * @return True if in admin mode, false if in user mode
     */
    public static boolean isAdmin() {
        return sAdminMode;
    }


    /** Returns the time in millis when the Admin Mode expires as measured by uptimeMillis()
     *
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


    /** Sets the state variable to Admin Mode (true) and the expire time when the system will
     * go back to User Mode (false) */
    protected static void startAdminMode(){
        sAdminMode = true;
        sAdminExpireTime = SystemClock.uptimeMillis() + ADMIN_TIMEOUT;
    }


    /** Terminates the Admin Mode period immediately. Sets the state variable back to User Mode
     * (false) and resets the timer */
    protected static void stopAdminMode(){
        sAdminMode = false;
        sAdminExpireTime = 0;
    }


    /** Sets the current admin mode of the activity by starting or stopping the associated
     * Admin Mode Service and updating the Admin Mode state variables.
     *
     * @param context Application context used to call the startService() function
     * @param adminMode Determines new admin mode state (true = Admin Mode; false = User Mode) */
    public static void setAdminMode(Context context, boolean adminMode){
        sAdminMode = adminMode;

        if(adminMode){
            Log.d(LOG_TAG, "AdminService started");
            context.startService(new Intent(context,AdminModeService.class));
        } else {
            Log.d(LOG_TAG, "AdminService stopped");
            context.stopService(new Intent(context,AdminModeService.class));
        }
    }

}
