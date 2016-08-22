package ru.yandex.qatools.wifitool;

import android.net.wifi.WifiManager;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.annotation.Nonnull;

import bolts.CancellationToken;
import bolts.Continuation;
import bolts.Task;
import ru.yandex.qatools.wifitool.utils.SystemServiceLocator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.yandex.qatools.wifitool.TestData.NET_ID;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectorTest {
    @Nonnull
    private final TestData mData;

    @Mock
    SystemServiceLocator mServiceLocator;

    @Mock
    ConnectivityChecker mConnectivityChecker;

    @Mock
    NetworkManager mNetworkManager;

    public ConnectorTest() {
        MockitoAnnotations.initMocks(this);

        mData = new TestData();
        mConnector = new Connector(mData.serviceLocator, mConnectivityChecker, mNetworkManager);
    }

    private Connector mConnector;

    @Test(expected = IllegalStateException.class)
    public void unknownWifi_Throws() throws InterruptedException {
        doReturn(WifiManager.WIFI_STATE_UNKNOWN).when(mData.wifiManager).getWifiState();

        Task<Void> task = mConnector.connect(TestData.UNSECURE_PARAMS);
        task.waitForCompletion();
    }

    @Test
    public void enabledWifi_Succeeds() throws Exception {
        doReturn(WifiManager.WIFI_STATE_ENABLED).when(mData.wifiManager).getWifiState();

        doReturn(true).when(mConnectivityChecker).isWifiNetworkConnected(NET_ID);
        doReturn(NET_ID).when(mNetworkManager).getNetworkId(any(Params.class));
        doReturn(continuation()).when(mConnectivityChecker)
                .checkNetwork(any(CancellationToken.class));

        Task<Void> task = mConnector.connect(TestData.UNSECURE_PARAMS);
        task.waitForCompletion();

        Assert.assertFalse("Should connect but got\n" + task.getError(),
                task.isFaulted());
    }

    @Test
    public void disabledWifi_EnablesWifi() throws Exception {
        when(mData.wifiManager.getWifiState())
                .thenReturn(WifiManager.WIFI_STATE_DISABLED);

        Task<Void> task = mConnector.connect(TestData.UNSECURE_PARAMS);
        task.waitForCompletion();

        verify(mData.wifiManager).setWifiEnabled(true);
    }


    @Nonnull
    private Continuation<Void, Task<Void>> continuation() {
        return new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return Task.forResult(null);
            }
        };
    }
}
