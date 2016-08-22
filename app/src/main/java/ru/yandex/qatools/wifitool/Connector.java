package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiManager;
import android.util.Log;

import javax.annotation.Nonnull;

import bolts.CancellationToken;
import bolts.CancellationTokenSource;
import bolts.Continuation;
import bolts.Task;
import ru.yandex.qatools.wifitool.utils.SystemServiceLocator;
import ru.yandex.qatools.wifitool.utils.WifiStates;

/**
 * Runs sequence of actions required for connection.
 */
class Connector {
    private static final int ENABLE_WIFI_DELAY = 1000;
    private static final long CONNECTIVITY_DELAY = 5000;

    @Nonnull
    private final WifiManager mWifiManager;

    @Nonnull
    private ConnectivityChecker mConnectivityChecker;

    @Nonnull
    private NetworkManager mNetworkManager;

    Connector(SystemServiceLocator serviceLocator,
              ConnectivityChecker connectivityChecker, NetworkManager networkManager) {
        mWifiManager = serviceLocator.getWifiManager();
        mConnectivityChecker = connectivityChecker;
        mNetworkManager = networkManager;
    }

    @Nonnull
    Task<Void> connect(final Params params) {
        int wifiState = mWifiManager.getWifiState();
        Log.d(Tag.NAME, WifiStates.getName(wifiState));
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_ENABLED:
                return enableWifi(params)
                        .onSuccess(getNetworkId())
                        .onSuccess(connectNetwork())
                        .onSuccessTask(mConnectivityChecker.checkNetwork(getCancellationToken()));
            case WifiManager.WIFI_STATE_UNKNOWN:
            default:
                throw new IllegalStateException("WiFi state unknown. Please inspect the device");
        }
    }

    @Nonnull
    private Task<Params> enableWifi(final Params params) {
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return Task.forResult(params);
        }

        Log.d(Tag.NAME, "Setting WiFi enabled");
        mWifiManager.setWifiEnabled(true);
        return Task.delay(ENABLE_WIFI_DELAY).continueWith(new Continuation<Void, Params>() {
            @Override
            public Params then(Task<Void> task) {
                int wifiState = mWifiManager.getWifiState();
                Log.d(Tag.NAME, WifiStates.getName(wifiState));
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    return params;
                }
                throw new IllegalStateException("WiFi could not be enabled. Now " +
                        WifiStates.getName(wifiState));
            }
        });
    }

    /**
     * Get existing network id or add network and get its id.
     *
     * @return Network id.
     */
    @Nonnull
    private Continuation<Params, Integer> getNetworkId() {
        return new Continuation<Params, Integer>() {
            @Override
            public Integer then(Task<Params> task) throws Exception {
                return mNetworkManager.getNetworkId(task.getResult());
            }
        };
    }

    @Nonnull
    private Continuation<Integer, Integer> connectNetwork() {
        return new Continuation<Integer, Integer>() {
            @Override
            public Integer then(Task<Integer> task) throws Exception {
                Integer netId = task.getResult();

                if (mConnectivityChecker.isWifiNetworkConnected(netId)) {
                    return netId;
                }

                if (!mWifiManager.disconnect()) {
                    throw new Exception("Could not disconnect WiFi");
                }
                if (!mWifiManager.enableNetwork(netId, true)) {
                    throw new Exception("Could not enable a configured network");
                }
                if (!mWifiManager.reconnect()) {
                    throw new Exception("Could not connect to a configured network");
                }
                return netId;
            }
        };
    }

    @Nonnull
    private CancellationToken getCancellationToken() {
        CancellationTokenSource ts = new CancellationTokenSource();
        ts.cancelAfter(CONNECTIVITY_DELAY);
        return ts.getToken();
    }
}
