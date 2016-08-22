package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import javax.annotation.Nonnull;

import ru.yandex.qatools.wifitool.utils.SystemServiceLocator;
import ru.yandex.qatools.wifitool.utils.WifiConfigurationBuilder;

class NetworkManager {
    static final int NO_ID = -1;

    @Nonnull
    private final WifiManager mWifiManager;

    NetworkManager(SystemServiceLocator serviceLocator) {
        mWifiManager = serviceLocator.getWifiManager();
    }

    int getNetworkId(Params params) throws Exception {
        Log.d(Tag.NAME, "Get connected network id...");
        int connectedNetId = getConnectedNetworkWithSsid(params.quotedSsid);
        if (connectedNetId != NO_ID) {
            Log.d(Tag.NAME, "Configured network found. It is connected");
            return connectedNetId;
        }

        int netId = getConfiguredNetworkId(params);
        if (netId == NO_ID) {
            Log.d(Tag.NAME, "Configured network not found");
            return addNetwork(params);
        }

        Log.d(Tag.NAME, "Configured network found. It is not connected");
        if (updateNetworkConfiguration(params, netId)) {
            Log.d(Tag.NAME, "Network configuration updated");
            return netId;
        } else {
            Log.d(Tag.NAME, "Network configuration update failed. Removing configured network");
            if (mWifiManager.removeNetwork(netId)) {
                return addNetwork(params);
            }
            throw new IllegalStateException("Unable to remove existing network");
        }
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
        Log.d(Tag.NAME, "Add network");

        WifiConfiguration wfc = WifiConfigurationBuilder.create(params);
        int result = mWifiManager.addNetwork(wfc);
        if (result == NO_ID) {
            throw new IllegalStateException("Could not add network");
        }
        Log.d(Tag.NAME, "Network added");
        return result;
    }

    private boolean updateNetworkConfiguration(Params params, int netId) {
        Log.d(Tag.NAME, "Updating network configuration");
        WifiConfiguration config = WifiConfigurationBuilder.create(params);
        config.networkId = netId;
        return mWifiManager.updateNetwork(config) != NO_ID;
    }

}
