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

package com.mariniu.core.dataprovider;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.mariniu.core.LibConfiguration;

/**
 * Class used to manage different sorts of {@link BaseObservableDataProvider}s.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public class DataProviderManager {

    private static final String LOG_TAG = "DataProviderManager";
    private static final boolean LOG = LibConfiguration.isLoggerEnabled();
    /**
     * Creator used to provide the concrete implementation of the requested {@link BaseObservableDataProvider}s.<br/>
     */
    static DataProviderCreator mCreator;
    /**
     * A map containing all the {@link BaseObservableDataProvider}s created.
     * The keys used are the values used by the {@link DataProviderCreator}.
     */
    private static SparseArray<BaseObservableDataProvider> mDataProvidersMap = new SparseArray<>();
    /**
     * A map keeping track of how many people have access to the {@code DataProvider}s.
     * This is used to know when to remove a DataProvider from the map, releasing it only if nobody else is using it anymore.
     * The keys used are the values used by the {@link DataProviderCreator}, the values are counters of requester.
     */
    private static SparseIntArray mDataProviderRequestersMap = new SparseIntArray();

    private DataProviderManager() {
        //empty private constructor to hide the implicit public one
    }

    /**
     * Initialization method that users of this Manager <b>must</b> call if they want it to produce something.
     *
     * @param creator an implementation of {@link DataProviderCreator} used to produce the desired {@link BaseObservableDataProvider}s
     */
    public static void initBuilder(DataProviderCreator creator) {
        mCreator = creator;
    }

    /**
     * Proceeds to the creation or reuse of a {@link BaseObservableDataProvider} of some type the requester can interact with afterward.
     *
     * @param type the requested DataProviderType of data provider
     * @return the requested DataProvider if found/created, <code>null</code> otherwise.
     */
    public static BaseObservableDataProvider request(int type) {
        if (mCreator != null) {
            String name = getNameForType(type);
            if (LOG) {
                Log.i(LOG_TAG, "new request of DataProvider of type " + name);
            }
            if (mDataProvidersMap.get(type) != null) {
                return mDataProvidersMap.get(type);
            } else {
                BaseObservableDataProvider br = mCreator.getDataProvider(type);
                if (br == null) {
                    if (LOG) {
                        Log.w(LOG_TAG, "requesting unknown data provider");
                    }
                    return null;
                }
                if (mDataProviderRequestersMap.get(type) > 0) {
                    mDataProviderRequestersMap.put(type, mDataProviderRequestersMap.get(type) + 1);
                } else {
                    mDataProviderRequestersMap.put(type, 1);
                }
                mDataProvidersMap.put(type, br);
                if (LOG) {
                    Log.i(LOG_TAG, "current number of users of DataProvider of type " + name + ": " + mDataProviderRequestersMap.get(type));
                }
                return br;
            }
        }
        return null;
    }

    /**
     * Proceeds to the removal of the requested DataProvider from our internal list.
     * Calling this method indicates that a certain DataProvider is no longer needed around.
     *
     * @param type the requested DataProviderType of data provider to remove
     */
    public static void release(int type) {
        if (mCreator != null) {
            String name = getNameForType(type);
            if (LOG) {
                Log.i(LOG_TAG, "new release of DataProvider of type " + name);
            }
            if (mDataProviderRequestersMap.get(type) > 0) {
                mDataProviderRequestersMap.put(type, mDataProviderRequestersMap.get(type) - 1);
                if (LOG) {
                    Log.i(LOG_TAG, "current number of users of DataProvider of type " + name + ": " + mDataProviderRequestersMap.get(type));
                }
            }
            if (mDataProvidersMap.get(type) != null && mDataProviderRequestersMap.get(type) == 0) {
                if (LOG) {
                    Log.i(LOG_TAG, "removing DataProvider of type " + name + " from map");
                }
                mDataProvidersMap.get(type).removeAllSubscribers();
                mDataProviderRequestersMap.delete(type);
            }
        }
    }

    /**
     * Removes every DataProvider from our internal list.
     * Call this when you are sure you won't need any data provider anymore
     */
    public static void purge() {
        if (LOG) {
            Log.i(LOG_TAG, "purging all DataProviders");
        }
        int size = mDataProvidersMap.size();
        for (int i = 0; i < size; i++) {
            BaseObservableDataProvider bor = mDataProvidersMap.valueAt(i);
            if (bor != null) {
                bor.removeAllSubscribers();
            }
        }
        mDataProvidersMap.clear();
        mDataProviderRequestersMap.clear();
    }

    /**
     * Returns a description of the {@link BaseObservableDataProvider} represented by this code.
     * Useful in logging.
     *
     * @param type the {@code DataProviderType} for which we want a name
     * @return a useful description of the {@link BaseObservableDataProvider} represented by this code.
     */
    private static String getNameForType(int type) {
        String name = mCreator.getTypeName(type);
        if (TextUtils.isEmpty(name)) {
            name = String.valueOf(type);
        }
        return name;
    }
}
