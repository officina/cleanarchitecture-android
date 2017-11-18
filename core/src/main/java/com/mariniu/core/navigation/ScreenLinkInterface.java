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
 * Interface defining functionality used by the {@link NavigationManager} to manage the navigation between screens.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public interface ScreenLinkInterface {

    /**
     * Returns the tag associated with a screen
     *
     * @return the String currently used as a tag for the screen
     */
    int getTag();

    /**
     * Returns the name of a screen
     *
     * @return the String used as a name for the screen
     */
    String getName();

    /**
     * Returns a set of parameters used to create the screen
     *
     * @return a generic Object array containing the parameters used to create the screen
     */
    Object[] getParams();

    /**
     * Sets the parameters to be used to create the screen
     *
     * @param params an array of Objects to be used to create the screen
     * @return <code>this</code>
     */
    ScreenLinkInterface setParams(Object... params);

    /**
     * The class of the fragment used in the current screen
     *
     * @return the {@link Class} of the fragment  used in the current screen
     */
    Class getFragment();

    /**
     * Returns the screen that was shown before the current one
     *
     * @return the previous screen
     */
    ScreenLinkInterface getPreviousScreen();
}
