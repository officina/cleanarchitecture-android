package com.mariniu.core.events.rx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mariniu.core.events.rx.RxBus;

/**
 * Marks a method as an instance producer, as used by {@link RxAnnotatedHandlerFinder} and {@link RxBus}.
 * <p>
 * Otto infers the instance type from the annotated method's return type. Producer methods may return null when there is
 * no appropriate value to share. The calling {@link RxBus} ignores such returns and posts nothing.
 *
 * @author Umberto Marini
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RxProduce {
}
