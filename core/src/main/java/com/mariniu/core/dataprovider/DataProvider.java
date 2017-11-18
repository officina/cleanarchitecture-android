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

import android.os.Bundle;

/**
 * A {@code DataProvider} is a repository. It holds data, but exactly <b>where</b> it holds it, it is left to the implementation.
 * Examples can be a local field, a db, a shared preference, etc...
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public interface DataProvider<T> {

    /**
     * Saves the provided data into the data provider
     *
     * @param t data that needs to be saved
     */
    void save(T t);

    /**
     * Retrieves the data from the data provider. This data can be filtered based on the parameters passed in the bundle
     *
     * @param filters parameters indicating how to filter the data (if at all) to retrieved or retrieved.
     *                Pass <code>null</code> to skip filtering
     * @return the desired data
     */
    T retrieve(Bundle filters);

    /**
     * The idea behind this is to be able to transform the data somehow.
     *
     * @param transformationProperties a Bundle containing properties to be used to transform the data
     */
    void transform(Bundle transformationProperties);

    /**
     * The idea behind this is to be able to clear data according the parameters in the given bundle.
     *
     * @param clearProperties a Bundle containing properties to be used to clear the data
     */
    boolean clear(Bundle clearProperties);
}
