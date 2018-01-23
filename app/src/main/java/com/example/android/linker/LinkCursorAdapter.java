package com.example.android.linker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.linker.SQLdb.LinkContract;

/**
 * Extended {@link CursorAdapter} for Linker. The {@link CursorAdapter} populates {@link android.widget.ListView} with the data pointed to by the cursor.
 */
public class LinkCursorAdapter extends CursorAdapter {

    /** Tag identifies the originating class of the log output */
    private static final String LOG_TAG = LinkCursorAdapter.class.getSimpleName();

    public LinkCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Inflates the view that will hold the data pointed to by the cursor.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Binds the view created by {@link CursorAdapter#newView(Context, Cursor, ViewGroup) to the data pointed to by the cursor.}
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView image = view.findViewById(R.id.web_thumbnail);
        TextView title = view.findViewById(R.id.web_title);
        TextView url = view.findViewById(R.id.web_url);

        int columnImage = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_IMAGE);
        int columnTitle = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_TITLE);
        int columnUrl = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_URL);

        byte[] byteImage = cursor.getBlob(columnImage);
        String urlTitle = cursor.getString(columnTitle);
        String actualUrl = cursor.getString(columnUrl);

        title.setText(urlTitle);
        url.setText(actualUrl);

        if(byteImage != null) {
            image.setImageBitmap(LinkInfo.convertToBitmap(byteImage));
        }
    }
}
