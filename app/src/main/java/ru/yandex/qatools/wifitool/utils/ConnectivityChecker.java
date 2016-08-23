package ru.yandex.qatools.wifitool.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ru.yandex.qatools.wifitool.Tag;

public class ConnectivityChecker {
    @Nonnull
    private final ConnectivityManager mConnectivityManager;

    @Nonnull
    private final WifiManager mWifiManager;

    @Inject
    public ConnectivityChecker(ConnectivityManager connectivityManager, WifiManager wifiManager) {
        mConnectivityManager = connectivityManager;
        mWifiManager = wifiManager;
    }

    public boolean isWifiNetworkConnected(int netId) {
        NetworkInfo network = mConnectivityManager.getActiveNetworkInfo();
        if (network == null) {
            return false;
        }

        if (network.getType() != ConnectivityManager.TYPE_WIFI) {
            return false;
        }

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.d(Tag.NAME, "Wifi is not connected");
            return false;
        }

        Log.d(Tag.NAME, "Wifi supplicant state: " + wifiInfo.getSupplicantState());

        return network.isConnected() && wifiInfo.getNetworkId() == netId;
    }
}
