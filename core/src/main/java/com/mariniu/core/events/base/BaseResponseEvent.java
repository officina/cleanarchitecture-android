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

package com.mariniu.core.events.base;

import android.support.annotation.StringDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.mariniu.core.LibConfiguration;

/**
 * Created on 30/03/16.
 *
 * @author Umberto Marini
 */
public class BaseResponseEvent {

    public static final String TAG = "BaseResponseEvent";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            BASE_STATUS_OK,
            BASE_STATUS_FAIL
    })
    public @interface Status {
    }

    public static final String BASE_STATUS_OK = "BaseResponseEvent.Status.Ok";
    public static final String BASE_STATUS_FAIL = "BaseResponseEvent.Status.Fail";

    /**
     * The {@link Status} for this {@code BaseResponseEvent}.
     */
    @Status
    private String mEventStatus;

    /**
     * Delegate for this {@code BaseRequestEvent}.
     */
    private EventDelegate mDelegate;

    /**
     * Default constructor.
     */
    public BaseResponseEvent() {
        this.mDelegate = null;
    }

    /**
     * Getter for {@code EventStatus} field.
     *
     * @return The status for this {@code BaseResponseEvent}.
     */
    @Status
    public String getEventStatus() {
        return mEventStatus;
    }

    /**
     * Setter for {@code EventStatus} field.
     *
     * @param status The status for this {@code BaseResponseEvent}.
     */
    public void setEventStatus(@Status String status) {
        this.mEventStatus = status;
    }

    /**
     * Getter for {@code mDelegate} field.
     *
     * @return The delegate for this {@code BaseResponseEvent}.
     */
    public EventDelegate getDelegate() {
        return mDelegate;
    }

    /**
     * Setter for {@code mDelegate} field.
     *
     * @param delegate The delegate for this {@code BaseResponseEvent}.
     */
    public void setDelegate(EventDelegate delegate) {
        mDelegate = delegate;
    }

    // TODO split status check from owner one
    public boolean isValidResponse(String owner) {
        if (mDelegate == null) {
            return BASE_STATUS_OK.equals(mEventStatus);
        } else {
            return BASE_STATUS_OK.equals(mEventStatus) && mDelegate.matchOwnership(owner);
        }
    }

    // TODO split status check from owner one
    public boolean isValidResponse(Class owner) {
        if (mDelegate == null) {
            return BASE_STATUS_OK.equals(mEventStatus);
        } else {
            return BASE_STATUS_OK.equals(mEventStatus) && mDelegate.matchOwnership(owner);
        }
    }

    /**
     * This method copies the given {@code BaseRequestEvent}'s owner to the given {@code BaseResponseEvent}.
     *
     * @param from The {@link BaseRequestEvent} object used as source of the owner copy process.
     * @param to   The {@link BaseResponseEvent} object used as destination of the owner copy process.
     */
    public static void copyOwnership(BaseRequestEvent from, BaseResponseEvent to) {
        if (from != null && to != null) {
            to.setDelegate(from.getDelegate());
        }
    }

    /**
     * This method will make the event based on class passed by parameter with status {@link BaseResponseEvent#BASE_STATUS_OK}
     */
    public static <T extends BaseResponseEvent> T makeOkResponse(Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
            t.setEventStatus(BASE_STATUS_OK);
        } catch (Exception e) {
            if (LibConfiguration.isLoggerEnabled()) {
                Log.w(TAG, e.getMessage());
            }
        }
        return t;
    }

    /**
     * This method will make the event based on class passed by parameter with status {@link BaseResponseEvent#BASE_STATUS_FAIL}
     */
    public static <T extends BaseResponseEvent> T makeFailResponse(Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
            t.setEventStatus(BASE_STATUS_FAIL);
        } catch (Exception e) {
            if (LibConfiguration.isLoggerEnabled()) {
                Log.w(TAG, e.getMessage());
            }
        }
        return t;
    }
}