package com.mariniu.core.events.rx;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import com.mariniu.core.events.Event;
import com.mariniu.core.events.EventProcessor;
import com.mariniu.core.events.rx.annotations.RxAnnotatedHandlerFinder;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Class managing the events used throughout the application using {@link RxBus} classes.
 * <br>
 * <ul>
 * <li>{@link #BUS}: a Bus working on a thread separated by the Android Main Thread</li>
 * <li>{@link #UI_BUS}: a Bus working on the Android Main Thread</li>
 * </ul>
 * <br>
 * <br>
 * Events are emitted to all subscribers and maintains time sequence.
 * <br>
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public final class RxEventProcessor implements EventProcessor {

    private static final String LOG_TAG = RxEventProcessor.class.getSimpleName();

    /**
     * This Map contains in keys some autogenerate String and used to identify a subscriber, in values some timestamps.
     * These timestamps values are generated when a subscriber call the method {@code EventDispatcher.savePoint(...)}.
     */
    private final Map<String, Long> mSavePoints = new HashMap<>();
    /**
     * Bus working on a thread separated by the Android Main Thread
     */

    private final RxBus BUS = new RxBus();
    /**
     * Bus working on the Android Main Thread
     */
    private final RxBus UI_BUS = new RxBus(10);
    /**
     * This map contains in keys the objects registered to all Bus, in value the wrapper of object with {@link Observer} interface
     */
    private final Map<Object, ObserverWrapper> wrapperCache = new WeakHashMap();

    /**
     * Whether the RxEventProcessor should use logs or not. Default: {@code true}.
     */
    private static boolean sVerbose = false;

    private RxEventProcessor() {
        // No instances.
        BUS.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread()).onBackpressureBuffer();

        UI_BUS.observeOn(Schedulers.newThread()).subscribeOn(AndroidSchedulers.mainThread()).onBackpressureBuffer();
    }

    /**
     * Convenience method used for debugging purposes. It will log what kind of event is being posted on what Bus.
     *
     * @param ev      The event being posted
     * @param uiEvent whether it's a UI event or not.
     */
    private static void logEvent(Object ev, boolean uiEvent) {
        String bus;
        if (uiEvent) {
            bus = "UI_BUS";
        } else {
            bus = "BUS";
        }

        if (sVerbose) {
            Log.i(LOG_TAG, "posting " + ev.getClass().getSimpleName() + " of type " + ev.getClass().getAnnotation(Event.class).type() + " on " + bus);
        }
    }

    public static EventProcessor newInstance() {
        return new RxEventProcessor();
    }

    /**
     * This method is used to call the event on listener, it use reflection to know what method call on listener object.
     *
     * @param listener
     * @param event
     */
    private static void handleEvent(Object listener, Object event) {
        RxAnnotatedHandlerFinder.handleEvent(listener, event);
    }

    /**
     * It will retrieve an {@link ObserverWrapper} of the object passed by parameter
     * This method store the wrapper inside an internal cache.
     * It should be used in pair with {@code RxEventProcessor.removeWrapper(...)}
     *
     * @param o
     * @return
     */
    private ObserverWrapper getWrapper(Object o) {
        if (!wrapperCache.containsKey(o)) {
            wrapperCache.put(o, new ObserverWrapper(o));
        }
        return wrapperCache.get(o);
    }

    /**
     * It will remove the {@link ObserverWrapper} of the object passed by parameter
     * This method return {@link ObserverWrapper} removed from cache
     *
     * @param o
     * @return
     */
    private ObserverWrapper removeWrapper(Object o) {
        return wrapperCache.remove(o);
    }

    /**
     * This method is used to make a current timestamp
     *
     * @return
     */
    private long makeTimestamp() {
        return new Date().getTime();
    }

    /**
     * This method will set whether the RxEventProcessor should use logs or not.
     * Default: {@code true}.
     *
     * @param enabled {@code true} to enable logs, {@code false} otherwise
     */
    public void setVerbose(boolean enabled) {
        sVerbose = enabled;
    }

    @Override
    public void onRegister(Object o) {
        if (o != null) {
            ObserverWrapper observerWrapper = getWrapper(o);
            if (observerWrapper.mSavedTimestamp <= 0) {
                observerWrapper.mSavedTimestamp = makeTimestamp();
            }
            BUS.register(observerWrapper);
            UI_BUS.register(observerWrapper);
        }
    }

    @Override
    public void onUnregister(Object o) {
        if (o != null) {
            ObserverWrapper removedObject = removeWrapper(o);
            if (removedObject != null) {
                BUS.unregister(removedObject);
                UI_BUS.unregister(removedObject);
                removedObject.clear();
            }
        }
    }

    @Override
    public void onPost(Object o) {
        if (sVerbose) {
            Log.i(LOG_TAG, "received new object to post: " + o.getClass().getSimpleName());
        }

        //check if it's an event we recognise
        if (o != null && o.getClass().isAnnotationPresent(Event.class)) {
            //put it in the right list and sort the list
            Event.Type t = o.getClass().getAnnotation(Event.class).type();

            if (sVerbose) {
                Log.i(LOG_TAG, "object " + o.getClass().getSimpleName() + " is an event of type " + t);
            }

            switch (t) {
                case UI:
                    UI_BUS.post(new ObservedEvent(o, makeTimestamp(), t));
                    break;
                default:
                    BUS.post(new ObservedEvent(o, makeTimestamp(), t));
                    break;
            }
        }
    }

    @Override
    public String onSavePoint(Object object) {
        String key = null;
        // creating a key based on timestamp + class name + hashcode + random number
        if (object != null) {
            // creating timestamp
            long timestamp = makeTimestamp();
            // creating prefix of the string with class name and objecthashcode
            String prefix = object.getClass().getName() + "$" + object.hashCode();
            // creating a random number
            long LOWER_RANGE = 0; //assign lower range value
            long UPPER_RANGE = 1000000; //assign upper range value
            Random random = new Random();
            long randomSeed = LOWER_RANGE + (long) (random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));

            // assembling the final key
            key = prefix + randomSeed + timestamp;
            mSavePoints.put(key, new Date().getTime());
        }
        return key;
    }

    @Override
    public void onLoadPoint(Object object, String key) {
        if (object != null && key != null && !key.isEmpty()) {
            // retrieve the saved timestamp based on the key
            Long timestamp = mSavePoints.get(key);
            // save it inside the wrapper
            ObserverWrapper wrapper = getWrapper(object);
            if (wrapper != null && timestamp != null) {
                wrapper.mSavedTimestamp = timestamp;
            }
        }
    }

    /**
     * This class is used to wrap an object annotated with {@link Event} annotation.
     * This wrapper app information about the save point used with {@code EventDispatcher.savePoint} and {@code EventDispatcher.loadPoint}
     */
    private static final class ObservedEvent {
        /**
         * The reference of the event
         */
        private final WeakReference mEvent;
        /**
         * When the event was posted
         */
        private final long mEventPostTimestamp;
        /**
         * The {@link Event.Type} of the event
         */
        private final Event.Type mEventType;

        /**
         * This method is used to instantiate a new {@link ObservedEvent}
         *
         * @param event              the wrapped event
         * @param eventPostTimestamp The timestamp of when the event was posted
         * @param eventType          the wrapped event type
         */
        public ObservedEvent(Object event, long eventPostTimestamp, Event.Type eventType) {
            this.mEvent = new WeakReference(event);
            this.mEventPostTimestamp = eventPostTimestamp;
            this.mEventType = eventType;
        }
    }

    /**
     * This class is used to wrap bus subscribers and make them compatible with {@link Observer} interface
     */
    private static final class ObserverWrapper implements Observer {

        /**
         * The reference of the subscriber
         */
        private final WeakReference mWrapped;
        /**
         * The timestamp saved by method {@code EventDispatcher.savePoint}
         */
        private long mSavedTimestamp;

        public ObserverWrapper(Object wrapped) {
            mWrapped = new WeakReference(wrapped);
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object event) {
            if (mWrapped != null) {
                Object wrappedRefObject = mWrapped.get();
                if (wrappedRefObject != null && event instanceof ObservedEvent) {
                    ObservedEvent observedEvent = (ObservedEvent) event;
                    boolean shouldHandleEvent = true;

                    // manage timestamp of ObservedEvent
                    long eventTimestamp = observedEvent.mEventPostTimestamp;
                    Object eventToHandle = observedEvent.mEvent.get();
                    Event.Type eventType = observedEvent.mEventType;

                    // if the event post timestamp is before the last saved timestamp of the subscriber
                    // we don't emit it, cause the event was already catched by the subscriber
                    if (mSavedTimestamp > 0) {
                        shouldHandleEvent = eventTimestamp >= mSavedTimestamp;
                    }

                    if (shouldHandleEvent && eventToHandle != null) {
                        RxEventProcessor.logEvent(eventToHandle, eventType == Event.Type.UI);
                        RxEventProcessor.handleEvent(wrappedRefObject, eventToHandle);
                    }
                }
            }
        }

        public void clear() {
            if (mWrapped != null) {
                mWrapped.clear();
            }
        }
    }
}
