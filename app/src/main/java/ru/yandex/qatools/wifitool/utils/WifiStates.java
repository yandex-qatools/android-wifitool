package ru.yandex.qatools.wifitool.utils;

import android.net.wifi.WifiManager;
import android.util.SparseArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WifiStates {
    @Nonnull
    private static final SparseArray<String> mWifiStates = new SparseArray<>();

    static {
        mWifiStates.append(WifiManager.WIFI_STATE_DISABLED, "WIFI_STATE_DISABLED");
        mWifiStates.append(WifiManager.WIFI_STATE_DISABLING, "WIFI_STATE_DISABLING");
        mWifiStates.append(WifiManager.WIFI_STATE_ENABLED, "WIFI_STATE_ENABLED");
        mWifiStates.append(WifiManager.WIFI_STATE_ENABLING, "WIFI_STATE_ENABLING");
        mWifiStates.append(WifiManager.WIFI_STATE_UNKNOWN, "WIFI_STATE_UNKNOWN");
    }

    private WifiStates() {}

    @Nullable
    public static String getName(int wifiState) {
        return mWifiStates.get(wifiState);
    }
}
