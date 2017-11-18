package com.mariniu.core.events.rx;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import com.mariniu.core.events.rx.annotations.RxAnnotatedHandlerFinder;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * This class implements the Event Bus pattern using RxJava {@link SerializedSubject}.
 * The subject serialize a wrapped subject, the wrapped object depends on constructor called.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public class RxBus<T, R> extends SerializedSubject<T, R> {

    /**
     * This map handle the subscriptions list, keys contains {@link Observer}, values contains {@link Subscription}
     */
    private final Map<Observer, Subscription> mSubscriptionsList;

    /**
     * With this constructor the {@link RxBus} use a {@link PublishSubject} and instantiate it calling the static method {@code PublishSubject.create()}
     *
     * @see PublishSubject
     */
    public RxBus() {
        super((Subject<T, R>) PublishSubject.create());
        mSubscriptionsList = new WeakHashMap<>();
    }

    /**
     * With this constructor the {@link RxBus} use a {@link ReplaySubject} and instantiate it calling the static method {@code ReplaySubject.createWithSize(cacheSize)}
     *
     * @param cacheSize numbers of max events retained
     * @see ReplaySubject
     */
    public RxBus(int cacheSize) {
        super((Subject<T, R>) ReplaySubject.createWithSize(cacheSize));
        mSubscriptionsList = new WeakHashMap<>();
    }

    /**
     * With this constructor the {@link RxBus} use a {@link ReplaySubject} and instantiate it calling the static method  {@code ReplaySubject.createWithTime(retainTime, timeUnit, scheduler)}
     *
     * @param retainTime events retain time, passed to {@link ReplaySubject}
     * @param timeUnit   events retain time unit, passed to {@link ReplaySubject}
     * @param scheduler  events retain time scheduler, passed to {@link ReplaySubject}
     * @see ReplaySubject
     */
    public RxBus(int retainTime, TimeUnit timeUnit, Scheduler scheduler) {
        super((Subject<T, R>) ReplaySubject.createWithTime(retainTime, timeUnit, scheduler));
        mSubscriptionsList = new WeakHashMap<>();
    }

    /**
     * This method is used to register an {@link Observer} to this bus.
     * Here is called the {@code subscribe()} to subject
     *
     * @param o
     * @return
     */
    public synchronized Observable<R> register(Observer o) {
        if (o != null) {
            mSubscriptionsList.put(o, subscribe(o));
        }
        return this;
    }

    /**
     * This method is used to unregister an {@link Observer} from this bus
     * Here is called the {@code unsubscribe()}
     *
     * @param o
     * @return
     */
    public synchronized Observable<R> unregister(Observer o) {
        if (o != null) {
            if (mSubscriptionsList.containsKey(o)) {
                Subscription subscription = mSubscriptionsList.get(o);
                if (subscription != null) {
                    subscription.unsubscribe();
                    mSubscriptionsList.remove(o);
                }
            }
            RxAnnotatedHandlerFinder.clearResources(o);
        }
        return this;
    }

    /**
     * This method post events on bus, it use {@code onNext(ev)} method of subject
     *
     * @param ev
     */
    public synchronized void post(T ev) {
        onNext(ev);
    }
}

