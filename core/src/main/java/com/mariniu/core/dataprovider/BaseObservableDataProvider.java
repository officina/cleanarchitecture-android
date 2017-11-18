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

package com.mariniu.core.dataprovider;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Transforms a basic {@link DataProvider} into an Observable one.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class BaseObservableDataProvider<T> extends Observable<T> implements DataProvider<T> {

    /**
     * List of attached Subscriber to notify when something changes in our internals
     */
    protected List<Subscriber<T>> mSubscribers = new ArrayList<>();

    /**
     * Function called to notify the Subscribers
     */
    protected OnSubscribe<T> mOS;

    /**
     * Creates an Observable with a Function to execute when it is subscribed to.
     * <p>
     * <em>Note:</em> Use {@link #create(OnSubscribe)} to create an Observable, instead of this constructor,
     * unless you specifically have a need for inheritance.
     * </p>
     *
     * @param f {@link OnSubscribe} to be executed when {@link #subscribe(Subscriber)} is called
     */
    protected BaseObservableDataProvider(OnSubscribe<T> f) {
        super(f);
        mOS = f;
    }

    /**
     * Adds a Subscriber to the internal list (if not already present), so that it'll be notify when something changes
     *
     * @param f a Subscriber that wants to be notified when things change
     */
    public void addSubscriber(Subscriber<T> f) {
        if (mSubscribers != null && !mSubscribers.contains(f)) {
            mSubscribers.add(f);
        }
    }

    /**
     * Unsubscribes and removes a Subscriber from the internal list
     *
     * @param f the Subscriber to remove
     */
    public void removeSubscriber(Subscriber<T> f) {
        if (mSubscribers != null && mSubscribers.contains(f)) {
            int subscriberPosition = mSubscribers.indexOf(f);
            if (subscriberPosition >= 0) {
                Subscriber<T> s = mSubscribers.get(subscriberPosition);
                if (s != null && !s.isUnsubscribed()) {
                    s.unsubscribe();
                    mSubscribers.remove(subscriberPosition);
                }
            }
        }
    }

    /**
     * Unsubscribes and removes every Subscriber we have in our internal list
     */
    public void removeAllSubscribers() {
        if (mSubscribers != null && !mSubscribers.isEmpty() && mOS != null) {
            for (int i = mSubscribers.size() - 1; i >= 0; i--) {
                Subscriber<T> s = mSubscribers.get(i);
                removeSubscriber(s);
            }
        }
    }

    /**
     * Calls every observer subscribed to this Observable Repository
     */
    protected void notifyObservers() {
        if (mSubscribers != null && mOS != null) {
            for (Subscriber<T> s : mSubscribers) {
                if (s != null && !s.isUnsubscribed()) {
                    mOS.call(s);
                }
            }
        }
    }

    /**
     * Empty implementation of {@link DataProvider#clear(Bundle)} method added in v.0.0.12.
     * In this way, we avoid the chain propagation of method implementation request to all projects
     * uses {@code BusinessLogic} library as own dependency.
     */
    @Override
    public boolean clear(Bundle clearProperties) {
        return false;
    }
}
