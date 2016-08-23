package ru.yandex.qatools.wifitool;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.WIFI_SERVICE;

@Module
class IntentModule {
    @Nonnull
    private final Context mContext;

    IntentModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mContext;
    }

    @Provides
    @Singleton
    WifiManager provideWifiManager() {
        return (WifiManager) mContext.getSystemService(WIFI_SERVICE);
    }

    @Provides
    @Singleton
    ConnectivityManager provideConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
