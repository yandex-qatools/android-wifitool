package ru.yandex.qatools.wifitool;

import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ParamsTest {

    @Test
    public void retryCount_IsParsed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.RETRY_COUNT, "100");
        assertEquals(100, Params.create(intent).retryCount);
    }

    @Test
    public void retryDelay_IsParsed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.RETRY_DELAY, "50");
        assertEquals(50, Params.create(intent).retryDelay);
    }

    @Test
    public void ssid_IsParsed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.SSID, TestData.SOME_SSID);
        assertEquals("\"" + TestData.SOME_SSID + "\"", Params.create(intent).quotedSsid);
    }

    @Test
    public void securityWpa_IsParsed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.SECURITY, "WPA");
        assertEquals(Security.WPA, Params.create(intent).security);
    }

    @Test
    public void securityWep_IsParsed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.SECURITY, "WEP");
        assertEquals(Security.WEP, Params.create(intent).security);
    }

    @Test
    public void password_IsParsed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ParamNames.PASS, TestData.SOME_PASS);
        assertEquals(TestData.SOME_PASS, Params.create(intent).pass);
    }
}
