package ru.yandex.qatools.wifitool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import bolts.CancellationToken;
import bolts.Task;
import bolts.TaskCompletionSource;
import ru.yandex.qatools.wifitool.utils.ConnectivityChecker;

import static junit.framework.Assert.assertNotNull;

/**
 * Waits for network to get connected.
 * Checks connectivity on ConnectivityManager and WifiManager broadcasts.
 */
class ConnectivityMonitor {
    @Nonnull
    private final IntentFilter mIntentFilter = new IntentFilter();

    @Nonnull
    private final Context mContext;

    @Nonnull
    private final ConnectivityChecker mConnectivityChecker;

    @Nullable
    private BroadcastReceiver mBroadcastReceiver;

    @Nonnull
    private final TaskCompletionSource<Void> mCompletion = new TaskCompletionSource<>();

    @Inject
    ConnectivityMonitor(Context context, ConnectivityChecker connectivityChecker) {
        mContext = context;
        mConnectivityChecker = connectivityChecker;

        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    /**
     * Wait for network to get connectivity.
     */
    @Nonnull
    Task<Void> wait(final int netId, final CancellationToken timeoutToken) {
        if (mConnectivityChecker.isWifiNetworkConnected(netId)) {
            Log.d(Tag.NAME, "Wifi network is connected");
            return Task.forResult(null);
        }

        register(netId);
        timeoutToken.register(() -> {
            if (mCompletion.getTask().isCompleted()) {
                return;
            }
            Log.d(Tag.NAME, "Connectivity check timed out");
            mCompletion.setError(new Exception("Connectivity check timed out"));
        });

        return mCompletion.getTask().continueWithTask(
                task -> {
                    unregister();
                    return task;
                }
        );
    }

    private synchronized void register(final int netId) {
        Log.d(Tag.NAME, "Register network status receiver");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mConnectivityChecker.isWifiNetworkConnected(netId)) {
                    Log.d(Tag.NAME, "Network has been connected");
                    mCompletion.trySetResult(null);
                }
            }
        };
        mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private synchronized void unregister() {
        Log.d(Tag.NAME, "Unregister network status receiver");
        assertNotNull("Receiver must be registered before unregister",
                mBroadcastReceiver);
        mContext.unregisterReceiver(mBroadcastReceiver);
    }
}
