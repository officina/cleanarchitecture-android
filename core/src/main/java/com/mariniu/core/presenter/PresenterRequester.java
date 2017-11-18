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

/**
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public interface PresenterRequester {

    /**
     * Notifies whoever is interested that we're alive.
     * Typically it requests a Presenter(s) that suits our needs in this way:
     *
     * <code>
     * mIsPresenterLayerAvailable = PresenterManager.request(PresenterType.AUTH);
     * </code>
     *
     * or, if we need more than one Presenter:
     *
     * <code>
     * mIsPresenterLayerAvailable = PresenterManager.request(PresenterType.AUTH) && PresenterManager.request(PresenterType.APPDATA);
     * </code>
     */
    void onPresenterRequesterAttached();

    /**
     * Notifies whoever is interested that we're leaving.
     * Typically it releases its associated Presenter(s) and whatever resources it holds
     */
    void onPresenterRequesterDetached();

    /**
     * The idea behind this method is to launch an event in order to request the data needed to initialize the GUI and its state.
     * Or, whatever you need to do in an initialization phase.
     */
    void onPresenterRequesterCreated();

    /**
     * The idea behind this method is to do something when requested is resumed from a previous pause.
     */
    void onPresenterRequesterResumed();

    /**
     * Requests the Presenter of the desired type and returns it
     *
     * @param type the {@code PresenterType} of the Presenter requested
     * @return <code>true</code> if the requested Presenter is available, <code>false</code> otherwise
     */
    boolean requestPresenter(int type);

    /**
     * Releases the Presenter of the desired type
     *
     * @param type the {@code PresenterType} of the Presenter to be released
     */
    void releasePresenter(int type);
}
