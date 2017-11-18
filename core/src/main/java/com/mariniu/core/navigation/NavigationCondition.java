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

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public abstract class NavigationCondition {

    private static final List<NavigationCondition> conditionsList = new ArrayList<>();

    static {
        /*
         * this condition manages the "popup" dialog.
         * It can be opened by:
         * + agb
         * + trip details
         * + ranking
         * + score
         * + message (details)
         * + impressum
         *
         * Since we can switch from a section to the other with the navigation drawer, we ended up in weird situations, such as:
         * 1. dashboard
         * 2. score
         * 3. open score info popup
         * 4. from navdrawer open ranking
         * 5. open ranking info popup
         * 6. press 'back' --> score is shown
         *
         * This condition manages cases like this actually fixing the navigation flow that is to be followed.
         */
        conditionsList.add(new NavigationCondition() {
            @Override
            public ScreenLinkInterface checkCondition(Activity activity, final ScreenLinkInterface screenFrom, final ScreenLinkInterface screenTo) {
                if (screenTo.getTag() == 55) {
                    return new ScreenLinkInterface() {
                        @Override
                        public int getTag() {
                            return screenTo.getTag();
                        }

                        @Override
                        public String getName() {
                            return screenTo.getName();
                        }

                        @Override
                        public Object[] getParams() {
                            return screenTo.getParams();
                        }

                        @Override
                        public ScreenLinkInterface setParams(Object... params) {
                            return screenTo.setParams(params);
                        }

                        @Override
                        public Class getFragment() {
                            return screenTo.getFragment();
                        }

                        @Override
                        public ScreenLinkInterface getPreviousScreen() {
                            return screenFrom;
                        }
                    };
                }
                return screenTo;
            }
        });
    }

    public static List<NavigationCondition> getConditionsList() {
        return conditionsList;
    }

    /**
     * @param activity   activity generica
     * @param screenFrom lo screen attivo
     * @param screenTo   lo screen di destinazione
     * @return lo scrren dove andare oppure null per inibire la transizione
     */
    public abstract ScreenLinkInterface checkCondition(Activity activity, ScreenLinkInterface screenFrom, ScreenLinkInterface screenTo);
}
