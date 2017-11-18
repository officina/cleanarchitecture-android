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

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.mariniu.core.LibConfiguration;
import com.mariniu.core.events.EventDispatcher;

/**
 * Class used to manage different sorts of {@link BasePresenter}s.
 *
 * The Presenters are never given to the requester, but instead they are created if needed and reused.
 * This is because conceptually a Coordinator is assigned to a logic area of the app, potentially reusable in different sections of the app.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public class PresenterManager {

    private static final String LOG_TAG = "PresenterManager";
    private static final boolean LOG = LibConfiguration.isLoggerEnabled();

    /**
     * A map containing all the {@link BasePresenter}s created.
     * The keys used are the values used by the {@link PresenterCreator}.
     */
    private static SparseArray<BasePresenter> mPresentersMap = new SparseArray<>();

    /**
     * A map keeping track of how many people have access to the Presenters.
     * This is used to know when to remove a Presenter from the map, releasing it only if nobody else is using it anymore.
     * The keys used are the values used by the {@link PresenterCreator}, the values are counters of requester.
     */
    private static SparseIntArray mPresenterRequestersMap = new SparseIntArray();

    /**
     * Creator used to provide the concrete implementation of the requested {@link BasePresenter}s.<br/>
     */
    private static PresenterCreator mCreator;

    private PresenterManager() {
        //empty private constructor to hide the implicit public one
    }

    /**
     * Initialization method that users of this Manager <b>must</b> call if they want it to produce something.
     *
     * @param creator an implementation of {@link PresenterCreator} used to produce the desired {@link BasePresenter}s
     */
    public static synchronized void initBuilder(PresenterCreator creator) {
        mCreator = creator;
    }

    /**
     * Proceeds to the creation or reuse of a {@link BasePresenter} of some type the requester can interact with afterward.
     * These <code>Presenter</code>s aren't returned, thus marking them as "reusable" and "shareable".
     *
     * @param type the requested PresenterType of Presenter
     * @return <code>true</code> if the requested Presenter has been found/created, <code>false</code> otherwise.
     */
    public static synchronized boolean request(int type) {
        if (mCreator != null) {
            String name = getNameForType(type);
            if (LOG) {
                Log.i(LOG_TAG, "new request of Presenter of type " + name);
            }
            if (mPresentersMap.get(type) != null) {
            /*
             * if the Presenter is still around but has been previously unregistered
             * it will be useless until we register it again.
             */
                if (!mPresentersMap.get(type).isRegistered()) {
                    if (LOG) {
                        Log.i(LOG_TAG, "re-registering previously unregistered Presenter of type " + name);
                    }
                    EventDispatcher.register(mPresentersMap.get(type));
                    mPresentersMap.get(type).mIsRegistered = true;
                }
            } else {
                BasePresenter bp = mCreator.getPresenter(type);
                if (bp == null) {
                    if (LOG) {
                        Log.w(LOG_TAG, "requesting unknown Presenter");
                    }
                    return false;
                } else {
                    mPresentersMap.put(type, bp);
                }
            }
            if (mPresenterRequestersMap.get(type) > 0) {
                mPresenterRequestersMap.put(type, mPresenterRequestersMap.get(type) + 1);
            } else {
                mPresenterRequestersMap.put(type, 1);
            }
            if (LOG) {
                Log.i(LOG_TAG, "current number of users of Presenter of type " + name + ": " + mPresenterRequestersMap.get(type));
            }
            return true;
        }
        return false;
    }

    /**
     * Proceeds to the shutdown of the requested Presenter from our internal list.
     * Calling this method indicates that a certain Presenter is no longer needed.
     *
     * @param type the requested PresenterType of Presenter to release
     */
    public static synchronized void release(int type) {
        if (mCreator != null) {
            String name = getNameForType(type);
            if (LOG) {
                Log.i(LOG_TAG, "new release of Presenter of type " + name);
            }
            if (mPresenterRequestersMap.get(type) > 0) {
                mPresenterRequestersMap.put(type, mPresenterRequestersMap.get(type) - 1);
                if (LOG) {
                    Log.i(LOG_TAG, "current number of users of Presenter of type " + name + ": " + mPresenterRequestersMap.get(type));
                }
            }
            if (mPresentersMap.get(type) != null && mPresenterRequestersMap.get(type) == 0) {
                if (LOG) {
                    Log.i(LOG_TAG, "removing Presenter of type " + name + " from map");
                }
                mPresentersMap.get(type).shutdown();
                mPresenterRequestersMap.delete(type);
            }
        }
    }

    /**
     * Removes every Presenter from our internal list.
     * Call this when you are sure you won't need any Presenter anymore
     */
    public static synchronized void purge() {
        if (LOG) {
            Log.i(LOG_TAG, "purging all Presenters");
        }
        int size = mPresentersMap.size();
        for (int i = 0; i < size; i++) {
            BasePresenter bc = mPresentersMap.valueAt(i);
            if (bc != null) {
                bc.shutdown();
            }
        }
        mPresentersMap.clear();
        mPresenterRequestersMap.clear();
    }

    /**
     * Returns a description of the {@link BasePresenter} represented by this code.
     * Useful in logging.
     *
     * @param type the {@code PresenterType} for which we want a name
     * @return a useful description of the {@link BasePresenter} represented by this code.
     */
    private static String getNameForType(int type) {
        String name = mCreator.getTypeName(type);
        if (TextUtils.isEmpty(name)) {
            name = String.valueOf(type);
        }
        return name;
    }
}
