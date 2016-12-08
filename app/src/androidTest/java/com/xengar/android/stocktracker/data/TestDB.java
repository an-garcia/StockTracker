package com.xengar.android.stocktracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Database tables
 */
@RunWith(AndroidJUnit4.class)
public class TestDB {

    public static final String LOG_TAG = TestDB.class.getSimpleName();

    // Since we want each test to start with a clean slate
    private void deleteTheDatabase() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    @Before
    public void setUp() {
        deleteTheDatabase();
    }

    /**
     * Tests that the database exists and the quotes table has the correct columns.
     */
    @Test
    public void testCreateDb() throws Exception {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(Contract.Quote.TABLE_NAME);

        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(appContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the tables
        assertTrue("Error: Your database was created without the tables", tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + Contract.Quote.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(Contract.Quote._ID);
        locationColumnHashSet.add(Contract.Quote.COLUMN_SYMBOL);
        locationColumnHashSet.add(Contract.Quote.COLUMN_PRICE);
        locationColumnHashSet.add(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
        locationColumnHashSet.add(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);
        locationColumnHashSet.add(Contract.Quote.COLUMN_HISTORY);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required columns
        assertTrue("Error: The database doesn't contain all of the required columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /**
     * Tests that we can insert and query the database.
     */
    @Test
    public void testQuotesTable() {
        // Step 1: Get reference to writable database
        // If there's an error in the SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        Context appContext = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new DbHelper(appContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Step 2: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createBerkshireHathawayIncQuoteValues();

        // Step 3: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(Contract.Quote.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Step 4: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                Contract.Quote.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Step 5: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Step 6: Close Cursor and Database
        cursor.close();
        db.close();
    }

}
