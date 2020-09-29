package com.example.trazabilidadg;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class MyBrodcastRecieverService extends Service
{
    private static BroadcastReceiver br_ScreenOffReceiver;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        registerScreenOffReceiver();
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(br_ScreenOffReceiver);
        br_ScreenOffReceiver = null;
    }

    private void registerScreenOffReceiver()
    {
        /*br_ScreenOffReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "ACTION_SCREEN_OFF");
                // do something, e.g. send Intent to main app
            }
        };*/

        br_ScreenOffReceiver = new NetworkReceiver();

        //IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        //registerReceiver(br_ScreenOffReceiver, filter);

    }
}
