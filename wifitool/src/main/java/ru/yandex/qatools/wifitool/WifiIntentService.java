package ru.yandex.qatools.wifitool;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import bolts.Continuation;
import bolts.Task;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WifiIntentService extends IntentService {

    private final Injector mComponent;

    @Inject
    Provider<RetryConnector> mRetryConnectorProvider;

    public WifiIntentService() {
        super("WifiIntentService");

        mComponent = DaggerInjector.builder().intentModule(new IntentModule(this)).build();
        mComponent.inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            handle(intent);
        } catch (InterruptedException e) {
            Log.e(Tag.FAIL, "Process got interrupted", e);
        }
    }

    void handle(Intent intent) throws InterruptedException {
        Params params = Params.create(intent);
        ParamsValidator validator = new ParamsValidator(params);
        if (validator.isValid) {
            Log.d(Tag.NAME, "Start connection");

            RetryConnector retryConnector = mRetryConnectorProvider.get();
            retryConnector.connect(params)
                    .continueWith(reportResult())
                    .waitForCompletion();
        } else {
            Log.i(Tag.FAIL, validator.message);
            logUsage();
        }
    }

    @Nonnull
    private Continuation<Void, Void> reportResult() {
        return new Continuation<Void, Void>() {
            @Override
            public Void then(Task task) {
                if (task.isFaulted()) {
                    Log.i(Tag.FAIL, task.getError().getMessage());
                } else {
                    Log.i(Tag.SUCCESS, "Connected");
                }
                return null;
            }
        };
    }

    private void logUsage() {
        Log.d(Tag.NAME, "Enable WiFi on device and connect to WiFi network.\n" +
                "As soon as WiFi network is connected and IP is obtained, " + Tag.SUCCESS +
                " is logged\n" +
                "When connection fails, " + Tag.FAIL + " is logged.\n" +
                "Possible failure reasons: WiFi can not be enabled, " +
                "WiFi network can not be connected, IP can not be obtained." +
                "Usage:\n" +
                "adb shell am broadcast\n" +
                " -n ru.yandex.qatools.wifitool/.Connect\n" +
                " -e " + ParamNames.SSID + " SSID\n" +
                " -e " + ParamNames.SECURITY + " [WEP|WPA]\n" +
                " -e " + ParamNames.PASS + " password \n" +
                " -e " + ParamNames.RETRY_COUNT + " number of connection retries. Default is 0\n" +
                " -e " + ParamNames.RETRY_DELAY + " retry delay in milliseconds. " +
                "Default is 10000\n" +
                "Examples:\n" +
                "adb shell am broadcast " +
                " -n ru.yandex.qatools.wifitool/.Connect" +
                " -e " + ParamNames.SSID + " SecureNet" +
                " -e " + ParamNames.SECURITY + " WPA" +
                " -e " + ParamNames.PASS + " 123456" +
                " -e " + ParamNames.RETRY_COUNT + " 3" +
                " -e " + ParamNames.RETRY_DELAY + " 5\n" +
                "adb shell am broadcast " +
                " -n ru.yandex.qatools.wifitool/.Connect" +
                " -e " + ParamNames.SSID + " UnsecureNet"
        );
    }
}
