package com.xengar.android.stocktracker;

import android.app.Application;

import timber.log.Timber;


public class StockTrackerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}