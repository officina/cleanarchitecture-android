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

package com.mariniu.core.subscriber;

import rx.Subscriber;

/**
 * Convenience class to extend if you need to create a custom {@link Subscriber} but don't want to override all three methods of it
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class BaseSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {
        //do nothing by default
    }

    @Override
    public void onError(Throwable e) {
        //do nothing by default
    }

    @Override
    public void onNext(T t) {
        //do nothing by default
    }
}
