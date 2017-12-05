package com.mariniu.core.fragment;

import android.view.accessibility.AccessibilityEvent;

/**
 * This class represents a delegate that can enhance accessibility event with own content.
 * Created on 16/08/16.
 *
 * @author Umberto Marini
 */
public interface AccessibilityDelegate {

    /**
     * Populate the given {@link AccessibilityEvent} for adding it supporting text content.
     * <p>
     * The default implementation behaves as
     * {@link BaseFragment#populateAccessibilityEvent(AccessibilityEvent)}
     * for the case of no accessibility delegate subclass overrides the method.
     * </p>
     *
     * @param event The event.
     */
    void populateAccessibilityEvent(AccessibilityEvent event);

    /**
     * This method will return the accessibility description of the delegate.
     *
     * @return The accessibility supporting text.
     */
    CharSequence getAccessibilityText();
}
