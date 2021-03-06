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

package com.mariniu.core.sample.base.presenter;

import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.events.base.BaseResponseEvent;
import com.mariniu.core.events.rx.annotations.RxSubscribe;
import com.mariniu.core.presenter.BasePresenter;
import com.mariniu.core.sample.base.dagger.components.DaggerAppDataComponent;
import com.mariniu.core.sample.base.events.data.DataRetrieveWelcomeMessageRequestEvent;
import com.mariniu.core.sample.base.events.data.DataRetrieveWelcomeMessageResponseEvent;
import com.mariniu.core.sample.base.usecase.RetrieveWelcomeMessageUC;
import com.mariniu.core.subscriber.BaseSubscriber;

import javax.inject.Inject;

/**
 * Created on 28/02/17.
 *
 * @author Umberto Marini
 */
public class AppDataPresenter extends BasePresenter {

    @Inject
    RetrieveWelcomeMessageUC mRetrieveWelcomeMessageUC;

    public AppDataPresenter() {
        super();
        DaggerAppDataComponent.create().inject(this);
    }

    @Override
    protected void startup() {
        // empty constructor
    }

    @Override
    public void shutdown() {
        super.shutdown();
        mRetrieveWelcomeMessageUC.unsubscribe();
    }

    @RxSubscribe
    public void onConsumeDataRetrieveWelcomeMessageRequestEvent(final DataRetrieveWelcomeMessageRequestEvent request) {
        mRetrieveWelcomeMessageUC.execute(new BaseSubscriber<DataRetrieveWelcomeMessageResponseEvent>() {
            @Override
            public void onNext(DataRetrieveWelcomeMessageResponseEvent responseEvent) {
                super.onNext(responseEvent);
                // forward request owner through response
                BaseResponseEvent.copyOwnership(request, responseEvent);
                EventDispatcher.post(responseEvent);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);

                BaseResponseEvent errorEvent = BaseResponseEvent.makeFailResponse(DataRetrieveWelcomeMessageResponseEvent.class);
                BaseResponseEvent.copyOwnership(request, errorEvent);
                EventDispatcher.post(errorEvent);
            }
        });
    }
}