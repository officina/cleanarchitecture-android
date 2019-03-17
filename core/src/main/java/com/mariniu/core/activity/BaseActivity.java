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

package com.mariniu.core.activity;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.mariniu.core.R;
import com.mariniu.core.dagger.components.DaggerNavigationComponent;
import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.navigation.NavigationManager;
import com.mariniu.core.presenter.PresenterManager;
import com.mariniu.core.presenter.PresenterRequester;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/**
 * Base class for activities that use:
 * <ul>
 * <li>the {@link NavigationManager} to manage fragments flow</li>
 * <li>the {@link EventDispatcher} to register on event bus and receive events from {@code Presenter}s</li>
 * <li>the  double tap exit confirmation</li>
 * <li>the {@link Toast} message as feedback for short message to the user</li>
 * </ul>
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class BaseActivity extends AppCompatActivity implements PresenterRequester {

    /**
     * Default timeout to use to determine whether to exit from the Activity or not.
     */
    private static final long BACKPRESS_TIMEOUT = 1500;

    /**
     * The id of the fragment container for this {@link BaseActivity}.
     */
    protected int mContentViewId;

    /**
     * Timestamp of the 'back' button press event.
     */
    protected long mLastBackPressedTimestamp = 0;

    /**
     * Flag indicating when user must perform double back press to exit from the application.
     */
    protected boolean mExitConfirmationEnabled = true;

    /**
     * Convenience flag used to determine whether the {@code Presenter}(s) we requested to the
     * {@link PresenterManager} are all present and available.
     * If for some reason this isn't <code>true</code> it means that something went wrong in the
     * production of the {@code Presenter}(s) and we won't be able to provide certain functionality.
     */
    protected boolean mIsPresenterAvailable = false;

    /**
     * Toast for this {@link BaseActivity}. Each call of {@link BaseActivity#showToast(CharSequence)}
     * will clear the previous toast instance and, if it's showed, dismiss the view to show the
     * new message.
     */
    private Toast mToast;

    /**
     * This is a semaphore to fire {@link PresenterRequester#onPresenterRequesterCreated()} method
     * only the first time the Activity is loaded.
     */
    private int mPresenterRequesterCreationSemaphore = 0;

    /**
     * Indicates whether this Activity is active.
     */
    private AtomicBoolean mIsRunning = new AtomicBoolean(false);

    private CharSequence mEventDispatcherTag;

    @Inject
    protected NavigationManager mNavigationManager;

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getPackageName());

        ViewGroup.LayoutParams params = getWindow().getAttributes();
        boolean isFullScreen = (params.width == ViewGroup.LayoutParams.MATCH_PARENT) &&
                (params.height == ViewGroup.LayoutParams.MATCH_PARENT);
        event.setFullScreen(isFullScreen);

        // MOVED TO BaseFragment#onResume method
        // get the accessibility text from the current fragment
        //if (mNavigationManager != null) {
        //    BaseFragment fragment = mNavigationManager.getCurrentFragment(this);
        //    if (fragment != null) {
        //        fragment.populateAccessibilityEvent(event);
        //    }
        //}

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerNavigationComponent.create().inject(this);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("ett")) {
                mEventDispatcherTag = savedInstanceState.getString("ett");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsRunning.set(true);

        EventDispatcher.loadPoint(this, String.valueOf(mEventDispatcherTag));
        EventDispatcher.register(this);
        onPresenterRequesterAttached();
        if (mPresenterRequesterCreationSemaphore == 0) {
            mPresenterRequesterCreationSemaphore = 1;
            onPresenterRequesterCreated();
        }
        onPresenterRequesterResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsRunning.set(false);

        mEventDispatcherTag = EventDispatcher.savePoint(this);
        EventDispatcher.unregister(this);
        onPresenterRequesterDetached();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("ett", String.valueOf(mEventDispatcherTag));
    }

    @Override
    public boolean requestPresenter(int type) {
        return PresenterManager.request(type);
    }

    @Override
    public void releasePresenter(int type) {
        PresenterManager.release(type);
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationManager.goBack(this)) {
            if (mExitConfirmationEnabled) {
                exitFromApp(getExitConfirmationMessage());
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate avoiding fragments management by {@link NavigationManager}.
     */
    public void onSuperBackPressed() {
        super.onBackPressed();
    }

    /**
     * This method will returns the message to show the user if double tap on exit is enabled.
     *
     * @see {@link #setExitConfirmationEnabled(boolean)}
     */
    public CharSequence getExitConfirmationMessage() {
        return getString(R.string.message_exit_confirmation);
    }

    /**
     * Specifies the id of the fragment container of the Activity
     *
     * @param contentViewId the id of the root container. E.g. <code>R.id.content</code>
     */
    protected void bindContentView(int contentViewId) {
        this.mContentViewId = contentViewId;
    }

    /**
     * Returns the id of the fragment container of the Activity.
     *
     * @return the id of the container. E.g. <code>R.id.content</code>
     */
    public int getRootViewId() {
        return mContentViewId;
    }

    /**
     * Set the flag status driving the app exit confirmation.
     *
     * @param exitConfirmationEnabled {@code true} to enable the double back press to exit from the app,
     *                                {@code false} to exit with a single back press
     */
    public void setExitConfirmationEnabled(boolean exitConfirmationEnabled) {
        mExitConfirmationEnabled = exitConfirmationEnabled;
    }

    /**
     * Checks if the user pressed the 'back' button twice in the {@link #BACKPRESS_TIMEOUT}.
     * If that's not the case it shows a Toast with a message, otherwise it exits from the Activity.
     *
     * @param message a message to show the user in a Toast if it's the first time the user taps the 'back' button.
     */
    protected void exitFromApp(CharSequence message) {
        if (mLastBackPressedTimestamp + BACKPRESS_TIMEOUT > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            showToast(message);
        }
        mLastBackPressedTimestamp = System.currentTimeMillis();
    }

    /**
     * Shows a simple toast with a message.
     *
     * @param messageStringId Resource id for the string message.
     */
    public void showToast(@StringRes int messageStringId) {
        showToast(getString(messageStringId));
    }

    /**
     * Shows a simple toast with a message.
     *
     * @param message Message to show the user.
     */
    public void showToast(CharSequence message) {
        // dismiss previous toast
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * This method will return whether this Activity is running or not.
     */
    public boolean isRunning() {
        return mIsRunning.get();
    }

    public int getContentViewId() {
        return mContentViewId;
    }
}
