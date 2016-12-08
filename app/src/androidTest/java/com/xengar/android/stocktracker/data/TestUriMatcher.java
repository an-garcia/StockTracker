package com.xengar.android.stocktracker.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Test your UriMatcher.  Note that this class utilizes constants that are declared with package
 * protection inside of the UriMatcher, which is why the test must be in the same data package
 * as the Android app code.  Doing the test this way is a compromise between data hiding and testability.
 */
@RunWith(AndroidJUnit4.class)
public class TestUriMatcher {

    private static final String TEST_STOCK = "BRK-B";

    // content://com.xengar.android.stocktracker/quote"
    private static final Uri TEST_QUOTE_DIR = Contract.Quote.uri;
    private static final Uri TEST_QUOTE_WITH_SYMBOL_DIR = Contract.Quote.makeUriForStock(TEST_STOCK);


    /**
     * Tests that UriMatcher returns the correct integer value for each of the Uri types that
     *  our ContentProvider can handle.
     */
    @Test
    public void testUriMatcher() {
        UriMatcher testMatcher = StockProvider.buildUriMatcher();

        assertEquals("Error: The QUOTE URI was matched incorrectly.",
                testMatcher.match(TEST_QUOTE_DIR), StockProvider.QUOTE);
        assertEquals("Error: The QUOTE WITH SYMBOL URI was matched incorrectly.",
                testMatcher.match(TEST_QUOTE_WITH_SYMBOL_DIR), StockProvider.QUOTE_FOR_SYMBOL);
    }

    /**
     * Tests that the stock can be retrieved from the uri.
     */
    @Test
    public void testStockFromUri(){
        String stock = Contract.Quote.getStockFromUri(TEST_QUOTE_WITH_SYMBOL_DIR);
        assertEquals("Error: The QUOTE stock was matched incorrectly.", stock, TEST_STOCK);
    }

}
