package com.xengar.android.stocktracker.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.xengar.android.stocktracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements StockFragment.Callback {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);

        StockFragment stockFragment =  ((StockFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_stock));
    }

    public void button(View view) {
        new AddStockDialog().show(getFragmentManager(), getString(R.string.dialog_class_name));
    }

    /**
     * Adds the symbol to the preferences and calls for sync.
     *
     * @param symbol
     */
    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            StockFragment stockFragment = ((StockFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_stock));
            stockFragment.addStock(symbol);
        }
    }

    @Override
    public void onItemSelected(Uri dateUri, StockAdapter.StockViewHolder vh) {
        // TODO: Change into detail view
    }
}
