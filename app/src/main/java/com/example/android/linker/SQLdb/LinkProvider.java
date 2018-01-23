package com.example.android.linker.SQLdb;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;

/**
 * Extended {@link ContentProvider} for Linker.
 */

@SuppressWarnings("ConstantConditions")
public class LinkProvider extends ContentProvider {

    /** Tag identifies the originating class of the log output */
    public static final String LOG_TAG = LinkProvider.class.getSimpleName();

    /** Constant value that represents the entire link database when implemented in the {@link UriMatcher}. */
    private static final int ENTIRE_LINK_DB= 1;

    /** Constant value that represents a row of the database when implemented in the {@link UriMatcher}. */
    private static final int SINGLE_LINK_ROW = 2;

    /** Used to match URI in {@link ContentProvider} */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** Extended {@link android.database.sqlite.SQLiteOpenHelper} class. */
    private static LinkDbStructure linkDb;

    /*
     * This is a static initializer.
     * The contents of the block are executed when the class is initialized, alongside any other fields.
     */
    static {
        sUriMatcher.addURI(LinkContract.CONTENT_AUTHORITY, LinkContract.LinkEntry.APPEND_LINK_PATH, ENTIRE_LINK_DB);
        sUriMatcher.addURI(LinkContract.CONTENT_AUTHORITY, LinkContract.LinkEntry.APPEND_LINK_PATH + "/#", SINGLE_LINK_ROW);
    }

    /**
     * Initializes the content provider on start up.
     * This is done to prevent unnecessary initialization of the database until the content provider is used.
     */
    @Override
    public boolean onCreate() {
        linkDb = new LinkDbStructure(getContext());
        return true;
    }

    /** Handles query results from the {@link android.content.ContentResolver}. */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = linkDb.getReadableDatabase(); // Query just reads the database. It does not modify.
        Cursor cursor;

        /*
         * If the argument is a URI for the entire database, then sUriMatcher will return 1.
         * If the uri is for a row in the Link database, then sUriMatcher will return 2.
         */
        int match = sUriMatcher.match(uri);

        /* Depending on the URI, query will return an entire database in a cursor or just a single row */
        switch(match) {
            case ENTIRE_LINK_DB:
                cursor = db.query(LinkContract.LinkEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_LINK_ROW:
                selection = LinkContract.LinkEntry._ID + "=?"; // The ID parsed below is substituted into the question mark to identify the row ID.
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))}; //Parses the URI for the ID.
                cursor = db.query(LinkContract.LinkEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unable to query URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri); //Notifies the CursorLoader data has been altered.
        return cursor;
    }

    /**
     * Returns the MIME type of data of the URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case ENTIRE_LINK_DB:
                return LinkContract.LinkEntry.ALL_LINK_ENTRY;
            case SINGLE_LINK_ROW:
                return LinkContract.LinkEntry.SINGLE_LINK_ENTRY;
            default:
                throw new IllegalArgumentException("getType - Incompatible URI: " + uri);
        }
    }

    /** Handles the request to a insert row from the {@link android.content.ContentResolver}. */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        SQLiteDatabase db = linkDb.getWritableDatabase(); //Insert modifies the database data, therefore db needs to be a writable.

        /*
         * Validates the url put into the ContentValues.
         */
        validUrl(contentValues);

        /*
         * Checks whether the inserted database entry is a folder or a link.
         */
        Integer dataType = contentValues.getAsInteger(LinkContract.LinkEntry.COLUMN_LINK_DATA_TYPE);
        if(dataType == null || !LinkContract.LinkEntry.isDataTypeValid(dataType)) {
            throw new IllegalArgumentException("Error registering data type");
        }

        long rowId = db.insert(LinkContract.LinkEntry.TABLE_NAME, null, contentValues);

        if(rowId == -1) {
            throw new IllegalArgumentException("Failed to insert row for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, rowId);
    }

    /** Handles the delete request(s) of the {@link android.content.ContentResolver}. */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = linkDb.getWritableDatabase(); //Delete modifies the database data, therefore db needs to be a writable.
        int rowsDeleted;
        int match = sUriMatcher.match(uri);

        switch(match){
            case ENTIRE_LINK_DB:
                rowsDeleted = db.delete(LinkContract.LinkEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SINGLE_LINK_ROW:
                selection = LinkContract.LinkEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(LinkContract.LinkEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("No rows were deleted due to incorrect " + uri);
        }
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /** Handles the update request(s) of the {@link android.content.ContentResolver}. */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowNumbersUpdated;
        SQLiteDatabase db = linkDb.getWritableDatabase(); //Update modifies the database data, therefore db needs to be a writable.

        validUrl(contentValues);

        final int match = sUriMatcher.match(uri);
        switch(match) {
            case SINGLE_LINK_ROW:
                selection = LinkContract.LinkEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf((ContentUris.parseId(uri)))};
                rowNumbersUpdated = db.update(LinkContract.LinkEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rowNumbersUpdated;
            default:
                throw new IllegalArgumentException("Updating rows failed.");
        }
    }

    /**
     * Validates whether the URL provided follows the correct syntax.
     * @param contentValues
     */
    private void validUrl(ContentValues contentValues) {
        if(contentValues.containsKey(LinkContract.LinkEntry.COLUMN_LINK_URL)) {
            String validUrl = contentValues.getAsString(LinkContract.LinkEntry.COLUMN_LINK_URL);
            if (!(Patterns.WEB_URL.matcher(validUrl).matches()))
                throw new IllegalArgumentException(validUrl + " is an invalid URL.");
        } else {
            throw new NullPointerException("Url key is missing.");
        }
    }
}
