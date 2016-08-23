package ru.yandex.qatools.wifitool;

import android.content.Intent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.yandex.qatools.wifitool.utils.StringValues;

/**
 * Request parameters.
 */
public class Params {
    @Nullable
    public final String quotedSsid;

    @Nullable
    public final String pass;

    @Nullable
    public final String securityString;

    @Nonnull
    public final Security security;

    final int retryCount;

    final int retryDelay;

    Params(@Nullable String ssid, @Nullable String pass, @Nullable String security,
           int retryCount, int retryDelay) {
        this.quotedSsid = StringValues.enquote(ssid);
        this.pass = pass;
        this.security = makeSecurity(security, pass);
        this.securityString = security;
        this.retryCount = retryCount;
        this.retryDelay = retryDelay;
    }

    @Nonnull
    static Params create(Intent intent) {
        String ssid = intent.getStringExtra(ParamNames.SSID);
        String pass = intent.getStringExtra(ParamNames.PASS);
        String security = intent.getStringExtra(ParamNames.SECURITY);

        int retryCount = getIntExtra(intent, ParamNames.RETRY_COUNT, 0);
        int retryDelay = getIntExtra(intent, ParamNames.RETRY_DELAY, 10000);

        return new Params(ssid, pass, security, retryCount, retryDelay);
    }

    private static int getIntExtra(Intent intent, String name, int defaultValue) {
        if (intent.hasExtra(name)) {
            return Integer.parseInt(intent.getStringExtra(name));
        } else {
            return defaultValue;
        }
    }

    @Nonnull
    static Security makeSecurity(@Nullable String security, @Nullable String pass) {
        if (security == null) {
            if (pass == null) {
                return Security.NONE;
            } else {
                return Security.WPA;
            }
        }
        if (ParamNames.SECURITY_WPA.equalsIgnoreCase(security)) {
            return Security.WPA;
        }
        if (ParamNames.SECURITY_WEP.equalsIgnoreCase(security)) {
            return Security.WEP;
        }
        return Security.UNKNOWN;
    }

}
