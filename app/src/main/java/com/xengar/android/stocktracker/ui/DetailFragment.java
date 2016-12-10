package com.xengar.android.stocktracker.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.xengar.android.stocktracker.R;
import com.xengar.android.stocktracker.Utility;
import com.xengar.android.stocktracker.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        OnChartGestureListener,
        OnChartValueSelectedListener {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String STOCK_SHARE_HASHTAG = " #StockTrackerApp";
    private String mStockHistory;
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
    //For documentation https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started
    private LineChart mChart;


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
        mChart = (LineChart) rootView.findViewById(R.id.chart);
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, mStockHistory + STOCK_SHARE_HASHTAG);
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
            fillViewData(data);
            fillChart(data);
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
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        long milliSeconds = Long.parseLong(dateInMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String dateString = formatter.format(calendar.getTime());
        return dateString;
    }

    // Fill view data
    private void fillViewData(Cursor data)
    {
        String symbol = data.getString(COL_QUOTE_SYMBOL);
        float price = data.getFloat(COL_QUOTE_PRICE);
        float absoluteChange = data.getFloat(COL_QUOTE_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(COL_QUOTE_PERCENTAGE_CHANGE);

        // Display the data
        mSymbolView.setText(symbol);
        mPriceView.setText(Utility.getPriceInDisplayMode(price));
        mChangeView.setText(
                Utility.getChangeInDisplayMode(getContext(), absoluteChange, percentageChange));
    }


    /**
     * Custom implementation of the MarkerView.
     */
    public class MyMarkerView extends MarkerView {

        private TextView tvContent;
        private List<HistoricalPrice> historicalPrices;

        public MyMarkerView(Context context, int layoutResource,
                            List<HistoricalPrice> historicalPrices) {
            super(context, layoutResource);
            tvContent = (TextView) findViewById(R.id.tvContent);
            this.historicalPrices = historicalPrices;
        }

        // callbacks every time the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof CandleEntry) {
                CandleEntry ce = (CandleEntry) e;
                tvContent.setText(historicalPrices.get((int)e.getX()).getDate()
                        + "  " + Utility.getPriceInDisplayMode(ce.getHigh()));
            } else {
                tvContent.setText(historicalPrices.get((int)e.getX()).getDate()
                        + "  " + Utility.getPriceInDisplayMode(e.getY()));
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }

    // Fill the chart
    // See https://github.com/PhilJay/MPAndroidChart/wiki/Setting-Data
    private void fillChart(Cursor data)
    {
        String history = data.getString(COL_QUOTE_HISTORY);
        List<HistoricalPrice> historicalPrices = parseStockHistory(history);
        List<Entry> entries = new ArrayList<Entry>();
        for (int i=0; i < historicalPrices.size(); i++) {
            HistoricalPrice item = historicalPrices.get(i);
            // turn your data into Entry objects (x, y)
            entries.add(new Entry(i, item.getPrice()));
        }
        LineDataSet dataSet = new LineDataSet(entries, data.getString(COL_QUOTE_SYMBOL)); // add entries to dataset
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(1f);
        dataSet.setCircleRadius(2f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFormLineWidth(1f);
        dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        dataSet.setFormSize(15.f);

        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_red);
            dataSet.setFillDrawable(drawable);
        }
        else {
            dataSet.setFillColor(Color.parseColor("#FFCDD2"));
        }

        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData); //  add values (data) to the chart,

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // create a custom MarkerView (extend MarkerView) and specify the layout to use
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view,
                historicalPrices);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        mChart.getAxisRight().setEnabled(false);
        //mChart.animateX(250);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.CIRCLE);

        mChart.invalidate(); // refresh
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Timber.d("Gesture START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Timber.d("Gesture END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Timber.d("Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Timber.d("Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Timber.d("Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Timber.d("Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Timber.d("Scale / Zoom: ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Timber.d("Translate / Move:  dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Timber.d("Entry selected " + e.toString());
        Timber.d("LOWHIGH low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Timber.d("MIN MAX xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax()
                + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Timber.d("Nothing selected.");
    }
}
