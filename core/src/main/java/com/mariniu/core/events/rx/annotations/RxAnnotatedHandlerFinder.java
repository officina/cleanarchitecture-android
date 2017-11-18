package com.mariniu.core.events.rx.annotations;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Helper methods for finding methods annotated with {@link RxProduce} and {@link RxSubscribe}.
 *
 * @author Cliff Biffle
 * @author Louis Wasserman
 * @author Jake Wharton
 */
public final class RxAnnotatedHandlerFinder {

    /**
     * Cache event bus producer methods for each class.
     */
    private static final ConcurrentMap<Class<?>, Map<Class<?>, Method>> PRODUCERS_CACHE =
            new ConcurrentHashMap<Class<?>, Map<Class<?>, Method>>();

    /**
     * Cache event bus subscriber methods for each class.
     */
    private static final ConcurrentMap<Class<?>, Map<Class<?>, Set<Method>>> SUBSCRIBERS_CACHE =
            new ConcurrentHashMap<Class<?>, Map<Class<?>, Set<Method>>>();
    private static final String LOG_TAG = RxAnnotatedHandlerFinder.class.getSimpleName();

    private static void loadAnnotatedProducerMethods(Class<?> listenerClass,
                                                     Map<Class<?>, Method> producerMethods) {
        Map<Class<?>, Set<Method>> subscriberMethods = new HashMap<Class<?>, Set<Method>>();
        loadAnnotatedMethods(listenerClass, producerMethods, subscriberMethods);
    }

    private static void loadAnnotatedSubscriberMethods(Class<?> listenerClass,
                                                       Map<Class<?>, Set<Method>> subscriberMethods) {
        Map<Class<?>, Method> producerMethods = new HashMap<Class<?>, Method>();
        loadAnnotatedMethods(listenerClass, producerMethods, subscriberMethods);
    }

    /**
     * Load all methods annotated with {@link RxProduce} or {@link RxSubscribe} into their respective caches for the
     * specified class.
     */
    private static void loadAnnotatedMethods(Class<?> listenerClass,
                                             Map<Class<?>, Method> producerMethods, Map<Class<?>, Set<Method>> subscriberMethods) {
        for (Method method : listenerClass.getDeclaredMethods()) {
            // The compiler sometimes creates synthetic bridge methods as part of the
            // type erasure process. As of JDK8 these methods now include the same
            // annotations as the original declarations. They should be ignored for
            // subscribe/produce.
            if (method.isBridge()) {
                continue;
            }
            if (method.isAnnotationPresent(RxSubscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require a single argument.");
                }

                Class<?> eventType = parameterTypes[0];
                if (eventType.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + eventType
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + eventType
                            + " but is not 'public'.");
                }

                Set<Method> methods = subscriberMethods.get(eventType);
                if (methods == null) {
                    methods = new HashSet<Method>();
                    subscriberMethods.put(eventType, methods);
                }
                methods.add(method);
            } else if (method.isAnnotationPresent(RxProduce.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 0) {
                    throw new IllegalArgumentException("Method " + method + "has @Produce annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require zero arguments.");
                }
                if (method.getReturnType() == Void.class) {
                    throw new IllegalArgumentException("Method " + method
                            + " has a return type of void.  Must declare a non-void type.");
                }

                Class<?> eventType = method.getReturnType();
                if (eventType.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation on " + eventType
                            + " which is an interface.  Producers must return a concrete class type.");
                }
                if (eventType.equals(Void.TYPE)) {
                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation but has no return type.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation on " + eventType
                            + " but is not 'public'.");
                }

                if (producerMethods.containsKey(eventType)) {
                    throw new IllegalArgumentException("Producer for type " + eventType + " has already been registered.");
                }
                producerMethods.put(eventType, method);
            }
        }

        PRODUCERS_CACHE.put(listenerClass, producerMethods);
        SUBSCRIBERS_CACHE.put(listenerClass, subscriberMethods);
    }

    /**
     * This implementation finds all methods marked with a {@link RxProduce} annotation.
     */
    static Map<Class<?>, RxEventProducer> findAllProducers(Object listener) {
        final Class<?> listenerClass = listener.getClass();
        Map<Class<?>, RxEventProducer> handlersInMethod = new HashMap<Class<?>, RxEventProducer>();

        Map<Class<?>, Method> methods = PRODUCERS_CACHE.get(listenerClass);
        if (null == methods) {
            methods = new HashMap<Class<?>, Method>();
            loadAnnotatedProducerMethods(listenerClass, methods);
        }
        if (!methods.isEmpty()) {
            for (Map.Entry<Class<?>, Method> e : methods.entrySet()) {
                RxEventProducer producer = new RxEventProducer(listener, e.getValue());
                handlersInMethod.put(e.getKey(), producer);
            }
        }

        return handlersInMethod;
    }

    /**
     * This implementation finds all methods marked with a {@link RxSubscribe} annotation.
     */
    static Map<Class<?>, Set<RxEventHandler>> findAllSubscribers(Object listener) {
        Class<?> listenerClass = listener.getClass();
        Map<Class<?>, Set<RxEventHandler>> handlersInMethod = new HashMap<Class<?>, Set<RxEventHandler>>();

        Map<Class<?>, Set<Method>> methods = SUBSCRIBERS_CACHE.get(listenerClass);
        if (null == methods) {
            methods = new HashMap<Class<?>, Set<Method>>();
            loadAnnotatedSubscriberMethods(listenerClass, methods);
        }
        if (!methods.isEmpty()) {
            for (Map.Entry<Class<?>, Set<Method>> e : methods.entrySet()) {
                Set<RxEventHandler> handlers = new HashSet<RxEventHandler>();
                for (Method m : e.getValue()) {
                    handlers.add(new RxEventHandler(listener, m));
                }
                handlersInMethod.put(e.getKey(), handlers);
            }
        }

        return handlersInMethod;
    }

    /**
     * It will clear resources associated with listener and return reference count freed
     *
     * @param listener
     * @return total objects removed
     */
    public static int clearResources(Object listener) {
        Map<Class<?>, Set<Method>> subscribersCache = SUBSCRIBERS_CACHE.remove(listener);
        Map<Class<?>, Method> producersCache = PRODUCERS_CACHE.remove(listener);

        int subscribersRefCount = 0, producersRefCount = 0;
        if (subscribersCache != null) {
            subscribersRefCount = subscribersCache.size();
        }
        if (producersCache != null) {
            producersRefCount = producersCache.size();
        }
        return subscribersRefCount + producersRefCount;
    }

    /**
     * This method is used to call the event on listener, it use reflection to know what method call on listener object.
     *
     * @param listener
     * @param event
     */
    public static void handleEvent(Object listener, Object event) {
        Map<Class<?>, Set<RxEventHandler>> allSubscribers = RxAnnotatedHandlerFinder.findAllSubscribers(listener);

        if (allSubscribers != null && !allSubscribers.isEmpty()) {
            Set<RxEventHandler> rxEventHandlers = allSubscribers.get(event.getClass());
            if (rxEventHandlers != null && !rxEventHandlers.isEmpty()) {
                Iterator<RxEventHandler> iterator = rxEventHandlers.iterator();
                while (iterator.hasNext()) {
                    RxEventHandler handler = iterator.next();
                    try {
                        handler.handleEvent(event);
                    } catch (InvocationTargetException e) {
                        Log.e(LOG_TAG, Log.getStackTraceString(e.getCause()));
                    }
                }
            }
        }
    }

    private RxAnnotatedHandlerFinder() {
        // No instances.
    }
}
