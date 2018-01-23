package com.example.android.linker;


import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.linker.SQLdb.LinkContract;

/**
 * Extended {@link Fragment} for Linker.
 */
public class LinkFragment extends Fragment {

    /** Tag identifies the originating class of the log output. */
    private final String LOG_TAG = LinkFragment.class.getSimpleName();

    /** Unique identifier for the asynctaskloader. */
    private static final int LINK_INFO_ASYNC = 0;

    /** Unique identifier for the {@link CursorLoader}. */
    private static final int LINK_CURSOR_LOADER = 1;

    /** Unique identifier for the {@link android.support.v4.app.DialogFragment} that inserts data into the database. */
    public static final int INSERT_DIALOG = 0;

    /** Unique identifier for the {@link android.support.v4.app.DialogFragment} that deletes or updates data into the database. */
    public static final int QUERY_UPDATE_DIALOG = 1;

    /**
     * Loads the {@link android.support.v4.app.DialogFragment} that is show in focus in another thread by the end of
     * {@link android.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.content.Loader, Object)}.
     */
    private Handler handler;

    /** Used to reference the activity tied to the fragment. */
    private Context sContext;

    /** Will hold the url obtained through the {@link LinkFragment.SharedText} interface. */
    private String text;

    /**
     * LinkCursorAdapter extends {@link android.widget.CursorAdapter}.
     * It will populate our {@link ListView} with the data pointed to by the cursor.
     */
    private LinkCursorAdapter lCursorAdapter;

    /** TO DO FEATURE. Interface will be used to create folder fragments. */
    private ShowFolderFragment showFolderFragment;

    /** Interface will instantiate, initialize, and show a {@link android.support.v4.app.DialogFragment}. */
    private ShowDialog showDialog;

    /** Interface will initialize the text variable with the results of the {@link MainActivity} {@link Intent#getStringExtra(String)}. */
    private SharedText sharedUrl;

    /** Unique identifier for the {@link android.support.v4.app.DialogFragment} and it tells the dialog to populate its fields with data from the item's database. */
    public static final int DISPLAY_QUERY_UPDATE_INFO = 2;

    /** Unique identifier for the {@link android.support.v4.app.DialogFragment} and it tells the dialog to fill populate field with the data from the parsed URL. */
    public static final int DISPLAY_INSERT_INFO = 1;

    /** Instantiates a {@link LinkFragment}. */
    public static LinkFragment newInstance() {
        return new LinkFragment();
    }

    /** Used to verify whether {@link MainActivity} has implemented these interfaces. */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sContext = context;

        try {
            showFolderFragment = (ShowFolderFragment) sContext;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interface ShowFolderFragment.");
        }

        try {
            showDialog = (ShowDialog) sContext;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interface ShowDialog.");
        }

        try {
            sharedUrl = (SharedText) sContext;
        }
        catch ( ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interface DataBundle.");
        }
    }

    /** Loads the {@link CursorLoader}. */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LINK_CURSOR_LOADER, null, cursorLoader);
    }

    /** Creates and sets most of the UI elements. */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        lCursorAdapter = new LinkCursorAdapter(sContext, null);
        ListView linkListView = getActivity().findViewById(R.id.fragment_list_view); //fragment_list_view is the ListView located in the fragment_layout.xml under the layout directory.
        linkListView.setAdapter(lCursorAdapter);

        /** The URL of the item is queried and used to initialize an intent to open the URL in a browser. */
        linkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] projection = {
                        LinkContract.LinkEntry.COLUMN_LINK_URL,
                        LinkContract.LinkEntry.COLUMN_LINK_TITLE,
                        LinkContract.LinkEntry.COLUMN_LINK_IMAGE,
                };

                Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(LinkContract.LinkEntry.CONTENT_URI, l), projection, null, null, null);
                String actualUrl = "";

                if (cursor.moveToFirst()) {
                    int columnUrl = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_URL);
                    actualUrl = cursor.getString(columnUrl);
                }

                cursor.close();

                Intent viewUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(actualUrl));
                if (viewUrl.resolveActivity(sContext.getPackageManager()) != null) {
                    startActivity(viewUrl);
                }
            }
        });

        /** The URL, bitmap, and URL title are queried and and used to fill in the views of the custom dialog. The dialog can either delete or update the data of the item registered.*/
        linkListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle queryUpdateBundle = new Bundle();
                queryUpdateBundle.putInt("getDialog", QUERY_UPDATE_DIALOG);
                queryUpdateBundle.putInt("getDisplayInfo", DISPLAY_QUERY_UPDATE_INFO);
                queryUpdateBundle.putLong("getID", l);
                try {
                    showDialog.showDialog(queryUpdateBundle);
                }
                catch(Exception e) {
                    Toast.makeText(sContext,
                            e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
                return true;
            }
        });

        /**
         * Opens a dialog. If the text variable is null, the dialog's views will be empty. The user is free to populate the EditText themselves.
         * Otherwise, the views of the dialog  will be populate with the URL title and the bitmap parsed from the URL held by the text variable.
         */
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = sharedUrl.sharedText(true);

                if (text != null) {
                    getLoaderManager().initLoader(LINK_INFO_ASYNC, null, linkInfoLoad).forceLoad(); // Calls the AsyncTaskLoader and parses the URL outside the main/UI thread.
                }
                else {
                    Bundle emptyDialogBundle = new Bundle();
                    emptyDialogBundle.putInt("getDialog", INSERT_DIALOG);
                    try {
                        showDialog.showDialog(emptyDialogBundle); // Shows an empty dialog when text is null.
                    }
                    catch(Exception e) {
                        Toast.makeText(sContext,
                                e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });

        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    /**
     * The CursorLoader and CursorAdapter populates the fragment with ListViews of the database rows.
     * It also detects whether there has been any changes in the database and updates the ListViews.
     */
    private LoaderManager.LoaderCallbacks<Cursor> cursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {
                    LinkContract.LinkEntry._ID,
                    LinkContract.LinkEntry.COLUMN_LINK_URL,
                    LinkContract.LinkEntry.COLUMN_LINK_TITLE,
                    LinkContract.LinkEntry.COLUMN_LINK_IMAGE,
                    LinkContract.LinkEntry.COLUMN_LINK_DATA_TYPE,
                    LinkContract.LinkEntry.COLUMN_LINK_FOLDER_ID
            };

            return new CursorLoader( //Returns a cursor pointing to the data requested.
                    sContext,
                    LinkContract.LinkEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    LinkContract.LinkEntry.COLUMN_LINK_TITLE);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            lCursorAdapter.swapCursor(data); //Populates the CursorAdapter with the cursor returned from OnCreateLoader.
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            lCursorAdapter.swapCursor(null); //Dereferences the cursor.
        }
    };

    /** Populates a dialog with the URL, URL title, and bitmap parsed from the URL held by the text variable. */
    private LoaderManager.LoaderCallbacks<LinkInfo> linkInfoLoad = new LoaderManager.LoaderCallbacks<LinkInfo>() {
        @Override
        public Loader<LinkInfo> onCreateLoader(int id, Bundle args) {
            return new LinkInfoTask(sContext, text);
        }

        @Override
        public void onLoadFinished(Loader<LinkInfo> loader, LinkInfo data) {
            final Bundle loaderBundle = new Bundle();

            if (data.getImageBlob() != null) {
                loaderBundle.putByteArray("getByteArray", data.getImageBlob());
            }
            loaderBundle.putString("getTitle", data.getTitle());
            loaderBundle.putString("getUrl", data.getUrl());
            loaderBundle.putInt("getDialog", INSERT_DIALOG); // This is a dialog that will only enter data into the database.
            loaderBundle.putInt("getDisplayInfo", DISPLAY_INSERT_INFO); // Signals for a dialog populated with the parsed data.

            /** Opens a dialog with the data in another thread. Temporary solution until I find a cleaner way to do this. */
            handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    try {
                        showDialog.showDialog(loaderBundle);
                    }
                    catch(Exception e) {
                        Toast.makeText(sContext,
                                e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            };

            new Thread() {
                public void run() {
                    handler.sendEmptyMessage(0);
                }
            }.start();
            getLoaderManager().destroyLoader(LINK_INFO_ASYNC);
        }

        @Override
        public void onLoaderReset(Loader<LinkInfo> loader) {
        }
    };

    public interface ShowFolderFragment {
        void showFolderFragment(long id);
    }

    public interface ShowDialog {
        void showDialog(Bundle bundle);
    }

    public interface SharedText {
        String sharedText(boolean state);
    }
}

