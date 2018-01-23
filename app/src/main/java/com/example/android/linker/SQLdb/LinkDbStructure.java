package com.example.android.linker.SQLdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.linker.SQLdb.LinkContract.LinkEntry;

/**
 * Lays the structure and creates a database for the scraped HTML data.
  */
public class LinkDbStructure extends SQLiteOpenHelper {

    /** Database name */
    private static final String DATABASE_NAME = "linker.db";

    /** Database version */
    private static final int DATABASE_VERSION = 1;

    /** Defines the database schema and creates and upgrades it.*/
    public LinkDbStructure(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Creates the database using the schema defined. */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String LINK_DATABASE_SCHEMA = "CREATE TABLE " + LinkEntry.TABLE_NAME + " ("
                + LinkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LinkEntry.COLUMN_LINK_DATA_TYPE + " INTEGER NOT NULL DEFAULT(0), "
                + LinkEntry.COLUMN_LINK_FOLDER_ID + " INTEGER, "
                + LinkEntry.COLUMN_LINK_TITLE + " TEXT, "
                + LinkEntry.COLUMN_LINK_URL + " TEXT NOT NULL, "
                + LinkEntry.COLUMN_LINK_IMAGE + " BLOB);";

        /* It executes a single SQL statement that is not a SELECT statement or any other SQL statement that returns data. */
        sqLiteDatabase.execSQL(LINK_DATABASE_SCHEMA);
    }

    /** Will update the database schema to the newer version in the constructor argument. */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //TO DO
    }
}
