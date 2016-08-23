package ru.yandex.qatools.wifitool.utils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.yandex.qatools.wifitool.Params;

public class WifiConfigurationBuilder {
    @Nonnull
    public static WifiConfiguration create(Params params) {
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = params.quotedSsid;
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 100;

        switch (params.security) {
            case NONE:
                wfc.allowedKeyManagement.clear();
                wfc.allowedKeyManagement.set(KeyMgmt.NONE);
                wfc.allowedAuthAlgorithms.clear();
                break;
            case WEP:
                wfc.allowedKeyManagement.set(KeyMgmt.NONE);
                wfc.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                wfc.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (isHexString(params.pass)) {
                    wfc.wepKeys[0] = params.pass;
                } else {
                    wfc.wepKeys[0] = StringValues.enquote(params.pass);
                }
                wfc.wepTxKeyIndex = 0;
                break;
            case WPA:
                wfc.preSharedKey = StringValues.enquote(params.pass);
                break;
            case UNKNOWN:
            default:
                throw new IllegalArgumentException("Unknown security value " + params.security +
                        ". Argument was " + params.securityString);
        }
        return wfc;
    }

    private static boolean isHexString(@Nullable String pass) {
        return pass.matches("[0-9a-fA-F]+");
    }
}
