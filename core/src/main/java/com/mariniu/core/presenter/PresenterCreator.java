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
 * Interface defining the methods that a {@code PresenterCreator} must have to provide concrete implementations of various {@code PresenterType}s.
 *
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
public interface PresenterCreator {

    /**
     * It provides implementations of the various types of {@link BasePresenter} available in the application.
     * <p>
     * This list of types is defined in the {@code PresenterType} class in the form of a list of <code>int</code>s.
     * </p>
     *
     * @param type a {@code PresenterType} defining what {@link BasePresenter} is requested
     * @return a concrete implementation of the requested {@link BasePresenter} or <code>null</code> if the type is unknown.
     */
    BasePresenter getPresenter(int type);

    /**
     * Returns a description of the {@link BasePresenter} represented by this code.
     * Useful in logging.
     *
     * @param typeCode the {@code PresenterType} for which we want a name
     * @return a useful description of the {@link BasePresenter} represented by this code.
     */
    String getTypeName(int typeCode);
}
