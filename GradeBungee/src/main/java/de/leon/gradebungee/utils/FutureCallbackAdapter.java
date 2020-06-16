package de.leon.gradebungee.utils;

import com.google.common.util.concurrent.FutureCallback;
import de.leon.gradebungee.GradeBungee;

/**
 * Internal use only!
 */
public abstract class FutureCallbackAdapter<V> implements FutureCallback<V> {

    @Override
    public void onFailure(Throwable throwable) {
        GradeBungee.logError("An Error occurred in a Callback (" + throwable.getMessage() + ")");
        throwable.printStackTrace();
    }
}
