package ru.yandex.qatools.wifitool;

import android.util.Log;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import bolts.Continuation;
import bolts.Task;

class RetryConnector {
    @Nonnull
    private Provider<Connector> mConnectorFactory;
    private int mAttempt = 0;
    private boolean mNeedRetry = true;

    @Inject
    RetryConnector(Provider<Connector> connectorProvider) {
        mConnectorFactory = connectorProvider;
    }

    @Nonnull
    Task<Void> connect(final Params params) throws InterruptedException {
        Task<Void> task = Task.forResult(null);
        return task.continueWhile(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return canRetry(params);
            }
        }, new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.d(Tag.NAME, "Attempt " + mAttempt);
                mAttempt++;
                Connector connector = mConnectorFactory.get();
                return connector.connect(params)
                        .continueWithTask(new Continuation<Void, Task<Void>>() {
                            @Override
                            public Task<Void> then(Task<Void> task) throws Exception {
                                boolean faulted = task.isFaulted();
                                mNeedRetry = faulted;
                                if (canRetry(params)) {
                                    Log.d(Tag.NAME, "Attempt failed: " +
                                            task.getError().getMessage());
                                    Log.d(Tag.NAME, "Delay for " + params.retryDelay + " ms");
                                    return Task.delay(params.retryDelay);
                                } else {
                                    return task;
                                }
                            }
                        });
            }
        });
    }

    private boolean canRetry(Params params) {
        return mNeedRetry && mAttempt <= params.retryCount;
    }
}
