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

package com.mariniu.core.sample;

import android.app.Application;

import com.mariniu.core.application.ContextManager;
import com.mariniu.core.dataprovider.DataProviderManager;
import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.events.rx.RxEventProcessor;
import com.mariniu.core.presenter.PresenterManager;
import com.mariniu.core.sample.base.dataprovider.DataProviderType;
import com.mariniu.core.sample.base.presenter.PresenterType;

/**
 * Created on 28/02/17.
 *
 * @author Umberto Marini
 */
public class MyMainApplicationConfig {

    private static final MyMainApplicationConfig sInstance = new MyMainApplicationConfig();

    public static synchronized MyMainApplicationConfig getInstance() {
        return sInstance;
    }

    public synchronized void onCreate(Application application) {
        initContextManager(application);
        initEventBus();
        initCoordinatorManager();
        initRepositoryManager();
    }

    private void initEventBus() {
        EventDispatcher.useEventProcessor(RxEventProcessor.newInstance());
    }

    private void initCoordinatorManager() {
        PresenterManager.initBuilder(new PresenterType.PresenterCreatorImpl());
    }

    private void initRepositoryManager() {
        DataProviderManager.initBuilder(new DataProviderType.DataProviderCreatorImpl());
    }

    private void initContextManager(Application application) {
        ContextManager.initBuilder(new ContextManager.ContextProvider(application));
    }
}
