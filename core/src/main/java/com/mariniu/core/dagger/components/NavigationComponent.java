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

package com.mariniu.core.dagger.components;

import dagger.Component;
import com.mariniu.core.activity.BaseActivity;
import com.mariniu.core.dagger.modules.NavigationModule;
import com.mariniu.core.dagger.scopes.AppScope;
import com.mariniu.core.navigation.NavigationManager;

/**
 * Interface for Dagger compiler indicating a dependency-injected implementation of {@link NavigationManager} is to be generated for {@link BaseActivity}.
 * Created on 17/02/2016.
 *
 * @author Umberto Marini
 */
@Component(modules = NavigationModule.class)
@AppScope
public interface NavigationComponent {
    void inject(NavigationManager navigationManager);
    void inject(BaseActivity baseActivity);
}
