/*
 * Copyright (C) 2017 Umberto Marini.
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

package com.mariniu.session.base.dataprovider;

import com.mariniu.core.dataprovider.BaseObservableDataProvider;
import com.mariniu.core.dataprovider.DataProviderCreator;

public final class DataProviderType {

    /**
     * Indicates a Repository containing session data available throughout the whole application
     */
    public static final int SESSION = -1;

    private DataProviderType() {
        //empty private coordinator to hide the implicit public one
    }

    /**
     * Provides a concrete implementation of the {@link DataProviderCreator} used to create DataProviders
     */
    public static class DataProviderCreatorImpl implements DataProviderCreator {

        @Override
        public BaseObservableDataProvider getDataProvider(int type) {
            BaseObservableDataProvider dp;
            switch (type) {
                case SESSION:
                    dp = new SessionDataProvider(SessionDataProvider.ONSUBSCRIBE);
                    break;
                default:
                    dp = null;
                    break;
            }
            return dp;
        }

        @Override
        public String getTypeName(int typeCode) {
            switch (typeCode) {
                case SESSION:
                    return "SESSION";
                default:
                    return "*** UNKNOWN ***";
            }
        }
    }
}
