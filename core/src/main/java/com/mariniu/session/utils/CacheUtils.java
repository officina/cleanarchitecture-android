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

package com.mariniu.session.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirClearCallback;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Utility class wrapping functionality of Reservoir cache library.
 *
 * @author Umberto Marini
 */
public class CacheUtils {

    public static final class Keys {
        public static final String KEY_APP_DATA = "Cache.AppData";
        public static final String KEY_SESSION_DATA = "Cache.SessionData";
    }

    private static final String TAG = "CacheUtils";

    /**
     * Default bytes value of disk max space to be used for caching purpose.
     */
    private static final long DEFAULT_CACHE_SIZE = 10 * 10 * 1024; // 10 MB

    private static boolean sInitialized;
    private static long sMaxDiskCacheSize = DEFAULT_CACHE_SIZE;

    /**
     * Initialize Reservoir
     *
     * @param context context.
     * @return {@code true} if Reservoir is correctly initialized, {@code false} otherwise.
     */
    public static boolean init(Context context) {
        Log.i(TAG, "New Reservoir initialization requested");
        sInitialized = false;
        try {
            calculateMaxDiskLruCacheMemory();
            Reservoir.init(context, sMaxDiskCacheSize, GsonUtils.create());
            sInitialized = true;
        } catch (Exception e) {
            Log.e(TAG, "Error while initialize Reservoir: " + e.getMessage(), e);
            sInitialized = false;
        }

        return sInitialized;
    }

    /**
     * In order to choose a suitable size for a LruCache, a number of factors should
     * be taken into consideration, for example:
     * <p>
     * <ul>
     * <li>How memory intensive is the rest of your activity and/or application?</li>
     * <li>How many images will be on-screen at once? How many need to be available
     * ready to come on-screen?</li>
     * <li>What is the screen size and density of the device? An extra high density
     * screen (xhdpi) device like Galaxy Nexus will need a larger cache to hold the
     * same number of images in memory compared to a device like Nexus S (hdpi).</li>
     * <li>What dimensions and configuration are the bitmaps and therefore how much
     * memory will each take up?</li>
     * <li>How frequently will the images be accessed? Will some be accessed more
     * frequently than others? If so, perhaps you may want to keep certain items
     * always in memory or even have multiple LruCache objects for different groups
     * of bitmaps.</li>
     * <li>Can you balance quality against quantity? Sometimes it can be more useful
     * to store a larger number of lower quality bitmaps, potentially loading a higher
     * quality version in another background task.</li>
     * </ul>
     * </p>
     * <p>
     * There is no specific size or formula that suits all applications, it's up to
     * you to analyze your usage and come up with a suitable solution. A cache that is
     * too small causes additional overhead with no benefit, a cache that is too large
     * can once again cause java.lang.OutOfMemory exceptions and leave the rest of your
     * app little memory to work with.
     * </p>
     *
     * @see {@link http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html}
     */
    @SuppressWarnings("JavadocReference")
    public static int calculateMaxLruCacheMemory() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());

        // Use 1/8th of the available memory for this memory cache.
        final int maxLruCacheSize = maxMemory / 8;
        Log.i(TAG, "VM memory for LruCache is: " + maxLruCacheSize + " bytes.");
        return maxLruCacheSize;
    }

    /**
     * Calculte max disk space that cache can initializate. It compare device available
     * internal memory with {@link #DEFAULT_CACHE_SIZE} to understand whick value use.
     */
    private static void calculateMaxDiskLruCacheMemory() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            final long blockSize = stat.getBlockSizeLong();
            final long availableBlocks = stat.getAvailableBlocksLong();
            final long availableSize = blockSize * availableBlocks;
            if (DEFAULT_CACHE_SIZE > availableSize) {
                sMaxDiskCacheSize = (long) (availableSize * .75);
            } else {
                if (DEFAULT_CACHE_SIZE * 1.25 < availableSize) {
                    sMaxDiskCacheSize = DEFAULT_CACHE_SIZE;
                } else {
                    sMaxDiskCacheSize = availableSize;
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error during execute calculateMaxDiskLruCacheMemory method!", e);
        }

        Log.i(TAG, "Disk memory for LruCache is: " + sMaxDiskCacheSize + " bytes.");
    }

    /**
     * Whether Reservoir is initialized via {@link #init(Context)} method.
     *
     * @return {@code true} if Reservoir is correctly initialized, {@code false} otherwise.
     */
    public static boolean isInitialized() {
        if (!sInitialized) {
            Log.w(TAG, "Reservoir not initialized yet.");
        }

        return sInitialized;
    }

    /**
     * Return the size of the disk cache.
     *
     * @return an {@code long} value.
     */
    public static long getCacheMaxSize() {
        return sMaxDiskCacheSize;
    }

    public static void clearCache() {
        try {
            Reservoir.clearAsync(new ReservoirClearCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Reservoir cache is now empty!");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error while clearing Reservoir cache: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Error while clearing Reservoir cache: " + e.getMessage());
        }
    }

    public static boolean put(String key, Object data) {
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "You must provide a key in order to save data in cache!");
            return false;
        }

        if (isInitialized()) {
            try {
                Reservoir.put(key, data);
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Error while saving data into Reservoir cache [key: " + key + ", error: " + e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    public static <T> T get(String key, Class<T> clazz) {
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "You must provide a key in order to get data from cache!");
            return null;
        }

        if (isInitialized()) {
            try {
                return Reservoir.get(key, clazz);
            } catch (Exception e) {
                Log.d(TAG, "Error while getting data from Reservoir cache [key: " + key + ", error: " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Return the list associated to the given key into cache. In order to map correctly object in list,
     * you must provide a {@link Type} that will be used by Reservoir Gson mapper to deserialize the list.
     *
     * @param key     the {@link String} key associated to data you searching for
     * @param typeOfT the {@link Type} of the list. For ex: <code>new TypeToken<List<String>>() {}.getType()</code>
     * @return the cached {@link List} or null if Reservoir don't find any data for the given key
     */
    public static <T> T getCollection(String key, Type typeOfT) {
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "You must provide a key in order to get data from cache!");
            return null;
        }

        if (isInitialized()) {
            try {
                return Reservoir.get(key, typeOfT);
            } catch (Exception e) {
                Log.d(TAG, "Error while getting data from Reservoir cache [key: " + key + ", error: " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean contains(String key) {
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "You must provide a key in order to verify existence of data in cache!");
            return false;
        }

        if (isInitialized()) {
            try {
                Reservoir.contains(key);
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Error while check existence of data into Reservoir cache [key: " + key + ", error: " + e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean delete(String key) {
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "You must provide a key in order to delete data from cache!");
            return false;
        }

        if (isInitialized()) {
            try {
                Reservoir.delete(key);
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Error while deleting data into Reservoir cache [key: " + key + ", error: " + e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }
}
