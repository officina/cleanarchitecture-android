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

package com.mariniu.core.sample.base.usecase;

import android.os.Bundle;

import com.mariniu.core.events.base.BaseResponseEvent;
import com.mariniu.core.sample.base.events.data.DataRetrieveWelcomeMessageResponseEvent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on 28/02/17.
 *
 * @author Umberto Marini
 */
public class RetrieveWelcomeMessageUC extends AppBaseUseCase<DataRetrieveWelcomeMessageResponseEvent> {

    public RetrieveWelcomeMessageUC() {
        super(Schedulers.computation(), AndroidSchedulers.mainThread());
    }

    @Override
    protected Observable<DataRetrieveWelcomeMessageResponseEvent> buildUseCaseObservable(Bundle b) {
        if (mAppRepo == null) {
            return Observable.error(new IllegalArgumentException("Error during reading AppDataRepository. It cannot be null!"));
        }

        return Observable.just(mAppRepo.retrieve(null))
                .map(appDataHolder -> {
                    DataRetrieveWelcomeMessageResponseEvent event = BaseResponseEvent.makeOkResponse(DataRetrieveWelcomeMessageResponseEvent.class);
                    event.setWelcomeMessage(appDataHolder.getWelcomeMessage());
                    return event;
                });
    }
}
