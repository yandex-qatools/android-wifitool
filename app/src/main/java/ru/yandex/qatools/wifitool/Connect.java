package ru.yandex.qatools.wifitool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Entry point. Redirects intent into an intent service.
 */
public class Connect extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(intent);
        serviceIntent.setClass(context, WifiIntentService.class);
        context.startService(serviceIntent);
    }
}
