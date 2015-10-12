package com.pandora_escape.javier.pandora_escape;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.CountDownLatch;

public class AdminModeService extends Service {


    public static final String ADMIN_MODE_EXTRA = "com.pandora_escape.javier.pandora_escape.ADMIN_MODE_EXTRA";

    private static LocalBroadcastManager sLocalBroadcastManager;

    static CountDownTimer sAdminModeTimer;

    public AdminModeService() {
    }


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Instantiate LocalBroacastManager
        sLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(sAdminModeTimer!=null) {
            sAdminModeTimer.cancel();
        }

        AdminActivity.adminModeTimerStart();

        sLocalBroadcastManager.sendBroadcast(new Intent(AdminActivity.ADMIN_MODE_INTENT)
                .putExtra(ADMIN_MODE_EXTRA,true));

        sAdminModeTimer = new CountDownTimer(AdminActivity.ADMIN_TIMEOUT,AdminActivity.ADMIN_TIMEOUT) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                sLocalBroadcastManager.sendBroadcast(new Intent(AdminActivity.ADMIN_MODE_INTENT)
                                                            .putExtra(ADMIN_MODE_EXTRA,false));
            }
        };

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        sLocalBroadcastManager.sendBroadcast(new Intent(AdminActivity.ADMIN_MODE_INTENT)
                .putExtra(ADMIN_MODE_EXTRA,false));

        super.onDestroy();
    }
}
