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

package com.mariniu.session.base.presenter;

import android.util.Log;

import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.events.base.BaseResponseEvent;
import com.mariniu.core.events.rx.annotations.RxSubscribe;
import com.mariniu.core.presenter.BasePresenter;
import com.mariniu.core.subscriber.BaseSubscriber;
import com.mariniu.session.base.dagger.components.DaggerSessionDataComponent;
import com.mariniu.session.base.events.GetSessionDataRequestEvent;
import com.mariniu.session.base.events.GetSessionDataResponseEvent;
import com.mariniu.session.base.events.PutSessionDataRequestEvent;
import com.mariniu.session.base.events.PutSessionDataResponseEvent;
import com.mariniu.session.base.usecase.GetSessionDataUC;
import com.mariniu.session.base.usecase.PutSessionDataUC;

import javax.inject.Inject;

/**
 * Created on 17/11/2016.
 *
 * @author Umberto Marini
 */
public class SessionPresenter extends BasePresenter {

    private static final String TAG = "SessionPresenter";

    @Inject
    PutSessionDataUC mPutSessionDataUC;

    @Inject
    GetSessionDataUC mGetSessionDataUC;

    public SessionPresenter() {
        super();
        DaggerSessionDataComponent.create().inject(this);
    }

    @Override
    protected void startup() {
    }

    @Override
    public void shutdown() {
        super.shutdown();
        mPutSessionDataUC.unsubscribe();
        mGetSessionDataUC.unsubscribe();
    }

    /**
     * This method will listen to a {@code PutSessionDataRequestEvent} to put a value into app session data.
     * To satisfy this request it'll use a {@link PutSessionDataUC} and will emit a {@link PutSessionDataResponseEvent}.
     *
     * @param request The {@link PutSessionDataRequestEvent} containing value to put into app session data.
     */
    @RxSubscribe
    public void onConsumeEvent(final PutSessionDataRequestEvent request) {
        Log.d(TAG, "received PutSessionDataRequestEvent");
        mPutSessionDataUC.execute(new BaseSubscriber<PutSessionDataResponseEvent>() {
            @Override
            public void onNext(PutSessionDataResponseEvent response) {
                super.onNext(response);
                BaseResponseEvent.copyOwnership(request, response);
                EventDispatcher.post(response);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);

                PutSessionDataResponseEvent errorEvent = BaseResponseEvent.makeFailResponse(PutSessionDataResponseEvent.class);
                BaseResponseEvent.copyOwnership(request, errorEvent);

                EventDispatcher.post(errorEvent);
            }
        }, PutSessionDataUC.createParametersBundle(request.getSessionEntryKey(), request.getSessionEntryValue(), request.getSessionEntryValueClass()));
    }

    /**
     * This method will listen to a {@code GetSessionDataRequestEvent} to get a value from app session data.
     * To satisfy this request it'll use a {@link GetSessionDataUC} and will emit a {@link GetSessionDataResponseEvent}.
     *
     * @param request The {@link GetSessionDataRequestEvent} containing value to put into app session data.
     */
    @RxSubscribe
    public void onConsumeEvent(final GetSessionDataRequestEvent request) {
        Log.d(TAG, "received GetSessionDataRequestEvent");
        mGetSessionDataUC.execute(new BaseSubscriber<GetSessionDataResponseEvent>() {
            @Override
            public void onNext(GetSessionDataResponseEvent response) {
                super.onNext(response);
                BaseResponseEvent.copyOwnership(request, response);
                EventDispatcher.post(response);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);

                PutSessionDataResponseEvent errorEvent = BaseResponseEvent.makeFailResponse(PutSessionDataResponseEvent.class);
                BaseResponseEvent.copyOwnership(request, errorEvent);

                EventDispatcher.post(errorEvent);
            }
        }, GetSessionDataUC.createParametersBundle(request.getSessionEntryKey(), request.getSessionEntryValueClass()));
    }
}