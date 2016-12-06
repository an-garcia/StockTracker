package com.xengar.android.stocktracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


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
}
