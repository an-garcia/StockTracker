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
package com.xengar.android.stocktracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xengar.android.stocktracker.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class Utility {

    /**
     * Checks if there is network connection.
     *
     * @return boolean
     */
    public static boolean networkUp(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Returns the change string formated according to the display mode.
     *
     * @param context
     * @param rawAbsoluteChange
     * @param percentageChange
     * @return
     */
    public static String getChangeInDisplayMode(Context context, float rawAbsoluteChange,
                                                float percentageChange)
    {
        DecimalFormat dollarFormatWithPlus;
        DecimalFormat percentageFormat;

        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix( context.getString(R.string.prefix_dollar));
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix(context.getString(R.string.prefix_positive));

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            return change;
        } else {
            return percentage;
        }
    }

    /**
     * Returns the price formated for display.
     *
     * @param rawPrice
     * @return
     */
    public static String getPriceInDisplayMode(float rawPrice) {
        DecimalFormat dollarFormat;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        return dollarFormat.format(rawPrice);
    }


    /**
     * Boolean used to log or not lines
     * Usage:
     *      if (LOG) {
     *           if (condition) Log.i(...);
     *      }
     *  When you set LOG to false, the compiler will strip out all code inside such checks
     * (since it is a static final, it knows at compile time that code is not used.)
     * http://stackoverflow.com/questions/2446248/remove-all-debug-logging-calls-before-publishing-are-there-tools-to-do-this
     */
    public static final boolean LOG = true;

}
