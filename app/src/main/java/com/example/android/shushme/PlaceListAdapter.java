package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.location.places.PlaceBuffer;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    public static final String TAG = PlaceListAdapter.class.getSimpleName();

    private Context mContext;
    private PlaceBuffer mPlaces;

    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    public PlaceListAdapter(Context context, PlaceBuffer places) {
        this.mContext = context;
        this.mPlaces = places;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item
     *
     * @param parent   The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new PlaceViewHolder that holds a View with the item_place_card layout
     */
    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_place_card, parent, false);

        return new PlaceViewHolder(view);
    }

    /**
     * Binds the data from a particular position in the cursor to the corresponding view holder
     *
     * @param holder   The PlaceViewHolder instance corresponding to the required position
     * @param position The current position that needs to be loaded with data
     */
    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, final int position) {
        String placeName = mPlaces.get(position).getName().toString();
        final String placeAddress = mPlaces.get(position).getAddress().toString();
        holder.nameTextView.setText(placeName);
        holder.nameTextView.setText(placeAddress);
    }

    public void swapPlaces(PlaceBuffer newPlaces) {
        mPlaces = newPlaces;
        if (mPlaces != null) {
            this.notifyDataSetChanged();
        }
    }

    /**
     * Returns the number of items in the cursor
     *
     * @return Number of items in the cursor, or 0 if null
     */
    @Override
    public int getItemCount() {
        return mPlaces == null ? 0 : this.mPlaces.getCount();
    }

    /**
     * PlaceViewHolder class for the recycler view item
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView addressTextView;

        PlaceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            addressTextView = itemView.findViewById(R.id.address_text_view);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, view);
                    popup.inflate(R.menu.places_item_context_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.delete:
                                    String[] projection = { PlaceContract.PlaceEntry._ID };
                                    String selection = PlaceContract.PlaceEntry.COLUMN_PLACE_ID + " = ?";
                                    String[] selectionArgs = { mPlaces.get(getAdapterPosition()).getId() };

                                    Uri queryUri = PlaceContract.PlaceEntry.CONTENT_URI;
                                    Cursor c = mContext.getContentResolver().query(queryUri, projection, selection, selectionArgs, null);

                                    if (c != null && c.moveToFirst()) {
                                        int id = c.getInt(c.getColumnIndexOrThrow(PlaceContract.PlaceEntry._ID));
                                        Uri deleteUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI, id);
                                        int placesDeleted = mContext.getContentResolver().delete(deleteUri, null, null);

                                        if (placesDeleted != 0) {
                                            Toast.makeText(mContext, "Deleted " + nameTextView.getText(), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e(TAG, "Failed to delete item.");
                                    }

                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

                    popup.show();
                    return true;
                }
            });

        }

    }
}
