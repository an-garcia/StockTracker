package com.xengar.android.stocktracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.xengar.android.stocktracker.utils.PollingCheck;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/*
    These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your Contract class to exactly match the one
    in our solution to use these as-given.
 */
@RunWith(AndroidJUnit4.class)
public class TestUtilities {

    // Sample stock Berkshire Hathaway Inc.
    static final String TEST_STOCK = "BRK-B";
    // Sample history of BRK-B for one year
    static final String TEST_HISTORY_BRK_B_STOCK =
            "Date,Open,High,Low,Close,Volume,Adj Close\n" +
            "2016-12-05,160.25,161.550003,159.270004,161.339996,4004900,161.339996\n" +
            "2016-11-28,157.899994,160.240005,156.820007,159.389999,4075300,159.389999\n" +
            "2016-11-21,158.100006,159.089996,157.279999,158.179993,2690000,158.179993\n" +
            "2016-11-14,157.479996,158.639999,156.020004,157.75,4426400,157.75\n" +
            "2016-11-07,144.710007,157.279999,144.300003,156.919998,5996300,156.919998\n" +
            "2016-10-31,144.300003,144.860001,142.350006,142.949997,3149100,142.949997\n" +
            "2016-10-24,144.089996,145.289993,142.899994,144.00,2594700,144.00\n" +
            "2016-10-17,144.50,145.710007,142.800003,143.600006,2564200,143.600006\n" +
            "2016-10-10,145.050003,145.679993,141.919998,144.179993,2951100,144.179993\n" +
            "2016-10-03,144.270004,144.940002,142.800003,144.520004,2984900,144.520004\n" +
            "2016-09-26,144.520004,145.520004,143.360001,144.470001,3355000,144.470001\n" +
            "2016-09-19,145.339996,147.089996,144.759995,145.00,3127200,145.00\n" +
            "2016-09-12,146.369995,149.389999,144.679993,145.009995,5463200,145.009995\n" +
            "2016-09-06,150.539993,150.979996,146.559998,146.619995,3551000,146.619995\n" +
            "2016-08-29,148.229996,151.050003,148.00,150.720001,3235300,150.720001\n" +
            "2016-08-22,148.509995,149.240005,147.220001,147.910004,2275200,147.910004\n" +
            "2016-08-15,147.720001,149.360001,147.00,148.75,2437500,148.75\n" +
            "2016-08-08,145.289993,147.979996,144.229996,147.720001,3104100,147.720001\n" +
            "2016-08-01,144.619995,145.649994,142.919998,145.649994,2500000,145.649994\n" +
            "2016-07-25,144.869995,145.00,143.479996,144.270004,2419100,144.270004\n" +
            "2016-07-18,146.00,146.990005,143.889999,144.600006,3176100,144.600006\n" +
            "2016-07-11,143.75,146.660004,143.429993,145.970001,3361900,145.970001\n" +
            "2016-07-05,143.190002,143.740005,140.949997,143.649994,3203100,143.649994\n" +
            "2016-06-27,138.789993,144.830002,136.649994,143.960007,4793600,143.960007\n" +
            "2016-06-20,141.960007,146.00,139.470001,139.710007,5669100,139.710007\n" +
            "2016-06-13,141.229996,142.649994,138.960007,140.729996,3740600,140.729996\n" +
            "2016-06-06,141.619995,142.690002,141.160004,141.759995,2514700,141.759995\n" +
            "2016-05-31,143.889999,144.139999,140.110001,141.139999,3938300,141.139999\n" +
            "2016-05-23,141.850006,144.940002,141.050003,143.350006,2655500,143.350006\n" +
            "2016-05-16,141.399994,142.490005,139.679993,141.830002,3145900,141.830002\n" +
            "2016-05-09,144.539993,144.979996,141.130005,141.399994,3075700,141.399994\n" +
            "2016-05-02,145.770004,147.139999,143.330002,144.619995,2707100,144.619995\n" +
            "2016-04-25,145.649994,148.029999,144.520004,145.479996,2905200,145.479996\n" +
            "2016-04-18,143.00,146.440002,142.949997,146.110001,2856100,146.110001\n" +
            "2016-04-11,141.789993,144.350006,140.960007,143.449997,2840900,143.449997\n" +
            "2016-04-04,143.990005,143.990005,140.270004,141.059998,2837900,141.059998\n" +
            "2016-03-28,140.949997,144.050003,140.330002,143.789993,3336300,143.789993\n" +
            "2016-03-21,142.160004,142.990005,139.559998,140.110001,3038500,140.110001\n" +
            "2016-03-14,140.800003,142.960007,139.009995,142.160004,4455900,142.160004\n" +
            "2016-03-07,137.699997,140.759995,137.429993,140.649994,4138600,140.649994\n" +
            "2016-02-29,133.360001,138.059998,132.940002,137.970001,5148300,137.970001\n" +
            "2016-02-22,132.190002,133.289993,128.880005,131.919998,3560600,131.919998\n" +
            "2016-02-16,129.440002,132.350006,128.070007,131.050003,4596200,131.050003\n" +
            "2016-02-08,125.699997,129.179993,124.040001,128.070007,5480800,128.070007\n" +
            "2016-02-01,128.940002,129.639999,123.550003,126.559998,4943200,126.559998\n" +
            "2016-01-25,126.550003,129.770004,123.900002,129.770004,4585200,129.770004\n" +
            "2016-01-19,126.860001,128.270004,124.040001,127.040001,5356900,127.040001\n" +
            "2016-01-11,128.919998,129.419998,124.510002,126.139999,5870800,126.139999\n" +
            "2016-01-04,130.160004,131.759995,128.210007,128.330002,5931300,128.330002\n" +
            "2015-12-28,133.479996,134.550003,131.949997,132.039993,2884100,132.039993\n" +
            "2015-12-21,131.160004,134.550003,129.649994,133.889999,3218400,133.889999\n" +
            "2015-12-14,130.50,136.160004,129.529999,129.529999,5287900,129.529999\n" +
            "2015-12-07,136.199997,136.449997,129.559998,130.309998,3971500,130.309998\n" +
            "2015-11-30,134.820007,136.740005,132.289993,136.479996,4498800,136.479996\n" +
            "2015-11-23,136.289993,136.779999,134.119995,134.630005,2152300,134.630005\n" +
            "2015-11-16,131.820007,137.419998,131.470001,136.630005,3059600,136.630005\n" +
            "2015-11-09,136.00,136.190002,131.740005,131.960007,3450600,131.960007\n" +
            "2015-11-02,136.770004,138.619995,135.139999,136.330002,2935400,136.330002\n" +
            "2015-10-26,137.860001,138.50,135.070007,136.020004,2802700,136.020004\n" +
            "2015-10-19,133.50,138.00,133.100006,137.779999,2947900,137.779999\n" +
            "2015-10-12,133.399994,135.00,131.050003,133.809998,2847200,133.809998\n" +
            "2015-10-05,130.50,134.380005,130.380005,133.029999,3610000,133.029999\n" +
            "2015-09-28,128.589996,131.399994,127.459999,129.830002,4471300,129.830002\n" +
            "2015-09-21,130.00,131.309998,127.75,129.639999,3835200,129.639999\n" +
            "2015-09-14,131.149994,133.600006,128.940002,129.100006,5907600,129.100006\n" +
            "2015-09-08,132.020004,134.660004,130.199997,131.369995,4719000,131.369995\n" +
            "2015-08-31,135.089996,135.50,128.910004,129.639999,5085300,129.639999\n" +
            "2015-08-24,127.550003,136.309998,125.50,135.740005,7916800,135.740005\n" +
            "2015-08-17,142.00,142.460007,134.160004,134.220001,4553100,134.220001\n" +
            "2015-08-10,141.369995,143.580002,140.460007,142.570007,3812000,142.570007\n" +
            "2015-08-03,143.110001,143.990005,140.919998,143.550003,2522300,143.550003\n" +
            "2015-07-27,141.139999,143.75,140.419998,142.740005,2760900,142.740005\n" +
            "2015-07-20,143.960007,144.690002,141.190002,141.25,2569600,141.25\n" +
            "2015-07-13,140.800003,144.00,140.449997,143.880005,2942500,143.880005\n" +
            "2015-07-06,136.130005,140.300003,136.039993,139.729996,3718300,139.729996\n" +
            "2015-06-29,139.00,139.240005,136.080002,137.389999,4483100,137.389999\n" +
            "2015-06-22,142.00,142.539993,139.119995,139.779999,2824000,139.779999\n" +
            "2015-06-15,139.720001,142.960007,138.779999,140.960007,3627400,140.960007\n" +
            "2015-06-08,140.429993,141.610001,139.139999,140.289993,2794500,140.289993\n" +
            "2015-06-01,143.259995,144.070007,140.520004,140.759995,2858200,140.759995\n" +
            "2015-05-26,144.440002,145.149994,143.00,143.00,2878900,143.00\n" +
            "2015-05-18,145.389999,147.00,144.529999,144.600006,2324600,144.600006\n" +
            "2015-05-11,148.50,148.570007,144.020004,145.259995,2801900,145.259995\n" +
            "2015-05-04,144.949997,148.419998,142.940002,148.309998,3827100,148.309998\n" +
            "2015-04-27,142.279999,143.360001,140.589996,143.360001,3050900,143.360001\n" +
            "2015-04-20,141.690002,143.020004,141.089996,142.089996,2638600,142.089996\n" +
            "2015-04-13,143.279999,143.949997,140.330002,140.699997,3001300,140.699997\n" +
            "2015-04-06,143.00,144.449997,142.570007,143.50,3031600,143.50\n" +
            "2015-03-30,144.470001,146.119995,143.149994,143.559998,3244700,143.559998\n" +
            "2015-03-23,145.479996,146.289993,142.50,143.889999,3367000,143.889999\n" +
            "2015-03-16,144.919998,146.550003,143.940002,145.529999,3684300,145.529999\n" +
            "2015-03-09,145.50,146.529999,143.179993,143.970001,3524200,143.970001\n" +
            "2015-03-02,147.710007,147.979996,144.070007,145.279999,3785500,145.279999\n" +
            "2015-02-23,148.199997,149.389999,146.820007,147.410004,3057800,147.410004\n" +
            "2015-02-17,148.360001,149.369995,146.580002,148.720001,3148100,148.720001\n" +
            "2015-02-09,149.910004,150.509995,148.009995,148.339996,3083000,148.339996\n" +
            "2015-02-02,144.130005,151.630005,143.300003,150.179993,4162100,150.179993\n" +
            "2015-01-26,149.190002,149.729996,143.889999,143.910004,4554800,143.910004\n" +
            "2015-01-20,150.279999,150.600006,146.589996,149.119995,4445400,149.119995\n" +
            "2015-01-12,149.960007,150.990005,146.419998,149.210007,4367100,149.210007\n" +
            "2015-01-05,148.809998,151.690002,146.110001,149.470001,4017200,149.470001\n" +
            "2014-12-29,151.020004,152.669998,148.50,149.169998,3023300,149.169998\n" +
            "2014-12-22,151.979996,152.699997,151.089996,151.350006,3239500,151.350006\n" +
            "2014-12-15,147.970001,152.740005,144.75,151.559998,6369400,151.559998\n" +
            "2014-12-08,150.880005,152.940002,146.300003,146.460007,5048700,146.460007\n";


    @Test
    public void testAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Assert.assertEquals("com.xengar.android.stocktracker", appContext.getPackageName());
    }


    // Sample ContentValues
    static ContentValues createBerkshireHathawayIncQuoteValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(Contract.Quote.COLUMN_SYMBOL, TEST_STOCK);
        testValues.put(Contract.Quote.COLUMN_PRICE, 164.50);
        testValues.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, 3.35);
        testValues.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, 2.08);
        testValues.put(Contract.Quote.COLUMN_HISTORY, TEST_HISTORY_BRK_B_STOCK);

        return testValues;
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    // Compares valueCursor with expectedValues.
    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static long insertBerkshireHathawayIncQuoteValues(Context context) {
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createBerkshireHathawayIncQuoteValues();

        long locationRowId = db.insert(Contract.Quote.TABLE_NAME, null, testValues);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert Berkshire Hathaway Inc Quote Values",
                locationRowId != -1);
        return locationRowId;
    }



    /*
     * The functions inside of TestProvider use this utility class to test the ContentObserver
     * callbacks using the PollingCheck class that we grabbed from the Android CTS tests.
     * Note that this only tests that the onChange function is called; it does not test that the
     * correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {

        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

}
