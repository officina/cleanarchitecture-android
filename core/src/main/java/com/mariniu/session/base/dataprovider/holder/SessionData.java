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

package com.mariniu.session.base.dataprovider.holder;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 17/11/2016.
 *
 * @author Umberto Marini
 */
public class SessionData extends BaseDataHolder {

    public static class Constants {
        public static final String SESSION_KEY_CONTENT_IDENTIFIER = "Session.Key.ContentIdentifier";
        public static final String SESSION_KEY_CONTENTBEACON_IDENTIFIER = "Session.Key.ContentBeaconIdentifier";
    }

    /**
     * The session data {@code Map}.
     */
    private Map<String, Object> sessionDataMap = new HashMap<>(0);

    public SessionData() {
        // empty constructor
    }

    ///**
    // * Maps the specified key to the specified session value.
    // *
    // * @param key   the key.
    // * @param value the value.
    // * @return the value of any previous mapping with the specified key or {@code null} if there was no mapping.
    // **/
    //public Object put(String key, Object value) {
    //    if (TextUtils.isEmpty(key)) {
    //        return null;
    //    }
    //
    //    return sessionDataMap.put(key, value);
    //}

    /**
     * This method will put the given SessionEntry to session data Map.
     *
     * @param entry The {@link SessionEntry} object.
     * @return the value of any previous mapping with the specified key or {@code null} if there was no mapping.
     **/
    public Object put(SessionEntry entry) {
        if (entry == null || TextUtils.isEmpty(entry.getKey())) {
            return null;
        }

        return sessionDataMap.put(entry.getKey(), entry.getValue());
    }

    ///**
    // * Returns the value of the mapping with the specified key.
    // *
    // * @param key the key.
    // * @return the value of the mapping with the specified key, or {@code null} if no mapping for the specified key is found.
    // */
    //public <T extends Object> T get(String key, Class<T> valueClass) {
    //    if (TextUtils.isEmpty(key)) {
    //        return null;
    //    }
    //
    //    T returnValue = null;
    //    Object sessionValue = sessionDataMap.get(key);
    //    if (sessionValue != null) {
    //        try {
    //            if (Long.class.isAssignableFrom(valueClass)) {
    //                Double doubleValue = Double.valueOf(String.valueOf(sessionValue));
    //                returnValue = (T) Long.valueOf(doubleValue.longValue());
    //            } else if (Integer.class.isAssignableFrom(valueClass)) {
    //                Double doubleValue = Double.valueOf(String.valueOf(sessionValue));
    //                returnValue = (T) Integer.valueOf(doubleValue.intValue());
    //            } else if (Float.class.isAssignableFrom(valueClass)) {
    //                Double doubleValue = Double.valueOf(String.valueOf(sessionValue));
    //                returnValue = (T) Float.valueOf(doubleValue.floatValue());
    //            } else {
    //                returnValue = (T) sessionValue;
    //            }
    //        } catch (Exception e) {
    //            Log.w("SessionData", "Error during getting value from session with key " + key, e);
    //        }
    //    }
    //
    //    return returnValue;
    //}

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key        the key.
     * @param valueClass the expected class of the value object.
     * @return the value of the mapping with the specified key, or {@code null} if no mapping for the specified key is found.
     */
    public <T extends Object> SessionEntry get(String key, Class<T> valueClass) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        T returnValue = null;
        Object sessionValue = sessionDataMap.get(key);
        if (sessionValue != null) {
            try {
                if (Long.class.isAssignableFrom(valueClass)) {
                    Double doubleValue = Double.valueOf(String.valueOf(sessionValue));
                    returnValue = (T) Long.valueOf(doubleValue.longValue());
                } else if (Integer.class.isAssignableFrom(valueClass)) {
                    Double doubleValue = Double.valueOf(String.valueOf(sessionValue));
                    returnValue = (T) Integer.valueOf(doubleValue.intValue());
                } else if (Float.class.isAssignableFrom(valueClass)) {
                    Double doubleValue = Double.valueOf(String.valueOf(sessionValue));
                    returnValue = (T) Float.valueOf(doubleValue.floatValue());
                } else {
                    returnValue = (T) sessionValue;
                }
            } catch (Exception e) {
                Log.w("SessionData", "Error during getting value from session with key " + key, e);
            }
        }

        return new SessionEntry(key, returnValue);
    }

    /**
     * Removes all elements from the session {@code Map}, leaving it empty.
     *
     * @throws UnsupportedOperationException if removing elements from this {@code Map} is not supported.
     */
    public void clear() {
        try {
            sessionDataMap.clear();
        } catch (Exception e) {
            Log.w("SessionData", "Error during clearing session map.");
        }
    }

    public static final class SessionEntry<T extends Object> {

        private String mKey;
        private T mValue;

        public SessionEntry() {
            // empty contructor
        }

        public SessionEntry(String key, T value) {
            mKey = key;
            mValue = value;
        }

        public String getKey() {
            return mKey;
        }

        public void setKey(String key) {
            mKey = key;
        }

        public T getValue() {
            return mValue;
        }

        public void setValue(T value) {
            mValue = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            SessionEntry<?> that = (SessionEntry<?>) o;

            return mKey != null ? mKey.equals(that.mKey) : that.mKey == null;
        }

        @Override
        public int hashCode() {
            return mKey != null ? mKey.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "SessionEntry{" +
                    "key='" + mKey + '\'' +
                    ", value=" + mValue +
                    '}';
        }
    }
}
