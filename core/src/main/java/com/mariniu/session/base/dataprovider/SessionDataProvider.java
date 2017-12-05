/*
 * Copyright (C) 2017 Umberto Marini.
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

package com.mariniu.session.base.dataprovider;

import android.os.Bundle;

import com.mariniu.core.application.ContextManager;
import com.mariniu.core.dataprovider.BaseObservableDataProvider;
import com.mariniu.session.base.dataprovider.holder.SessionData;
import com.mariniu.session.utils.CacheUtils;

import rx.Subscriber;

/**
 * Created on 17/11/2016.
 *
 * @author Umberto Marini
 */
public class SessionDataProvider extends BaseObservableDataProvider<SessionData.SessionEntry> {

    private static final String BUNDLE_FILTER_SESSION_ENTRY_KEY = "SessionDataRepository.Filter.SessionEntryKey";
    private static final String BUNDLE_FILTER_SESSION_ENTRY_VALUE_CLASS = "SessionDataRepository.Filter.SessionEntryValueClass";

    private static SessionData sSessionData;
    private static SessionData.SessionEntry sLastSessionDataEntry;

    public static final OnSubscribe<SessionData.SessionEntry> ONSUBSCRIBE = new OnSubscribe<SessionData.SessionEntry>() {
        @Override
        public void call(Subscriber<? super SessionData.SessionEntry> subscriber) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(sLastSessionDataEntry);
                subscriber.onCompleted();
            }
        }
    };

    public SessionDataProvider(OnSubscribe<SessionData.SessionEntry> f) {
        super(f);

        // initialize Cache
        if (!CacheUtils.isInitialized()) {
            CacheUtils.init(ContextManager.obtainContext());
        }
    }

    @Override
    public void save(SessionData.SessionEntry entry) {
        if (entry == null) {
            return;
        }

        // initialize SessionData if needed
        if (sSessionData == null) {
            sSessionData = new SessionData();
        }

        sSessionData.put(entry);

        // save SessionData to Reservoir cache
        if (CacheUtils.isInitialized()) {
            CacheUtils.put(CacheUtils.Keys.KEY_SESSION_DATA, sSessionData);
        }

        notifyObservers();
    }

    @Override
    public SessionData.SessionEntry retrieve(Bundle filters) {
        // initialize SessionData if needed
        if (sSessionData == null) {

            // retrieve AppData from Reservoir cache
            if (CacheUtils.isInitialized()) {
                if (CacheUtils.contains(CacheUtils.Keys.KEY_SESSION_DATA)) {
                    sSessionData = CacheUtils.get(CacheUtils.Keys.KEY_SESSION_DATA, SessionData.class);
                }
            }

            if (sSessionData == null) {
                sSessionData = new SessionData();
            }
        }

        SessionData.SessionEntry sessionEntryFound = null;
        if (filters != null && filters.containsKey(BUNDLE_FILTER_SESSION_ENTRY_KEY)) {
            String sessionEntryKey = filters.getString(BUNDLE_FILTER_SESSION_ENTRY_KEY);
            Class sessionEntryValueClass = (Class) filters.getSerializable(BUNDLE_FILTER_SESSION_ENTRY_VALUE_CLASS);
            sessionEntryFound = sSessionData.get(sessionEntryKey, sessionEntryValueClass);

            if(sessionEntryFound != null){
                sLastSessionDataEntry = sessionEntryFound;
            }
        }

        return sessionEntryFound;
    }

    @Override
    public void transform(Bundle transformationProperties) {
        // empty implementation
    }

    @Override
    public boolean clear(Bundle clearProperties) {
        // TODO call clear when app is started
        if (sSessionData != null) {
            sSessionData.clear();
            sSessionData = null;
        }

        // clear cache
        if (CacheUtils.isInitialized()) {
            CacheUtils.delete(CacheUtils.Keys.KEY_SESSION_DATA);
        }

        return true;
    }

    /**
     * Create a bundle containing session data key to retrieve the related
     * {@code SessionData.SessionEntry} object using {@link #retrieve(Bundle)} method.
     *
     * @param sessionEntryKey        The key of session data entry. One of keys declared in {@link SessionData.Constants}.
     * @param sessionEntryValueClass The class of the value expected.
     * @return the {@link Bundle} created with the given parameter.
     */
    public static Bundle createParametersBundle(String sessionEntryKey, Class sessionEntryValueClass) {
        Bundle bundle = new Bundle(2);
        bundle.putString(BUNDLE_FILTER_SESSION_ENTRY_KEY, sessionEntryKey);
        bundle.putSerializable(BUNDLE_FILTER_SESSION_ENTRY_VALUE_CLASS, sessionEntryValueClass);
        return bundle;
    }
}
