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

package com.mariniu.core.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.accessibility.AccessibilityEvent;

import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.navigation.NavigationBackPress;
import com.mariniu.core.presenter.PresenterManager;
import com.mariniu.core.presenter.PresenterRequester;

/**
 * Base class for fragments that:
 * <ul>
 * <li>use the {@link EventDispatcher} to register on event bus and receive events from {@code Presenter}s</li>
 * </ul>
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class BaseFragment extends Fragment implements PresenterRequester, NavigationBackPress, AccessibilityDelegate {//, ScreenComponent {

    protected boolean mIsPresenterAvailable = false;

    private String mEventDispatcherTag;

    /**
     * This is a semaphore to fire {@link PresenterRequester#onPresenterRequesterCreated()} method
     * only the first time the fragment is loaded.
     */
    private int mPresenterRequesterCreationSemaphore = 0;

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        String tag = null;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("ett")) {
                tag = savedInstanceState.getString("ett");
            }
        }
        mEventDispatcherTag = tag;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventDispatcher.loadPoint(this, mEventDispatcherTag);
        EventDispatcher.register(this);
        onPresenterRequesterAttached();
        if(mPresenterRequesterCreationSemaphore == 0){
            mPresenterRequesterCreationSemaphore = 1;
            onPresenterRequesterCreated();
        }
        onPresenterRequesterResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventDispatcherTag = EventDispatcher.savePoint(this);
        EventDispatcher.unregister(this);
        onPresenterRequesterDetached();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEventDispatcherTag != null) {
            outState.putString("ett", mEventDispatcherTag);
        }
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
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void populateAccessibilityEvent(AccessibilityEvent event) {
        // do nothing. fragments extending from BaseFragment should implement
        // this method to add supporting accessibility text to the given event
    }
}
