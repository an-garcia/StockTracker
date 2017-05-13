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

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import com.xengar.android.stocktracker.R;

import timber.log.Timber;

/**
 * Lollipop JobScheduler API for schedule a job to take place according to some parameters.
 * So, we can set parameters that will save battery life.
 * {@link}http://toastdroid.com/2015/02/21/how-to-use-androids-job-scheduler/
 *
 * There are 3 main parts: JobInfo, JobService and JobScheduler
 * The JobService is the service that is going to run the job. It runs on the main thread.
 */
public class QuoteJobService extends JobService {

    /**
     * Called when the JobScheduler decides to run the job based on the parameters.
     */
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Timber.d(getString(R.string.msg_intent_handled));
        Intent nowIntent = new Intent(getApplicationContext(), QuoteIntentService.class);
        getApplicationContext().startService(nowIntent);
        return true;
    }

    /**
     * It's called when the parameters are no longer being met (when the user switches off of wifi,
     * unplugs or turns the screen on their device).
     * This means that we want to stop our job as soon as we can; so, don't update the caller.
     *
     * @param jobParameters
     * @return
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
