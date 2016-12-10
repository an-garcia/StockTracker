package com.xengar.android.stocktracker.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.xengar.android.stocktracker.R;
import com.xengar.android.stocktracker.Utility;
import com.xengar.android.stocktracker.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String STOCK_SHARE_HASHTAG = " #StockTrackerApp";
    private String mForecast;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };
    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these must change.
    public static final int COL_QUOTE_ID = 0;
    public static final int COL_QUOTE_SYMBOL = 1;
    public static final int COL_QUOTE_PRICE = 2;
    public static final int COL_QUOTE_ABSOLUTE_CHANGE = 3;
    public static final int COL_QUOTE_PERCENTAGE_CHANGE = 4;
    public static final int COL_QUOTE_HISTORY = 5;

    // View elements
    private TextView mSymbolView;
    private TextView mPriceView;
    private TextView mChangeView;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
        mSymbolView = (TextView) rootView.findViewById(R.id.detail_symbol_textview);
        mPriceView = (TextView) rootView.findViewById(R.id.detail_price_textview);
        mChangeView = (TextView) rootView.findViewById(R.id.detail_change_textview);

        return rootView;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createQuoteHistoryIntent());
    }

    private Intent createQuoteHistoryIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + STOCK_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if ( getActivity() instanceof DetailActivity ){
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }
    }

    void onQuoteChanged(String newStock) {
        // replace the uri, since the stock has changed
        if (null != mUri) {
            mUri = Contract.Quote.makeUriForStock(newStock);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView ) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView ) {
                ((View)vp).setVisibility(View.VISIBLE);
            }

            // Read data fom the cursor and display it
            String symbol = data.getString(COL_QUOTE_SYMBOL);
            float price = data.getFloat(COL_QUOTE_PRICE);
            float absoluteChange = data.getFloat(COL_QUOTE_ABSOLUTE_CHANGE);
            float percentageChange = data.getFloat(COL_QUOTE_PERCENTAGE_CHANGE);
            String history = data.getString(COL_QUOTE_HISTORY);

            List<HistoricalPrice> historicalPrices = parseStockHistory(history);

            // Display the data
            mSymbolView.setText(symbol);
            mPriceView.setText(Utility.getPriceInDisplayMode(price));
            mChangeView.setText(
                    Utility.getChangeInDisplayMode(getContext(), absoluteChange, percentageChange));
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);
        if ( null != toolbarView ) {
            Menu menu = toolbarView.getMenu();
            if ( null != menu ) menu.clear();
            toolbarView.inflateMenu(R.menu.detailfragment);
            finishCreatingMenu(toolbarView.getMenu());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }


    // Represents a Time and Price for a stock
    private class HistoricalPrice{
        private String date;
        private float price;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public float getPrice() { return price; }
        public void setPrice(float price) { this.price = price; }

        public HistoricalPrice(String date, float price){
            this.date = date;
            this.price = price;
        }
    }

    /**
     * Returns a list of historical prices.
     *
     * @param history The format is "1480917600000, 20.455\n" for each quote with timeinMilliseconds
     * @return
     */
    private List<HistoricalPrice> parseStockHistory(String history) {
        List<HistoricalPrice> quotes = new ArrayList<>();

        String lineStr[] = history.split("\\r\\n|\\n|\\r");
        for(String str: lineStr){
            String itemStr[] = str.split(",");
            String date = getCalenderFromString(itemStr[0]);
            float price = Float.valueOf(itemStr[1]);
            // Add in reverse order because the dates are inverted
            quotes.add(0, new HistoricalPrice(date, price) );
        }
        return quotes;
    }

    // Get date string from Milliseconds.
    private String getCalenderFromString(String dateInMillis){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        //Date date = new Date(dateInMillis);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        long milliSeconds = Long.parseLong(dateInMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String dateString = formatter.format(calendar.getTime());
        return dateString;
    }
}
