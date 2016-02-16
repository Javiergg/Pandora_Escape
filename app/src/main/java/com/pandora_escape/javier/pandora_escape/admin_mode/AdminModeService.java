package com.pandora_escape.javier.pandora_escape.admin_mode;

import com.pandora_escape.javier.pandora_escape.R;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;


public class AdminModeService extends Service {

    /** Local pointer to the LocalBroadcastManager */
    private static LocalBroadcastManager sLocalBroadcastManager;

    private static CountDownTimer sAdminModeTimer;


    /** Default constructor. Empty */
    public AdminModeService() {}


    /**  */
    private void broadcastAdminMode(boolean adminMode){
        // Set or disable the Admin Mode time variable
        if(adminMode){
            AdminMode.startAdminMode();
        } else {
            AdminMode.stopAdminMode();
        }

        // Generate admin mode intent with mode as boolean extra
        Intent adminModeIntent = new Intent(AdminMode.ADMIN_MODE_INTENT)
                .putExtra(AdminMode.EXTRA_ADMIN_MODE, adminMode);
        // Send broadcast message
        sLocalBroadcastManager.sendBroadcast(adminModeIntent);
    }


    private void broadcastAdminCountdown(long time){
        // Generate tick intent with time as extra
        Intent adminModeIntent = new Intent(AdminMode.ADMIN_COUNTDOWN_INTENT)
                .putExtra(AdminMode.EXTRA_ADMIN_COUNTDOWN, time);
        // Send second tick broadcast message
        sLocalBroadcastManager.sendBroadcast(adminModeIntent);
    }


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Disable the timer if it is running
        if(sAdminModeTimer!=null) {
            sAdminModeTimer.cancel();
        }

        // Broadcast the start of Admin Mode
        broadcastAdminMode(true);

        /* Start a countdown timer that will broadcast the millis left every second and the
           end of the Admin Mode period */
        sAdminModeTimer = new CountDownTimer(AdminMode.ADMIN_TIMEOUT, 1000) {
            @Override
            public void onTick(long timeLeft) {
                // Broadcast a second tick
                broadcastAdminCountdown(timeLeft);
            }

            @Override
            public void onFinish() {
                // Set the settings option to false
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences != null &&
                        sharedPreferences.getBoolean(getString(R.string.pref_key_admin_mode), false)) {
                    sharedPreferences.edit().putBoolean(getString(R.string.pref_key_admin_mode), false)
                            .apply();
                }

                // Broadcast the end of Admin Mode
                broadcastAdminMode(false);

                // Stop the service
                stopSelf();
            }
        }.start();

        // If the process is killed it will not restart the service
        return Service.START_NOT_STICKY;
    }


    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        broadcastAdminMode(false);

        super.onDestroy();
    }
}
