package com.mariniu.core.application;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Interface defining the methods that a ContextBuilder must have to provide concrete
 * implementations of {@link ContextWrapper}s.
 * <p/>
 * Created on 09/02/2016.
 *
 * @author Umberto Marini
 */
public interface ContextBuilder {

    /**
     * Set the base context for the ContextWrapper.
     *
     * @param context The new base context for this wrapper.
     * @throws IllegalStateException if a base context has already been set
     */
    void init(Context context) throws IllegalStateException;

    /**
     * Free ContextWrapper.
     */
    void free();

    /**
     * Return the context.
     *
     * @return the {@link Context}
     * @throws IllegalStateException if a base context has not been set before.
     */
    Context getContext() throws IllegalStateException;
}

