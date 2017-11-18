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

package com.mariniu.core.presenter;

import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.navigation.NavigationManager;
import com.mariniu.core.usecase.BaseUseCase;

/**
 * Class to extend to get base functionality of all {@code Presenter}s.
 * Extending classes will orchestrate the execution of 0 or more {@link BaseUseCase}(s).
 * By default they will be registered to the <i>Event Bus</i> at creation.
 *
 * Extending classes <b>MUST</b> call <code>super.shutdown()</code> in their implementation of the same method.
 * Interface defining functionality used by the {@link NavigationManager} to manage the navigation between screens.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class BasePresenter {

    /**
     * Whether this {@code Presenter} is registered to receive events from Otto's bus
     */
    protected boolean mIsRegistered = false;

    /**
     * Registers this Presenter on all event bus(es) and calls {@link #startup()}
     */
    protected BasePresenter() {
        EventDispatcher.register(this);
        mIsRegistered = true;
        startup();
    }

    public void setIsRegistered(boolean isRegistered) {
        this.mIsRegistered = isRegistered;
    }

    public boolean isRegistered() {
        return mIsRegistered;
    }

    /**
     * Convenience method called in the constructor to initialize the Presenter with whatever it might need
     */
    protected abstract void startup();

    /**
     * It unregisters this Presenter from all event bus(es).<br/>
     * <b>Don't forget to call <code>super.shutdown()</code> in your own implementation!</b>
     * Another thing your own implementation should take care of, is to call <code>unregister()</code> on every UseCase it defined.
     */
    public void shutdown() {
        EventDispatcher.unregister(this);
        mIsRegistered = false;
    }
}
