package com.xengar.android.stocktracker.sync;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;


/**
 * A service used to handle asynchronous work off the main thread by way of Intent requests.
 * Each intent is added to the IntentService’s queue and handled sequentially.
 *
 */
public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
