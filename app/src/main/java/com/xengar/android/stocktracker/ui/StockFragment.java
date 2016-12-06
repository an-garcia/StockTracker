package com.xengar.android.stocktracker.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xengar.android.stocktracker.R;
import com.xengar.android.stocktracker.Utility;
import com.xengar.android.stocktracker.data.Contract;
import com.xengar.android.stocktracker.data.PrefUtils;
import com.xengar.android.stocktracker.sync.QuoteSyncJob;

import timber.log.Timber;

/**
 * Encapsulates fetching the stocks and displaying it as a {@link android.support.v7.widget.RecyclerView} layout.
 */
public class StockFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler{

    public static final String LOG_TAG = StockFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private Context mContext;

    private static final int STOCK_LOADER = 0;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView error;
    private StockAdapter adapter;

    private boolean waitingStockCheck = false;
    // Broadcast receiver for the invalid stock
    private BroadcastReceiver stockCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String symbol = intent.getStringExtra(Contract.Quote.COLUMN_SYMBOL);
            Toast.makeText(getActivity().getApplication().getApplicationContext(),
                    getString(R.string.error_stock_not_found, symbol),
                    Toast.LENGTH_LONG).show();
            // As it's invalid remove it from the preferences
            PrefUtils.removeStock(context, symbol);
        }
    };


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, StockAdapter.StockViewHolder vh);
    }

    public StockFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = inflater.getContext();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        error = (TextView) rootView.findViewById(R.id.error);
        // Set the layout manager
        adapter = new StockAdapter(mContext, this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));


        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        //onRefresh();

        QuoteSyncJob.initialize(mContext);
        getLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(getActivity(), symbol);
                mContext.getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(mRecyclerView);

        return rootView;
    }

    /**
     *  Instantiate and return a new Loader for the given ID.
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.uri,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    /**
     * Called when onCreateLoader(int, Bundle) has finished its load.
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);

        // Unregister the stock check
        if (waitingStockCheck) {
            getActivity().unregisterReceiver(stockCheckReceiver);
            waitingStockCheck = false;
        }
    }

    /**
     *  Called when a previously created loader in onCreateLoader(int, Bundle) is being reset,
     *  thus making its data unavailable
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);

        // Unregister the stock check
        if (waitingStockCheck) {
            getActivity().unregisterReceiver(stockCheckReceiver);
            waitingStockCheck = false;
        }
    }


    /**
     * Adds the symbol to the preferences and calls for sync.
     *
     * @param symbol
     */
    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (Utility.networkUp(mContext)) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(mContext, symbol);
            QuoteSyncJob.syncImmediately(mContext);

            // Register for receiving a message that the stock is invalid
            IntentFilter filter = new IntentFilter();
            filter.addAction(QuoteSyncJob.ACTION_INVALID_STOCK);
            mContext.registerReceiver(stockCheckReceiver, filter);
            waitingStockCheck = true;
        }
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(mContext);

        if (!Utility.networkUp(mContext) && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!Utility.networkUp(mContext)) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(mContext).size() == 0) {
            Timber.d(getString(R.string.msg_panic));
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(String symbol) {
        Timber.d(getString(R.string.msg_symbol_clicked, symbol));
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(getActivity())
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity_settings, menu);

        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(getActivity());
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

}
