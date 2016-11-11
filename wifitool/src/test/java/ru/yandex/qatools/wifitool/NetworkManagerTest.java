package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.annotation.Nonnull;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static ru.yandex.qatools.wifitool.NetworkManager.NO_ID;
import static ru.yandex.qatools.wifitool.TestData.NET_ID;
import static ru.yandex.qatools.wifitool.TestData.UNSECURE_PARAMS;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NetworkManagerTest {
    @Nonnull
    private final TestData mData;
    @Nonnull
    private NetworkManager mNetworkManager;

    public NetworkManagerTest() {
        MockitoAnnotations.initMocks(this);
        mData = new TestData();
        mNetworkManager = new NetworkManager(mData.wifiManager);
    }

    @Test
    public void networkIsConfiguredAndConnected_ReturnsId() throws Exception {
        mData.whenNetworkIsConnected();

        assertEquals(NET_ID, mNetworkManager.createNetwork(UNSECURE_PARAMS));
    }

    @Test
    public void networkIsConfiguredAndNotConnected_CanBeRemoved_ReturnsId() throws Exception {
        mData.whenNetworkIsConfigured();
        whenNetworkCanBeRemoved();
        whenNetworkCanBeAdded();

        assertEquals(NET_ID, mNetworkManager.createNetwork(UNSECURE_PARAMS));
    }

    @Test(expected = Exception.class)
    public void networkIsConfiguredAndNotConnected_CantUpdate_Throws() throws Exception {
        mData.whenNetworkIsConfigured();

        assertEquals(NET_ID, mNetworkManager.createNetwork(UNSECURE_PARAMS));
    }

    @Test
    public void networkIsNotConfigured_CanAdd_ReturnsId() throws Exception {
        whenNetworkCanBeAdded();

        assertEquals(NET_ID, mNetworkManager.createNetwork(UNSECURE_PARAMS));
    }

    @Test(expected = Exception.class)
    public void networkIsNotConfigured_CantAdd_Throws() throws Exception {
        whenNetworkCanNotBeAdded();

        mNetworkManager.createNetwork(UNSECURE_PARAMS);
    }

    private void whenNetworkCanBeRemoved() {
        doReturn(true).when(mData.wifiManager).removeNetwork(NET_ID);
    }

    private void whenNetworkCanBeAdded() {
        doReturn(NET_ID).when(mData.wifiManager).addNetwork(any(WifiConfiguration.class));
    }

    private void whenNetworkCanNotBeAdded() {
        doReturn(NO_ID).when(mData.wifiManager).addNetwork(any(WifiConfiguration.class));
    }

}
