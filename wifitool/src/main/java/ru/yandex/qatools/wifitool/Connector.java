package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiManager;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import bolts.CancellationTokenSource;
import bolts.Continuation;
import bolts.Task;
import ru.yandex.qatools.wifitool.utils.ConnectivityChecker;
import ru.yandex.qatools.wifitool.utils.WifiManagerException;
import ru.yandex.qatools.wifitool.utils.WifiStates;

/**
 * Runs sequence of actions required for connection.
 */
class Connector {
    private static final int ENABLE_WIFI_TIMEOUT = 1000;
    private static final long CONNECTIVITY_TIMEOUT = 5000;

    @Nonnull
    private final WifiManager mWifiManager;

    @Nonnull
    private final ConnectivityMonitor mConnectivityMonitor;

    @Nonnull
    private final NetworkManager mNetworkManager;

    @Nonnull
    private final ConnectivityChecker mConnectivityChecker;

    @Inject
    Connector(WifiManager wifiManager, ConnectivityMonitor connectivityMonitor,
              NetworkManager networkManager, ConnectivityChecker connectivityChecker) {
        mWifiManager = wifiManager;
        mConnectivityMonitor = connectivityMonitor;
        mNetworkManager = networkManager;
        mConnectivityChecker = connectivityChecker;
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
                        .onSuccessTask(waitConnectivity());
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
        return Task.delay(ENABLE_WIFI_TIMEOUT).continueWith(task -> {
            int wifiState = mWifiManager.getWifiState();
            Log.d(Tag.NAME, WifiStates.getName(wifiState));
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                return params;
            }
            throw new IllegalStateException("WiFi could not be enabled. Now " +
                    WifiStates.getName(wifiState));
        });
    }

    /**
     * Get existing network id or add network and get its id.
     *
     * @return Network id.
     */
    @Nonnull
    private Continuation<Params, Integer> getNetworkId() {
        return task -> mNetworkManager.createNetwork(task.getResult());
    }

    @Nonnull
    private Continuation<Integer, Integer> connectNetwork() {
        return task -> {
            Integer netId = task.getResult();

            if (mConnectivityChecker.isWifiNetworkConnected(netId)) {
                return netId;
            }

            if (!mWifiManager.disconnect()) {
                throw new WifiManagerException("Could not disconnect WiFi");
            }
            if (!mWifiManager.enableNetwork(netId, true)) {
                throw new WifiManagerException("Could not enable a configured network");
            }
            if (!mWifiManager.reconnect()) {
                throw new WifiManagerException("Could not connect to a configured network");
            }
            return netId;
        };
    }

    private Continuation<Integer, Task<Void>> waitConnectivity() {
        return task -> {
            CancellationTokenSource ts = new CancellationTokenSource();
            ts.cancelAfter(CONNECTIVITY_TIMEOUT);
            return mConnectivityMonitor.wait(task.getResult(), ts.getToken());
        };
    }

}
