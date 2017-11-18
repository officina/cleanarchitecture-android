/*
 * Copyright (c) 2016 Umberto Marini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mariniu.core;

import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created on 03/02/16.
 *
 * @author Umberto Marini
 */
public class LibConfiguration {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SILENT_LOGGER, DEFAULT_LOGGER})
    public @interface Logger {
    }

    /**
     * Convenience constant to turn off any log in the Base library.
     */
    public static final int SILENT_LOGGER = 0;

    /**
     * Convenience constant of default {@link Log} implementation of Android platform.
     */
    public static final int DEFAULT_LOGGER = 1;

    @Logger
    private static int sLogger = DEFAULT_LOGGER;

    /**
     * Whether the logger is enabled or it has been turned off.
     *
     * @return {@code true} if log is enabled, {@code false} otherwise
     */
    public static boolean isLoggerEnabled() {
        return sLogger == DEFAULT_LOGGER;
    }

    /**
     * Set the logger to use in Base library.
     *
     * @param logger one {@link Logger} value of {@link #SILENT_LOGGER}, {@link #DEFAULT_LOGGER}
     */
    public static void setLogger(@Logger int logger) {
        sLogger = logger;
    }
}
