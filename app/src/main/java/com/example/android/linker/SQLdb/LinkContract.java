package com.example.android.linker.SQLdb;

/**
 * Created by moeh on 06/08/17.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for Linker app. Provides constant values for database entry.
 */
public final class LinkContract {

    /*
     * To prevent instantiating of the contract class, set the access modifier to be private.
     */
    private LinkContract() {}

    /** The content authority of the content uri*/
    public static final String CONTENT_AUTHORITY = "com.example.android.linker";

    /**
     * Base content uri consisting of the scheme and {@link #CONTENT_AUTHORITY}
     * Each database will append their unique path to the base uri.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Inner class details constant values of the link table.
     */
    public static final class LinkEntry implements BaseColumns {

        /** The path of the table of the {@link LinkEntry} class*/
        public static final String APPEND_LINK_PATH = "link";

        /** The content uri that provides access to the link database */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, APPEND_LINK_PATH);

        /**
         * Custom MIME data type of the {@link #CONTENT_URI} used by content providers.
         * The constant defines a type that is a cursor with multiple data entries.
         * The subtype is our linker database.
         */
        public static final String ALL_LINK_ENTRY = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + APPEND_LINK_PATH;

        /**
         * Custom MIME data type of the {@link #CONTENT_URI} used by content providers.
         * The constant defines a type that is a cursor with a single data entry.
         * The subtype is our linker database.
         */
        public static final String SINGLE_LINK_ENTRY = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + APPEND_LINK_PATH;

        /**
         * Database table name for the HTML scrapped data
         * Type: INTEGER
         */
        public static final String TABLE_NAME = "link";

        /** Unique ID used to identify the table rows*/
        public static final String _ID = BaseColumns._ID;

        /**
         * Type used to distinguish whether the data represents
         * a folder or link.
         */
        public static final String COLUMN_LINK_DATA_TYPE = "type";

        /** Values representing the type of data */
        public static final int TYPE_LINK = 0;
        public static final int TYPE_FOLDER = 1;

        /** Title of the folder or HTML scrapped data(link)*/
        public static final String COLUMN_LINK_TITLE = "title";

        /** Url obtained from the HTML scrapped data */
        public static final String COLUMN_LINK_URL = "url";

        /** Image url obtained from scrapped data */
        public static final String COLUMN_LINK_IMAGE = "image";

        /** Date of insertion into the database */
        public static final String COLUMN_LINK_DATE = "date";

        /** Name of parent folder */
        public static final String COLUMN_LINK_FOLDER_ID = "folder_id";

        /**
         * Confirms whether the data is either a folder or a link
         * @param data
         * @return
         */
        public static boolean isDataTypeValid (int data) {
            if (data == TYPE_FOLDER || data == TYPE_LINK) {
                return true;
            }
            return false;
        }
    }




}
