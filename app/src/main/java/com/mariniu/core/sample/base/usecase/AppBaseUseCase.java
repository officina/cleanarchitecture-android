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

import com.mariniu.core.dataprovider.BaseObservableDataProvider;
import com.mariniu.core.sample.base.dataprovider.DataProviderType;
import com.mariniu.core.sample.base.dataprovider.model.AppDataHolder;
import com.mariniu.core.subscriber.BaseSubscriber;
import com.mariniu.core.usecase.BaseUseCase;

import rx.Scheduler;

/**
 * Created on 28/02/17.
 *
 * @author Umberto Marini
 */
public abstract class AppBaseUseCase<T> extends BaseUseCase<T> {

    protected BaseObservableDataProvider<AppDataHolder> mAppRepo;

    private BaseSubscriber<AppDataHolder> mDefaultAppRepoSubscriber;

    public AppBaseUseCase(Scheduler threadExecutor, Scheduler postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @Override
    protected void initSubscribers() {
        super.initSubscribers();
        mDefaultAppRepoSubscriber = onInitAppBaseRepository();
    }

    protected final BaseSubscriber onInitAppBaseRepository() {
        return new BaseSubscriber<AppDataHolder>() {
            @Override
            public void onNext(AppDataHolder appData) {

            }
        };
    }

    @Override
    protected void initDataProviders() {
        super.initDataProviders();
        mAppRepo = requestDataProvider(DataProviderType.APP);
        if (mAppRepo != null) {
            mAppRepo.addSubscriber(mDefaultAppRepoSubscriber);
        }
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();
        mAppRepo.removeSubscriber(mDefaultAppRepoSubscriber);
        releaseDataProvider(DataProviderType.APP);
    }
}