package com.mariniu.core.events.rx.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps a 'producer' method on a specific object.
 *
 * <p> This class only verifies the suitability of the method and event type if something fails.  Callers are expected
 * to verify their uses of this class.
 *
 * @author Jake Wharton
 * @author Umberto Marini
 */
class RxEventProducer {

    /** Object sporting the producer method. */
    final Object target;
    /** Producer method. */
    private final Method method;
    /** Object hash code. */
    private final int hashCode;
    /** Should this producer produce events? */
    private boolean valid = true;

    RxEventProducer(Object target, Method method) {
        if (target == null) {
            throw new NullPointerException("EventProducer target cannot be null.");
        }
        if (method == null) {
            throw new NullPointerException("EventProducer method cannot be null.");
        }

        this.target = target;
        this.method = method;
        method.setAccessible(true);

        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        final int prime = 31;
        hashCode = (prime + method.hashCode()) * prime + target.hashCode();
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * If invalidated, will subsequently refuse to produce events.
     *
     * Should be called when the wrapped object is unregistered from the Bus.
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * Invokes the wrapped producer method.
     *
     * @throws IllegalStateException  if previously invalidated.
     * @throws InvocationTargetException  if the wrapped method throws any {@link Throwable} that is not
     *     an {@link Error} ({@code Error}s are propagated as-is).
     */
    public Object produceEvent() throws InvocationTargetException {
        if (!valid) {
            throw new IllegalStateException(toString() + " has been invalidated and can no longer produce events.");
        }
        try {
            return method.invoke(target);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    @Override public String toString() {
        return "[EventProducer " + method + "]";
    }

    @Override public int hashCode() {
        return hashCode;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final RxEventProducer other = (RxEventProducer) obj;

        return method.equals(other.method) && target == other.target;
    }
}