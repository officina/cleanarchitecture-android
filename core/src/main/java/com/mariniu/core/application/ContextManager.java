package com.mariniu.core.application;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created on 09/02/2016.
 *
 * @author Umberto Marini
 */
public class ContextManager {

    private static final String LOG_TAG = "ContextManager";

    private static ContextBuilder builder;

    /**
     * Initialization method that users of this Manager <b>must</b> call if they want it to use Context reference in future.
     *
     * @param builder an implementation of {@link ContextBuilder} used to produce the desired {@link Context}
     */
    public static synchronized void initBuilder(ContextBuilder builder) {
        ContextManager.builder = builder;
    }

    /**
     * Return the reference to the global Context setted via {@link #initBuilder(ContextBuilder)}.
     *
     * @return the reference to the {@link Context}
     *
     * @throws IllegalStateException if {@link ContextBuilder} has never been initialized
     */
    public static synchronized Context obtainContext() {
        if (builder == null) {
            throw new IllegalStateException("ContextBuilder is null. Call initBuilder(ContextBuilder) to initialize the ContextManager.");
        }
        return builder.getContext();
    }

    /**
     * Set the {@link ContextWrapper} of the builder to null, avoiding memory leaks.
     */
    public static synchronized void freeContext() {
        if (builder != null) {
            builder.free();
        }
    }

    public static class ContextProvider implements ContextBuilder {

        private ContextWrapper contextWrapper;

        public ContextProvider(Context context) {
            init(context);
        }

        @Override
        public void init(Context context) {
            if (contextWrapper != null) {
                throw new IllegalStateException("Context already set. This invocation of init(Context) will be ignored");
            }
            contextWrapper = new ContextWrapper(context);
        }

        @Override
        public void free() {
            if (contextWrapper != null) {
                contextWrapper = null;
            }
        }

        @Override
        public Context getContext() {
            if (contextWrapper == null) {
                throw new IllegalStateException("Context is null. Call init(Context) to initialize the ContextWrapper.");
            }
            return contextWrapper.getApplicationContext();
        }
    }
}

