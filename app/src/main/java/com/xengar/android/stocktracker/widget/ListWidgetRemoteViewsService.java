/*
 * Copyright (C) 2016 Angel Garcia
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
package com.xengar.android.stocktracker.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.xengar.android.stocktracker.R;
import com.xengar.android.stocktracker.Utility;
import com.xengar.android.stocktracker.data.Contract;

/**
 * RemoteViewsService controlling the data being shown in the scrollable stock list widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = ListWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] QUOTE_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
    };
    // these indices must match the projection
    static final int INDEX_QUOTE_ID = 0;
    static final int INDEX_QUOTE_SYMBOL = 1;
    static final int INDEX_QUOTE_PRICE = 2;
    static final int INDEX_QUOTE_ABSOLUTE_CHANGE = 3;
    static final int INDEX_QUOTE_PERCENTAGE_CHANGE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.uri,
                        QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                // Read the data
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                String symbol = data.getString(INDEX_QUOTE_SYMBOL);
                float price = data.getFloat(INDEX_QUOTE_PRICE);
                float absoluteChange = data.getFloat(INDEX_QUOTE_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(INDEX_QUOTE_PERCENTAGE_CHANGE);

                // Display the data
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);
                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_price, Utility.getPriceInDisplayMode(price));
                views.setTextViewText(R.id.widget_change, Utility.getChangeInDisplayMode(
                                getApplicationContext(), absoluteChange, percentageChange));

                // Set red rectangle for negative values, green otherwise
                if (absoluteChange > 0) {
                    views.setInt(R.id.widget_change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }

                // Create the intent in case of click on the item
                final Intent fillInIntent = new Intent();
                fillInIntent.setData(Contract.Quote.makeUriForStock(symbol));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTE_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
