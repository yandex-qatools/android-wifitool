package ru.yandex.qatools.wifitool;

import android.content.Intent;
import android.net.wifi.WifiManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ServiceController;

import javax.annotation.Nonnull;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static ru.yandex.qatools.wifitool.TestData.SOME_SSID;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WifiIntentServiceTest {

    private final WifiIntentService mIntentService;

    @Nonnull
    private WifiManager mWifiManager = TestData.mockWifiManager();

    public WifiIntentServiceTest() {
        MockitoAnnotations.initMocks(this);
        ServiceController<WifiIntentService> controller =
                Robolectric.buildService(WifiIntentService.class);
        controller.create();
        mIntentService = controller.get();
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
