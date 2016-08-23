package ru.yandex.qatools.wifitool;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import bolts.Task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.wifitool.TestData.NO_PASS;
import static ru.yandex.qatools.wifitool.TestData.NO_SECURITY;
import static ru.yandex.qatools.wifitool.TestData.SOME_SSID;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetryConnectorTest {

    private Provider<Connector> mConnectorProvider;

    @Mock
    private Connector mConnector;

    @Mock
    private Context mContext;

    public RetryConnectorTest() {
        MockitoAnnotations.initMocks(this);

        // mockito fails to mock javax.inject.Provider
        mConnectorProvider = new Provider<Connector>() {
            @Override
            public Connector get() {
                return mConnector;
            }
        };
    }

    @Test
    public void zeroRetries_Connects() throws InterruptedException {
        Params params = getRetryParams(0);
        getRetryConnector().connect(params).waitForCompletion();
        verify(mConnector).connect(any(Params.class));
    }

    @Nonnull
    private RetryConnector getRetryConnector() {
        return new RetryConnector(mConnectorProvider);
    }

    @Test
    public void oneRetry_OnSuccessfulConnect_Connects1Time() throws InterruptedException {
        whenConnectionSucceed();
        Params params = getRetryParams(1);
        getRetryConnector().connect(params).waitForCompletion();
        verify(mConnector).connect(any(Params.class));
    }

    private void whenConnectionSucceed() {
        Task<Object> successfulTask = Task.forResult(null);
        doReturn(successfulTask).when(mConnector).connect(any(Params.class));
    }

    @Test
    public void oneRetry_OnFailedConnect_Connects2Times() throws InterruptedException {
        whenConnectionFail();
        Params params = getRetryParams(1);
        getRetryConnector().connect(params).waitForCompletion();
        verify(mConnector, times(2)).connect(any(Params.class));
    }

    private void whenConnectionFail() {
        Task<Object> faultedTask = Task.forError(new Exception());
        doReturn(faultedTask).when(mConnector).connect(any(Params.class));
    }

    @Nonnull
    private Params getRetryParams(int retryCount) {
        return new Params(SOME_SSID, NO_PASS, NO_SECURITY, retryCount, 100);
    }
}
