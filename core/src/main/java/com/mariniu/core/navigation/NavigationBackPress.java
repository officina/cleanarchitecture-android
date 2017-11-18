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

package com.mariniu.core.navigation;

/**
 * Interface to be implemented by the Fragment managed by the {@link NavigationManager} to provide a sensible navigation flow.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public interface NavigationBackPress {

    /***
     * Returns <code>true</code> if we deal with the event locally,
     * <code>false</code> if we want the {@link NavigationManager} to take us back to previous screen
     *
     * @return <code>true</code> if we do something, <code>false</code> otherwise
     */
    boolean onBackPressed();
}
