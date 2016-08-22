package ru.yandex.qatools.wifitool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import junit.framework.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import bolts.CancellationToken;
import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import ru.yandex.qatools.wifitool.utils.SystemServiceLocator;

class ConnectivityChecker {
    @Nonnull
    private final IntentFilter mIntentFilter = new IntentFilter();

    @Nonnull
    private final ConnectivityManager mConnectivityManager;

    @Nonnull
    private final WifiManager mWifiManager;

    @Nonnull
    private Context mContext;

    @Nullable
    private BroadcastReceiver mBroadcastReceiver;

    @Nonnull
    private final TaskCompletionSource<Void> mCompletion = new TaskCompletionSource<>();

    ConnectivityChecker(Context context, SystemServiceLocator serviceLocator) {
        mContext = context;

        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mWifiManager = serviceLocator.getWifiManager();
        mConnectivityManager = serviceLocator.getConnectivityManager();
    }

    @Nonnull
    Continuation<Integer, Task<Void>> checkNetwork(final CancellationToken timeoutToken) {
        return new Continuation<Integer, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Integer> task) throws Exception {
                final int netId = task.getResult();
                if (isWifiNetworkConnected(netId)) {
                    Log.d(Tag.NAME, "Wifi network is connected");
                    return Task.forResult(null);
                }

                register(netId);
                timeoutToken.register(new Runnable() {
                    @Override
                    public void run() {
                        if (mCompletion.getTask().isCompleted()) {
                            return;
                        }
                        Log.d(Tag.NAME, "Connectivity check timed out");
                        mCompletion.setError(new Exception("Connectivity check timed out"));
                    }
                });

                return mCompletion.getTask().continueWithTask(
                        new Continuation<Void, Task<Void>>() {
                            @Override
                            public Task<Void> then(Task<Void> task) throws Exception {
                                unregister();
                                return task;
                            }
                        }
                );
            }
        };
    }

    boolean isWifiNetworkConnected(int netId) {
        NetworkInfo network = mConnectivityManager.getActiveNetworkInfo();
        if (network == null) {
            return false;
        }

        if (network.getType() != ConnectivityManager.TYPE_WIFI) {
            return false;
        }

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.d(Tag.NAME, "Wifi is not connected");
            return false;
        }

        Log.d(Tag.NAME, "Wifi supplicant state: " + wifiInfo.getSupplicantState());

        return network.isConnected() && wifiInfo.getNetworkId() == netId;
    }

    private synchronized void register(final int netId) {
        Log.d(Tag.NAME, "Register network status receiver");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isWifiNetworkConnected(netId)) {
                    Log.d(Tag.NAME, "Network has been connected");
                    mCompletion.trySetResult(null);
                }
            }
        };
        mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private synchronized void unregister() {
        Log.d(Tag.NAME, "Unregister network status receiver");
        Assert.assertNotNull("Receiver must be registered before unregister",
                mBroadcastReceiver);
        mContext.unregisterReceiver(mBroadcastReceiver);
    }
}
