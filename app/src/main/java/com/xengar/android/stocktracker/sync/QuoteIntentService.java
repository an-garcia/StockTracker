/*
 * Copyright (C) 2017 Angel Garcia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xengar.android.stocktracker.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.xengar.android.stocktracker.R;

import static com.xengar.android.stocktracker.Utility.LOG;


/**
 * A service used to handle asynchronous work off the main thread by way of Intent requests.
 * Each intent is added to the IntentService’s queue and handled sequentially.
 *
 */
public class QuoteIntentService extends IntentService {

    private static final String TAG = QuoteIntentService.class.getSimpleName();

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (LOG) {
            Log.e(TAG, getApplicationContext().getString(R.string.msg_intent_handled));
        }
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
