package ru.yandex.qatools.wifitool;

import android.util.Log;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

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
        return task.continueWhile(
                () -> canRetry(params),
                attempt -> {
                    Log.d(Tag.NAME, "Attempt " + mAttempt);
                    mAttempt++;
                    Connector connector = mConnectorFactory.get();
                    return connector.connect(params)
                            .continueWithTask(connection -> {
                                boolean faulted = connection.isFaulted();
                                mNeedRetry = faulted;
                                if (canRetry(params)) {
                                    Log.d(Tag.NAME, "Attempt failed: " +
                                            connection.getError().getMessage());
                                    Log.d(Tag.NAME, "Delay for " + params.retryDelay + " ms");
                                    return Task.delay(params.retryDelay);
                                } else {
                                    return connection;
                                }
                            });
                }
        );
    }

    private boolean canRetry(Params params) {
        return mNeedRetry && mAttempt <= params.retryCount;
    }
}
