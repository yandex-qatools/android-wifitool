package ru.yandex.qatools.wifitool;

import android.content.Intent;
import android.net.ConnectivityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.annotation.Nonnull;

import bolts.CancellationToken;
import bolts.CancellationTokenSource;
import bolts.Task;
import ru.yandex.qatools.wifitool.utils.ConnectivityChecker;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectivityMonitorTest {

    @Nonnull
    private final ConnectivityMonitor mConnectivityMonitor;

    @Mock
    private ConnectivityChecker mConnectivityChecker;

    @Mock
    private CancellationToken mCancellationToken;

    public ConnectivityMonitorTest() {
        MockitoAnnotations.initMocks(this);
        mConnectivityMonitor = new ConnectivityMonitor(RuntimeEnvironment.application,
                mConnectivityChecker);
    }

    @Test
    public void whenConnected_Succeeds() throws InterruptedException {
        whenConnected();

        Task<Void> waitTask = mConnectivityMonitor.wait(TestData.NET_ID, mCancellationToken);

        assertSucceeds(waitTask);
    }

    @Test
    public void whenNotConnected_CancelFails() throws InterruptedException {
        whenNotConnected();
        CancellationTokenSource tokenSource = new CancellationTokenSource();

        Task<Void> waitTask = mConnectivityMonitor.wait(TestData.NET_ID, tokenSource.getToken());
        tokenSource.cancel();

        assertFails(waitTask);
    }

    @Test
    public void whenNotConnected_ConnectionBroadcastSucceeds() throws InterruptedException {
        whenNotConnected();

        Task<Void> waitTask = mConnectivityMonitor.wait(TestData.NET_ID, mCancellationToken);

        whenConnected();
        sendBroadcast();

        assertSucceeds(waitTask);
    }

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
        shadowOf(RuntimeEnvironment.application).sendBroadcast(intent);
    }

    private void assertSucceeds(Task<Void> waitTask) throws InterruptedException {
        waitTask.waitForCompletion();
        assertFalse(waitTask.isFaulted());
    }

    private void assertFails(Task<Void> waitTask) throws InterruptedException {
        waitTask.waitForCompletion();
        assertTrue(waitTask.isFaulted());
    }

    private void whenConnected() {
        doReturn(true).when(mConnectivityChecker).isWifiNetworkConnected(TestData.NET_ID);
    }

    private void whenNotConnected() {
        doReturn(false).when(mConnectivityChecker).isWifiNetworkConnected(TestData.NET_ID);
    }

}
