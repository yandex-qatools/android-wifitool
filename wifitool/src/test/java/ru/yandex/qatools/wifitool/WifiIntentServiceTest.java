package ru.yandex.qatools.wifitool;

import android.content.Intent;
import android.net.wifi.WifiManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ServiceController;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import bolts.Continuation;
import bolts.Task;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static ru.yandex.qatools.wifitool.TestData.SOME_SSID;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WifiIntentServiceTest {

    private final WifiIntentService mIntentService;

    @Mock
    private Task<Void> mContinuationTask;

    @Mock
    private Task<Void> mConnectTask;

    @Nonnull
    private WifiManager mWifiManager = TestData.mockWifiManager();

    @Mock
    private RetryConnector mRetryConnector;

    public WifiIntentServiceTest() throws InterruptedException {
        MockitoAnnotations.initMocks(this);
        ServiceController<WifiIntentService> controller =
                Robolectric.buildService(WifiIntentService.class);
        controller.create();
        mIntentService = controller.get();

        doReturn(mConnectTask).when(mRetryConnector).connect(any(Params.class));
        doReturn(mContinuationTask).when(mConnectTask).continueWith(any(Continuation.class));

        // mockito fails to mock javax.inject.Provider
        mIntentService.mRetryConnectorProvider = new Provider<RetryConnector>() {
            @Override
            public RetryConnector get() {
                return mRetryConnector;
            }
        };
    }

    @Test
    public void invalidIntentParams_Fails() throws Exception {
        Intent intent = new Intent();
        mIntentService.onHandleIntent(intent);
        Assert.assertThat(ShadowLog.getLogsForTag(Tag.FAIL), is(not(empty())));
    }

    @Test
    public void validIntentParams_WifiUnknown_Fails() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.SSID, SOME_SSID);

        doReturn(WifiManager.WIFI_STATE_UNKNOWN).when(mWifiManager).getWifiState();

        mIntentService.onHandleIntent(intent);

        Assert.assertThat("Actual logs: " + ShadowLog.getLogs(),
                ShadowLog.getLogsForTag(Tag.FAIL), is(not(empty())));
    }

    @Test
    public void validIntentParams_WifiEnabled_Succeeds() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.SSID, SOME_SSID);

        doReturn(WifiManager.WIFI_STATE_ENABLED).when(mWifiManager).getWifiState();

        mIntentService.onHandleIntent(intent);

        Assert.assertThat("Actual logs: " + ShadowLog.getLogs(),
                ShadowLog.getLogsForTag(Tag.FAIL), is(not(empty())));
    }
}
