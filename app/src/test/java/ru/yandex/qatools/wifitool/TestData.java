package ru.yandex.qatools.wifitool;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import javax.annotation.Nonnull;

import edu.emory.mathcs.backport.java.util.Arrays;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static ru.yandex.qatools.wifitool.utils.StringValues.enquote;

class TestData {
    static final int NET_ID = 100;

    static final String SOME_SSID = "Foo";
    static final String SOME_PASS = "Bar";
    static final String SOME_SSID_QUOTED = enquote(SOME_SSID);
    static final String NO_PASS = null;
    static final String NO_SECURITY = null;
    private static final int NO_RETRIES = 0;
    private static final int NO_DELAY = 0;

    static final Params UNSECURE_PARAMS =
            new Params(SOME_SSID, NO_PASS, NO_SECURITY, NO_RETRIES, NO_DELAY);

    @Mock
    WifiManager wifiManager;

    @Mock
    private WifiInfo activeWifi;

    @Mock
    private WifiConfiguration configuredWifi;

    TestData() {
        MockitoAnnotations.initMocks(this);
    }

    @Nonnull
    static WifiManager mockWifiManager() {
        ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
        WifiManager wifiManager = mock(WifiManager.class);
        shadowApplication.setSystemService(Context.WIFI_SERVICE, wifiManager);
        return wifiManager;
    }

    void whenNetworkIsConfigured() {
        configuredWifi.networkId = NET_ID;
        configuredWifi.SSID = SOME_SSID_QUOTED;
        List<WifiConfiguration> configurations =
                Arrays.asList(new WifiConfiguration[]{configuredWifi});

        doReturn(configurations).when(wifiManager).getConfiguredNetworks();
    }

    void whenNetworkIsConnected() {
        doReturn(NET_ID).when(activeWifi).getNetworkId();
        doReturn(SOME_SSID_QUOTED).when(activeWifi).getSSID();

        doReturn(activeWifi).when(wifiManager).getConnectionInfo();
    }
}
