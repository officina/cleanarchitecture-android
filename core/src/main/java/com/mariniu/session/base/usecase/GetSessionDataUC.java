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
import android.text.TextUtils;
import android.util.Log;

import com.mariniu.core.dataprovider.BaseObservableDataProvider;
import com.mariniu.core.events.base.BaseResponseEvent;
import com.mariniu.core.subscriber.BaseSubscriber;
import com.mariniu.core.usecase.BaseUseCase;
import com.mariniu.session.base.dataprovider.DataProviderType;
import com.mariniu.session.base.dataprovider.SessionDataProvider;
import com.mariniu.session.base.dataprovider.holder.SessionData;
import com.mariniu.session.base.events.GetSessionDataResponseEvent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on 17/11/2016.
 *
 * @author Umberto Marini
 */
public class GetSessionDataUC extends BaseUseCase<GetSessionDataResponseEvent> {

    private static final String BUNDLE_SESSION_ENTRY_KEY = "GetSessionDataUC.Bundle.SessionEntryKey";
    private static final String BUNDLE_SESSION_ENTRY_VALUE_CLASS = "GetSessionDataUC.Bundle.SessionEntryValueClass";

    /**
     * LogCat logger.
     */
    private static final String TAG = "GetSessionDataUC";

    /**
     * Reference to Data holder.
     */
    protected BaseObservableDataProvider<SessionData.SessionEntry> mSessionRepo;

    /**
     * Subscriber to the SessionDataRepository used to know when the underlying data changes.
     */
    private BaseSubscriber<SessionData.SessionEntry> mDefaultSessionRepoSubscriber;

    public GetSessionDataUC() {
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
    protected Observable<GetSessionDataResponseEvent> buildUseCaseObservable(Bundle b) {
        if (mSessionRepo == null) {
            return Observable.error(new IllegalStateException("Error during reading SessionDataRepository. It cannot be null!"));
        }

        if (!validateParametersBundle(b)) {
            return Observable.error(new IllegalArgumentException("Error during buildUseCaseObservable Bundle data. There are missing or incorrect parameters!"));
        }

        // get data from bundle and save into session repository
        final String sessionEntryKey = b.getString(BUNDLE_SESSION_ENTRY_KEY);
        final Class sessionEntryValueClass = (Class) b.getSerializable(BUNDLE_SESSION_ENTRY_VALUE_CLASS);

        // check if value has been saved correctly
        Bundle filters = SessionDataProvider.createParametersBundle(sessionEntryKey, sessionEntryValueClass);
        SessionData.SessionEntry entry = mSessionRepo.retrieve(filters);

        GetSessionDataResponseEvent responseEvent = BaseResponseEvent.makeOkResponse(GetSessionDataResponseEvent.class);
        responseEvent.setSessionEntryValue(entry != null ? entry.getValue() : null);
        return Observable.just(responseEvent);
    }

    /**
     * Create a bundle to execute this use case.
     *
     * @param sessionEntryKey        The key of session data entry. One of keys declared in {@link SessionData.Constants}.
     * @param sessionEntryValueClass The expected class of session data entry value object.
     * @return the {@link Bundle} created with the given parameter.
     */
    public static Bundle createParametersBundle(String sessionEntryKey, Class sessionEntryValueClass) {
        Bundle bundle = new Bundle(2);
        bundle.putString(BUNDLE_SESSION_ENTRY_KEY, sessionEntryKey);
        bundle.putSerializable(BUNDLE_SESSION_ENTRY_VALUE_CLASS, sessionEntryValueClass);
        return bundle;
    }

    /**
     * Whether the given {@code Bundle} contains whole data needed for this UC computation.
     *
     * @param b The input {@link Bundle} of {@link #buildUseCaseObservable(Bundle)} mehtod.
     * @return {@code true} if bundle is valid, {@code false} otherwise
     */
    private boolean validateParametersBundle(Bundle b) {
        return b != null && !TextUtils.isEmpty(b.getString(BUNDLE_SESSION_ENTRY_KEY)) && b.get(BUNDLE_SESSION_ENTRY_VALUE_CLASS) != null;
    }
}
