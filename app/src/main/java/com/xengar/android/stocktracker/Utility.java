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
}
