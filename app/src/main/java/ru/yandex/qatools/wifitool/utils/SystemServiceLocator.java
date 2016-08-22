package ru.yandex.qatools.wifitool.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import javax.annotation.Nonnull;

import static android.content.Context.WIFI_SERVICE;

public class SystemServiceLocator {
    @Nonnull
    private final Context mContext;

    public SystemServiceLocator(Context context) {
        mContext = context;
    }

    @Nonnull
    public WifiManager getWifiManager() {
        return (WifiManager) mContext.getSystemService(WIFI_SERVICE);
    }

    public ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
