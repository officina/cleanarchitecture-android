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

package com.mariniu.core.sample.app;

import android.os.Bundle;
import android.widget.TextView;

import com.mariniu.core.events.EventDispatcher;
import com.mariniu.core.events.rx.annotations.RxSubscribe;
import com.mariniu.core.sample.R;
import com.mariniu.core.sample.base.activity.MyBaseActivity;
import com.mariniu.core.sample.base.presenter.PresenterType;
import com.mariniu.core.sample.base.events.data.DataRetrieveWelcomeMessageRequestEvent;
import com.mariniu.core.sample.base.events.data.DataRetrieveWelcomeMessageResponseEvent;

public class MainActivity extends MyBaseActivity {

    private TextView mWelcomeMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWelcomeMessageTextView = (TextView) findViewById(R.id.main_welcome_message_tv);
    }

    @Override
    public void onPresenterRequesterAttached() {
        super.onPresenterRequesterAttached();

        mIsPresenterAvailable = requestPresenter(PresenterType.APPDATA);
    }

    @Override
    public void onPresenterRequesterDetached() {
        super.onPresenterRequesterDetached();

        mIsPresenterAvailable = false;
        releasePresenter(PresenterType.APPDATA);
    }

    @Override
    public void onPresenterRequesterCreated() {
        super.onPresenterRequesterCreated();

        EventDispatcher.post(DataRetrieveWelcomeMessageRequestEvent.create(MainActivity.class));
    }

    @RxSubscribe
    public void onConsumeEvent(final DataRetrieveWelcomeMessageResponseEvent event) {
        if (event.isValidResponse(MainActivity.class)) {
            mWelcomeMessageTextView.setText(event.getWelcomeMessage());
        }
    }
}
