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

/**
 * Created on 30/06/16.
 *
 * @author Umberto Marini
 */
public class BaseRequestEvent {

    /**
     * Delegate for this {@code BaseRequestEvent}.
     */
    private EventDelegate mDelegate;

    /**
     * Constructor.
     *
     * @param owner The identifier of the delegate to set for this {@code BaseRequestEvent}.
     */
    public BaseRequestEvent(String owner) {
        this.mDelegate = new EventDelegate(owner);
    }

    /**
     * Constructor.
     *
     * @param owner The class of the delegate to set for this {@code BaseRequestEvent}.
     */
    public BaseRequestEvent(Class owner) {
        this.mDelegate = new EventDelegate(owner);
    }

    /**
     * Getter for {@code EventDelegate} field.
     *
     * @return The delegate for this {@code BaseRequestEvent}.
     */
    public EventDelegate getDelegate() {
        return mDelegate;
    }

    /**
     * Setter for {@code EventDelegate} field.
     *
     * @param delegate The delegate for this {@code BaseRequestEvent}.
     */
    public void setDelegate(EventDelegate delegate) {
        mDelegate = delegate;
    }

    // TODO split status check from owner one
    public boolean isValidRequest(String owner) {
        if (mDelegate == null) {
            return owner == null;
        } else {
            return mDelegate.matchOwnership(owner);
        }
    }

    // TODO split status check from owner one
    public boolean isValidRequest(Class owner) {
        if (mDelegate == null) {
            return owner == null;
        } else {
            return mDelegate.matchOwnership(owner);
        }
    }
}