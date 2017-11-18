package com.mariniu.core.events.rx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mariniu.core.events.rx.RxBus;

/**
 * Marks a method as an event handler, as used by {@link RxAnnotatedHandlerFinder} and {@link RxBus}.
 *
 * <p>The method's first (and only) parameter defines the event type.
 * <p>If this annotation is applied to methods with zero parameters or more than one parameter, the object containing
 * the method will not be able to register for event delivery from the {@link RxBus}. Otto fails fast by throwing
 * runtime exceptions in these cases.
 *
 * @author Umberto Marini
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RxSubscribe {
}