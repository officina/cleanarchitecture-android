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

package com.mariniu.session.base.usecase;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.mariniu.core.dataprovider.BaseObservableDataProvider;
import com.mariniu.core.events.base.BaseResponseEvent;
import com.mariniu.core.subscriber.BaseSubscriber;
import com.mariniu.core.usecase.BaseUseCase;
import com.mariniu.session.base.dataprovider.DataProviderType;
import com.mariniu.session.base.dataprovider.SessionDataProvider;
import com.mariniu.session.base.dataprovider.holder.SessionData;
import com.mariniu.session.base.events.PutSessionDataResponseEvent;

import java.io.Serializable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on 17/11/2016.
 *
 * @author Umberto Marini
 */
public class PutSessionDataUC extends BaseUseCase<PutSessionDataResponseEvent> {

    private static final String BUNDLE_SESSION_ENTRY_KEY = "PutSessionDataUC.Bundle.SessionEntryKey";
    private static final String BUNDLE_SESSION_ENTRY_VALUE = "PutSessionDataUC.Bundle.SessionEntryValue";
    private static final String BUNDLE_SESSION_ENTRY_VALUE_CLASS = "PutSessionDataUC.Bundle.SessionEntryValueClass";

    /**
     * LogCat logger.
     */
    private static final String TAG = "PutSessionDataUC";

    /**
     * Reference to Data holder.
     */
    protected BaseObservableDataProvider<SessionData.SessionEntry> mSessionRepo;

    /**
     * Subscriber to the SessionDataRepository used to know when the underlying data changes.
     */
    private BaseSubscriber<SessionData.SessionEntry> mDefaultSessionRepoSubscriber;

    public PutSessionDataUC() {
        super(Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @Override
    protected void initSubscribers() {
        super.initSubscribers();
        mDefaultSessionRepoSubscriber = onInitSessionDataRepository();
    }

    protected final BaseSubscriber onInitSessionDataRepository() {
        return new BaseSubscriber<SessionData.SessionEntry>() {
            @Override
            public void onNext(SessionData.SessionEntry entry) {
                Log.i(TAG, "SessionData has changed.");
            }
        };
    }

    @Override
    protected void initDataProviders() {
        super.initDataProviders();
        mSessionRepo = requestDataProvider(DataProviderType.SESSION);
        if (mSessionRepo != null) {
            mSessionRepo.addSubscriber(mDefaultSessionRepoSubscriber);
        }
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();
        if (mSessionRepo != null) {
            mSessionRepo.removeSubscriber(mDefaultSessionRepoSubscriber);
        }
        releaseDataProvider(DataProviderType.SESSION);
    }

    @Override
    protected Observable<PutSessionDataResponseEvent> buildUseCaseObservable(Bundle b) {
        if (mSessionRepo == null) {
            return Observable.error(new IllegalStateException("Error during reading SessionDataRepository. It cannot be null!"));
        }

        if (!validateParametersBundle(b)) {
            return Observable.error(new IllegalArgumentException("Error during buildUseCaseObservable Bundle data. There are missing or incorrect parameters!"));
        }

        // get data from bundle and save into session repository
        final String sessionEntryKey = b.getString(BUNDLE_SESSION_ENTRY_KEY);
        final Object sessionEntryValue = b.get(BUNDLE_SESSION_ENTRY_VALUE);
        final Class sessionEntryValueClass = (Class) b.getSerializable(BUNDLE_SESSION_ENTRY_VALUE_CLASS);
        mSessionRepo.save(new SessionData.SessionEntry(sessionEntryKey, sessionEntryValue));

        // check if value has been saved correctly
        Bundle filters = SessionDataProvider.createParametersBundle(sessionEntryKey, sessionEntryValueClass);
        SessionData.SessionEntry entry = mSessionRepo.retrieve(filters);

        PutSessionDataResponseEvent responseEvent = BaseResponseEvent.makeOkResponse(PutSessionDataResponseEvent.class);
        responseEvent.setSaved(entry != null && entry.getValue().equals(sessionEntryValue));
        return Observable.just(responseEvent);
    }

    /**
     * Create a bundle to execute this use case.
     *
     * @param sessionEntryKey        The key of session data entry. One of keys declared in {@link SessionData.Constants}.
     * @param sessionEntryValue      The value of session data entry.
     * @param sessionEntryValueClass The class of session data entry value object.
     * @return the {@link Bundle} created with the given parameter.
     */
    public static Bundle createParametersBundle(String sessionEntryKey, Object sessionEntryValue, Class sessionEntryValueClass) {
        Bundle bundle = new Bundle(3);
        bundle.putString(BUNDLE_SESSION_ENTRY_KEY, sessionEntryKey);
        bundle.putSerializable(BUNDLE_SESSION_ENTRY_VALUE_CLASS, sessionEntryValueClass);
        if (sessionEntryValue != null) {
            Class valueClass = sessionEntryValue.getClass();
            if (Serializable.class.isAssignableFrom(valueClass)) {
                bundle.putSerializable(BUNDLE_SESSION_ENTRY_VALUE, (Serializable) sessionEntryValue);
            } else if (Parcelable.class.isAssignableFrom(valueClass)) {
                bundle.putParcelable(BUNDLE_SESSION_ENTRY_VALUE, (Parcelable) sessionEntryValue);
            } else if (Long.class.isAssignableFrom(valueClass)) {
                bundle.putLong(BUNDLE_SESSION_ENTRY_VALUE, (Long) sessionEntryValue);
            } else if (Integer.class.isAssignableFrom(valueClass)) {
                bundle.putInt(BUNDLE_SESSION_ENTRY_VALUE, (Integer) sessionEntryValue);
            } else if (Float.class.isAssignableFrom(valueClass)) {
                bundle.putInt(BUNDLE_SESSION_ENTRY_VALUE, (Integer) sessionEntryValue);
            } else if (Double.class.isAssignableFrom(valueClass)) {
                bundle.putDouble(BUNDLE_SESSION_ENTRY_VALUE, (Double) sessionEntryValue);
            } else {
                throw new RuntimeException("Cannot put value object into bundle. Unrecognized class '" + valueClass.getSimpleName() + "'");
            }
        } else {
            bundle.putString(BUNDLE_SESSION_ENTRY_VALUE, null);
        }
        return bundle;
    }

    /**
     * Whether the given {@code Bundle} contains whole data needed for this UC computation.
     *
     * @param b The input {@link Bundle} of {@link #buildUseCaseObservable(Bundle)} mehtod.
     * @return {@code true} if bundle is valid, {@code false} otherwise
     */
    private boolean validateParametersBundle(Bundle b) {
        return b != null && !TextUtils.isEmpty(b.getString(BUNDLE_SESSION_ENTRY_KEY)) && b.get(BUNDLE_SESSION_ENTRY_VALUE) != null && b.get(BUNDLE_SESSION_ENTRY_VALUE_CLASS) != null;
    }
}
