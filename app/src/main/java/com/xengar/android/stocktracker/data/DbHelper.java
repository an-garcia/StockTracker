package com.xengar.android.stocktracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xengar.android.stocktracker.data.Contract.Quote;

/**
 * Manages a local database for stock data.
 */
public class DbHelper extends SQLiteOpenHelper {

    static final String NAME = "StockTracker.db";
    // If you change the database schema, you must increment the database version.
    private static final int VERSION = 1;

    public DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " (" +
                Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Quote.COLUMN_SYMBOL + " TEXT NOT NULL, " +
                Quote.COLUMN_PRICE + " REAL NOT NULL, " +
                Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, " +
                Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, " +
                Quote.COLUMN_HISTORY + " TEXT NOT NULL, " +
                "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);
        onCreate(db);
    }
}
