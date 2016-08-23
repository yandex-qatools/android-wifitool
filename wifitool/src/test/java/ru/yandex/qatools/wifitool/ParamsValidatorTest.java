package ru.yandex.qatools.wifitool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.wifitool.TestData.SOME_PASS;
import static ru.yandex.qatools.wifitool.TestData.SOME_SSID;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ParamsValidatorTest {

    private static final String SOME_STRING = "Foo";
    private static final int NEGATIVE = -1;

    @Test
    public void noSsid_Invalid() throws Exception {
        Params params = new Params(null, SOME_PASS, ParamNames.SECURITY_WPA, 0, 0);

        assertFalse("SSID absence must be invalid",
                new ParamsValidator(params).isValid);
    }

    @Test
    public void noSecurity_NoPassword_Valid() throws Exception {
        Params params = new Params(SOME_SSID, SOME_PASS, ParamNames.SECURITY_WPA, 0, 0);

        assertTrue("No password must be valid with unsecure WiFi",
                new ParamsValidator(params).isValid);
    }

    @Test
    public void hasSecurity_NoPassword_Invalid() throws Exception {
        Params params = new Params(SOME_SSID, null, ParamNames.SECURITY_WPA, 0, 0);

        assertFalse("Password must be specified when securityString is specified",
                new ParamsValidator(params).isValid);
    }

    @Test
    public void hasSecurity_HasPassword_Valid() throws Exception {
        Params params = new Params(SOME_SSID, SOME_PASS, ParamNames.SECURITY_WPA, 0, 0);

        assertTrue("Security with password must be valid",
                new ParamsValidator(params).isValid);
    }

    @Test
    public void unknownSecurity_Invalid() throws Exception {
        Params params = new Params(SOME_SSID, SOME_PASS, SOME_STRING, 0, 0);

        assertFalse("Only WEP|WPA securityString can be valid",
                new ParamsValidator(params).isValid);
    }

    @Test
    public void negativeRetryCount_Invalid() throws Exception {
        Params params = new Params(SOME_SSID, SOME_PASS, ParamNames.SECURITY_WPA, NEGATIVE, 0);

        assertFalse("Retry count must be greater than or equal to 0",
                new ParamsValidator(params).isValid);
    }

    @Test
    public void negativeRetryDelay_Invalid() throws Exception {
        Params params = new Params(SOME_SSID, SOME_PASS, ParamNames.SECURITY_WPA, 0, NEGATIVE);

        assertFalse("Retry delay must be greater than or equal to 0",
                new ParamsValidator(params).isValid);
    }
}
