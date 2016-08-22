package ru.yandex.qatools.wifitool;

import android.util.Log;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import bolts.Continuation;
import bolts.Task;

class RetryConnector {
    @Nonnull
    private ConnectorFactory mConnectorFactory;
    @Nonnull
    private final Params mParams;
    private int mAttempt = 0;
    private boolean mNeedRetry = true;

    RetryConnector(ConnectorFactory connectorFactory, Params params) {
        mConnectorFactory = connectorFactory;
        mParams = params;
    }

    @Nonnull
    Task<Void> connect() throws InterruptedException {
        Task<Void> task = Task.forResult(null);
        return task.continueWhile(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return canRetry();
            }
        }, new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.d(Tag.NAME, "Attempt " + mAttempt);
                mAttempt++;
                Connector connector = mConnectorFactory.create();
                return connector.connect(mParams)
                        .continueWithTask(new Continuation<Void, Task<Void>>() {
                            @Override
                            public Task<Void> then(Task<Void> task) throws Exception {
                                boolean faulted = task.isFaulted();
                                mNeedRetry = faulted;
                                if (canRetry()) {
                                    Log.d(Tag.NAME, "Attempt failed: " +
                                            task.getError().getMessage());
                                    Log.d(Tag.NAME, "Delay for " + mParams.retryDelay + " ms");
                                    return Task.delay(mParams.retryDelay);
                                } else {
                                    return task;
                                }
                            }
                        });
            }
        });
    }

    private boolean canRetry() {
        return mNeedRetry && mAttempt <= mParams.retryCount;
    }
}
