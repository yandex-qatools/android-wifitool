package ru.yandex.qatools.wifitool;

import android.content.Context;

import javax.annotation.Nonnull;

import ru.yandex.qatools.wifitool.utils.SystemServiceLocator;

class ConnectorFactory {
    @Nonnull
    private final Context mContext;

    @Nonnull
    private final SystemServiceLocator mServiceLocator;

    @Nonnull
    private final NetworkManager mNetworkManager;

    ConnectorFactory(Context context) {
        mContext = context;
        mServiceLocator = new SystemServiceLocator(context);
        mNetworkManager = new NetworkManager(mServiceLocator);
    }

    @Nonnull
    Connector create() {
        return new Connector(mServiceLocator,
                new ConnectivityChecker(mContext, mServiceLocator),
                mNetworkManager);
    }
}
