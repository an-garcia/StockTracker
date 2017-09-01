/*
 * Copyright (C) 2017 Angel Garcia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xengar.android.stocktracker.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xengar.android.stocktracker.R;
import com.xengar.android.stocktracker.data.Contract;
import com.xengar.android.stocktracker.data.PrefUtils;

public class MainActivity extends AppCompatActivity implements StockFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        if (findViewById(R.id.stock_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            if (savedInstanceState == null) {
                DetailFragment fragment = new DetailFragment();
                String symbol = PrefUtils.getFirstStock(getApplicationContext());
                if (symbol != null ) {
                    // Display the fist symbol in the preferences.
                    Uri contentUri = Contract.Quote.makeUriForStock(symbol);
                    Bundle args = new Bundle();
                    args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
                    fragment.setArguments(args);
                }
                // In two-pane mode, show the detail view in this activity by adding or replacing
                // the detail fragment using a fragment transaction.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        }  else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
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
    public void onItemSelected(Uri contentUri, StockAdapter.StockViewHolder vh) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            ActivityCompat.startActivity(this, intent, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        SettingsActivity.GeneralPreferenceFragment.class.getName() );
                intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
                intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.action_settings);
                startActivity(intent);
            return true;

            case R.id.action_add:
                new AddStockDialog().show(getFragmentManager(), getString(R.string.dialog_class_name));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
