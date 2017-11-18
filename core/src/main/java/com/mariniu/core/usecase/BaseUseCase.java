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

package com.mariniu.core.usecase;

import android.os.Bundle;

import com.mariniu.core.dataprovider.BaseObservableDataProvider;
import com.mariniu.core.dataprovider.DataProviderManager;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * A UseCase represents and performs a single, atomic work unit.
 *
 * Inspiration taken from <a href="https://github.com/richardradics/RxAndroidBootstrap/blob/master/core/src/main/java/com/richardradics/core/interactor/UseCase.java">here</a>.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class BaseUseCase<T> {

    /**
     * Indicates on what thread to perform our work
     */
    private final Scheduler mThreadExecutor;
    /**
     * Indicates on what thread to give back the result of our work
     */
    private final Scheduler mPostExecutionThread;

    /**
     * A Subscription is a convenience object used to be able to unsubscribe from an observable.
     */
    protected Subscription mSubscription = Subscriptions.empty();

    /**
     * Will setup the 'execution' Scheduler and the 'emission' Scheduler to use.
     * Then it will perform two operations:
     * <ul>
     * <li>initialization of Subscribers used to observe the DataProviders used. See {@link #initSubscribers()}</li>
     * <li>
     * initialization of DataProviders used by this use case by means of:
     * <ul>request of the DataProvider: See {@link #requestDataProvider(int)}</ul>
     * <ul>if the desired DataProvider has been received, attach subscribers to it</ul>
     * </li>
     * </ul>
     *
     * @param threadExecutor      the {@link Scheduler} on which to execute the work
     * @param postExecutionThread the {@link Scheduler} on which to emit notifications
     */
    public BaseUseCase(Scheduler threadExecutor, Scheduler postExecutionThread) {
        this.mThreadExecutor = threadExecutor;
        this.mPostExecutionThread = postExecutionThread;
        initSubscribers();
        initDataProviders();
    }

    /**
     * Initializes all the subscribers this Use Case needs.
     * Called before {@link #initDataProviders()}.
     * <p/>
     * <p>
     * If your UseCase needs to subscribe to changes in DataProviders, initialize such Subscribers here.
     * </p>
     */
    protected void initSubscribers() {
        //do nothing by default
    }

    /**
     * Requests the DataProvider of the desired type and returns it
     *
     * @param type the {@code DataProviderType} of the DataProvider requested
     * @return the DataProvider of the desired type or <code>null</code> if unavailable
     */
    protected BaseObservableDataProvider requestDataProvider(int type) {
        return DataProviderManager.request(type);
    }

    /**
     * Releases the DataProvider of the desired type
     *
     * @param type the {@code DataProviderType} of the DataProvider to be released
     */
    protected void releaseDataProvider(int type) {
        DataProviderManager.release(type);
    }

    /**
     * Initializes all the DataProviders this Use Case needs.
     * Moreover, if needed, it adds the necessary Subscribers to them.
     */
    protected void initDataProviders() {
        //do nothing by default
    }

    /**
     * Builds an {@link Observable} which will be used when executing the current {@link BaseUseCase}.
     */
    protected abstract Observable<T> buildUseCaseObservable(Bundle b);

    /**
     * Executes the current use case.
     *
     * @param useCaseSubscriber The guy who will be listen to the observable build with {@link #buildUseCaseObservable(Bundle)}.
     */
    @SuppressWarnings("unchecked")
    public void execute(Subscriber<T> useCaseSubscriber) {
        execute(useCaseSubscriber, null);
    }

    /**
     * Executes the current use case.
     *
     * @param useCaseSubscriber The guy who will be listen to the observable build with {@link #buildUseCaseObservable(Bundle)}.
     * @param b                 A bundle containing parameters needed to generate the Observable
     */
    @SuppressWarnings("unchecked")
    public void execute(Subscriber<T> useCaseSubscriber, Bundle b) {
        if (useCaseSubscriber != null) {
            this.mSubscription = mThreadExecutor.createWorker().schedule(() -> buildUseCaseObservable(b).observeOn(mPostExecutionThread).subscribe(useCaseSubscriber));
        }
    }

    /**
     * This method will return the {@link Scheduler} on which to execute the work.
     *
     * @return the {@link Scheduler} set via class constructor.
     */
    public Scheduler getThreadExecutor() {
        return mThreadExecutor;
    }

    /**
     * This method will return the {@link Scheduler} on which to emit notifications.
     *
     * @return the {@link Scheduler} set via class constructor.
     */
    public Scheduler getPostExecutionThread() {
        return mPostExecutionThread;
    }

    /**
     * Unsubscribes from current {@link Subscription}.
     * <p>
     * Subclasses should override this method to remove all the Subscribers added to (if any)
     * DataProvider they hold a reference to, and then release such DataProviders.
     * </p>
     */
    public void unsubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }
}
