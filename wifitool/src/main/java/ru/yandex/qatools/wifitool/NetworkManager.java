package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ru.yandex.qatools.wifitool.utils.WifiConfigurationBuilder;

/**
 * Sets up network.
 */
class NetworkManager {
    static final int NO_ID = -1;

    @Nonnull
    private final WifiManager mWifiManager;

    @Inject
    NetworkManager(WifiManager wifiManager) {
        mWifiManager = wifiManager;
    }

    /**
     * Get pre-configured wifi network or add a new one.
     * @param params Network ssid and security parameters.
     * @return NetworkId of a network.
     * @throws Exception if network could not be set up.
     */
    int createNetwork(Params params) throws Exception {
        Log.d(Tag.NAME, "Get connected network id...");
        int connectedNetId = getConnectedNetworkWithSsid(params.quotedSsid);
        if (connectedNetId != NO_ID) {
            Log.d(Tag.NAME, "Configured network found. It is connected");
            return connectedNetId;
        }

        int netId = getConfiguredNetworkId(params);
        if (netId == NO_ID) {
            Log.d(Tag.NAME, "Configured network not found");
        } else {
            // updated network configuration are not able to connect on some devices.
            // removing network and adding it again is quite fast and is durable enough
            Log.d(Tag.NAME, "Configured network found. It is not connected");
            Log.d(Tag.NAME, "Removing configured network");
            if (!mWifiManager.removeNetwork(netId)) {
                throw new IllegalStateException("Unable to remove existing network");
            }
        }
        return addNetwork(params);
    }

    private int getConnectedNetworkWithSsid(String maskedSsid) {
        WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
        if (connectionInfo != null && maskedSsid.equals(connectionInfo.getSSID())) {
            return connectionInfo.getNetworkId();
        } else {
            return NO_ID;
        }
    }

    private int getConfiguredNetworkId(Params params) {
        List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration network : networks) {
            if (params.quotedSsid.equals(network.SSID)) {
                return network.networkId;
            }
        }
        return NO_ID;
    }

    private int addNetwork(Params params) {
        Log.d(Tag.NAME, "Adding network");

        WifiConfiguration wfc = WifiConfigurationBuilder.create(params);
        int result = mWifiManager.addNetwork(wfc);
        if (result == NO_ID) {
            throw new IllegalStateException("Could not add network");
        }
        Log.d(Tag.NAME, "Network added");
        return result;
    }
}
