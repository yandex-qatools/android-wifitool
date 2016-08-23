package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static ru.yandex.qatools.wifitool.NetworkManager.NO_ID;
import static ru.yandex.qatools.wifitool.TestData.NET_ID;
import static ru.yandex.qatools.wifitool.TestData.UNSECURE_PARAMS;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NetworkManagerTest {
    private final TestData mData;

    private NetworkManager mNetworkManager;

    public NetworkManagerTest() {
        MockitoAnnotations.initMocks(this);
        mData = new TestData();
        mNetworkManager = new NetworkManager(mData.wifiManager);
    }

    @Test
    public void networkIsConfiguredAndConnected_ReturnsId() throws Exception {
        mData.whenNetworkIsConnected();

        assertEquals(NET_ID, mNetworkManager.getNetworkId(UNSECURE_PARAMS));
    }

    @Test
    public void networkIsConfiguredAndNotConnected_CanUpdate_ReturnsId() throws Exception {
        mData.whenNetworkIsConfigured();
        doReturn(NET_ID).when(mData.wifiManager).updateNetwork(any(WifiConfiguration.class));

        assertEquals(NET_ID, mNetworkManager.getNetworkId(UNSECURE_PARAMS));
    }

    @Test(expected = Exception.class)
    public void networkIsConfiguredAndNotConnected_CantUpdate_Throws() throws Exception {
        mData.whenNetworkIsConfigured();
        doReturn(NO_ID).when(mData.wifiManager).updateNetwork(any(WifiConfiguration.class));

        assertEquals(NET_ID, mNetworkManager.getNetworkId(UNSECURE_PARAMS));
    }

    @Test
    public void networkIsNotConfigured_CanAdd_ReturnsId() throws Exception {
        doReturn(NET_ID).when(mData.wifiManager).addNetwork(any(WifiConfiguration.class));

        assertEquals(NET_ID, mNetworkManager.getNetworkId(UNSECURE_PARAMS));
    }

    @Test(expected = Exception.class)
    public void networkIsNotConfigured_CantAdd_Throws() throws Exception {
        doReturn(NO_ID).when(mData.wifiManager).addNetwork(any(WifiConfiguration.class));

        mNetworkManager.getNetworkId(UNSECURE_PARAMS);
    }

}
