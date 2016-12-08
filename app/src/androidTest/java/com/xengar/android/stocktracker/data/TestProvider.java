package com.xengar.android.stocktracker.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This is not a complete set of tests of the Weather ContentProvider, but it does test
 * that at least the basic functionality has been implemented correctly.
 */
@RunWith(AndroidJUnit4.class)
public class TestProvider {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
     * Helper function to delete all records from the database table using the ContentProvider.
     * Queries the ContentProvider to make sure that the database has been successfully deleted.
     */
    public void deleteAllRecordsFromProvider() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        appContext.getContentResolver().delete(Contract.Quote.uri, null, null );

        Cursor cursor = appContext.getContentResolver().query(
                Contract.Quote.uri,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
     * Helper function to delete all records from the database table using the database functions only.
     */
    public void deleteAllRecordsFromDB() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new DbHelper(appContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(Contract.Quote.TABLE_NAME, null, null);
        db.close();
    }


    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Before
    public void setUp(){
        deleteAllRecordsFromProvider();
    }

    /*
     * Checks to make sure that the content provider is registered correctly.
     */
    @Test
    public void testProviderRegistry() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        PackageManager pm = appContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(appContext.getPackageName(),
                StockProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: StockProvider registered with authority: " + providerInfo.authority
                    + " instead of authority: " + Contract.AUTHORITY,
                    providerInfo.authority, Contract.AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: StockProvider not registered at " + appContext.getPackageName(),
                    false);
        }
    }

    /*
     * Uses the database directly to insert and then uses the ContentProvider to read out the data.
     */
    @Test
    public void testBasicQuoteQueries() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(appContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createBerkshireHathawayIncQuoteValues();
        long quoteRowId = TestUtilities.insertBerkshireHathawayIncQuoteValues(appContext);

        // Test the basic content provider query
        Cursor quoteCursor = appContext.getContentResolver().query(
                Contract.Quote.uri,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicQuoteQueries, quote query", quoteCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Quote Query did not properly set NotificationUri",
                    quoteCursor.getNotificationUri(), Contract.Quote.uri);
        }
    }

    /*
     * This test uses the provider to insert and then update the data.
     */
    @Test
    public void testUpdateQuote() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createBerkshireHathawayIncQuoteValues();

        Uri quoteUri = appContext.getContentResolver().
                insert(Contract.Quote.uri, values);
        long quoteRowId = ContentUris.parseId(quoteUri);

        // Verify we got a row back.
        assertTrue(quoteRowId != -1);
        Log.d(LOG_TAG, "New row id: " + quoteRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(Contract.Quote._ID, quoteRowId);
        updatedValues.put(Contract.Quote.COLUMN_PRICE, 170.50);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = appContext.getContentResolver().query(Contract.Quote.uri, null,
                null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = appContext.getContentResolver().update(
                Contract.Quote.uri, updatedValues, Contract.Quote._ID + "= ?",
                new String[] { Long.toString(quoteRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        // If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = appContext.getContentResolver().query(
                Contract.Quote.uri,
                null,   // projection
                Contract.Quote._ID + " = " + quoteRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateQuote.  Error validating quote entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    @Test
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createBerkshireHathawayIncQuoteValues();

        Context appContext = InstrumentationRegistry.getTargetContext();
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        appContext.getContentResolver().registerContentObserver(Contract.Quote.uri, true, tco);
        Uri locationUri = appContext.getContentResolver().insert(Contract.Quote.uri, testValues);

        // Did our content observer get called?  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        appContext.getContentResolver().unregisterContentObserver(tco);

        long quoteRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(quoteRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = appContext.getContentResolver().query(
                Contract.Quote.uri,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating Quote entry.",
                cursor, testValues);
    }

    // Make sure we can still delete after adding/updating stuff
    @Test
    public void testDeleteRecords() {
        testInsertReadProvider();

        Context appContext = InstrumentationRegistry.getTargetContext();
        // Register a content observer for our quote delete.
        TestUtilities.TestContentObserver quoteObserver = TestUtilities.getTestContentObserver();
        appContext.getContentResolver().registerContentObserver(Contract.Quote.uri, true, quoteObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        quoteObserver.waitForNotificationOrFail();
        appContext.getContentResolver().unregisterContentObserver(quoteObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static private final String[] BULK_STOCKS = {"MMM", "ABT", "ABBV", "ACN", "APD", "AYI", "AET",
            "AMZN", "AXP", "BA"};
    static ContentValues[] createBulkInsertQuoteValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues quoteValues = TestUtilities.createBerkshireHathawayIncQuoteValues();
            quoteValues.put(Contract.Quote.COLUMN_SYMBOL, BULK_STOCKS[i]);
            quoteValues.put(Contract.Quote.COLUMN_PRICE, 100 + 10 * (i+1));
            returnContentValues[i] = quoteValues;
        }
        return returnContentValues;
    }

    @Test
    public void testBulkInsert() {
        // Now we can bulkInsert some quotes. With ContentProviders, you really only have to
        // implement the features you use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertQuoteValues();
        Context appContext = InstrumentationRegistry.getTargetContext();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver quoteObserver = TestUtilities.getTestContentObserver();
        appContext.getContentResolver().registerContentObserver(Contract.Quote.uri, true, quoteObserver);

        int insertCount = appContext.getContentResolver().bulkInsert(Contract.Quote.uri, bulkInsertContentValues);

        // If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        quoteObserver.waitForNotificationOrFail();
        appContext.getContentResolver().unregisterContentObserver(quoteObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = appContext.getContentResolver().query(
                Contract.Quote.uri,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                Contract.Quote.COLUMN_PRICE + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating QuoteEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }

}
