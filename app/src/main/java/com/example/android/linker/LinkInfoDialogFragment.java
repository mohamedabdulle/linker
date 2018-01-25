package com.example.android.linker;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.linker.SQLdb.LinkContract;

/** Opens a dialog when the floating action button or an item is long clicked. */
public class LinkInfoDialogFragment extends DialogFragment {

    /** Tag identifies the originating class of the log output. */
    private final String LOG_TAG = LinkInfoDialogFragment.class.getSimpleName();

    /** Holds the dialog view. */
    private View view;

    /** Used to build a dialog. */
    private AlertDialog alertDialog;

    /** References the thumbnail/{@link ImageView} portion of the dialog. */
    private ImageView dialogThumbnail;

    /** References the {@link android.widget.EditText) title of the dialog. */
    private EditText dialogTitle;

    /** References the {@link android.widget.EditText) URL of the dialog. */
    private EditText dialogUrl;

    /** Used to modify the text held by the {@link com.example.android.linker.LinkFragment.SharedText}. */
    private LinkFragment.SharedText sharedUrl;

    /**
     * Instantiates and initialized a LinkInfoDialogFragment object.
     * @param bundle
     * @return dialog
     */
    public static LinkInfoDialogFragment newInstance(Bundle bundle) {
        LinkInfoDialogFragment dialog = new LinkInfoDialogFragment();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Creates a specific dialog based on the identifier passed in the bundle.
     * @param savedInstanceState
     * @return dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);

        /**
         *  There are three forms of dialog.
         *  1. A dialog that has its field empty. Match value is 0.
         *  Activated when the {@link com.example.android.linker.LinkFragment.SharedText} text is null and floating action button is pressed.
         *  2. A dialog with fields populated with the URL, URL title, and URL thumbnail. Match value is 0 with {@link LinkFragment.DISPLAY_INSERT_INFO} value being 1.
         *  3. A dialog with the fields populated by the the database contents of a specific item. The goal is to update or delete the item's database contents.
         *  Match value is 1. {@link LinkFragment.DISPLAY_QUERY_UPDATE_INFO} value is 2.
         */
        final int match = this.getArguments().getInt("getDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null));

        switch(match) {
            /**
             * Match value is {@link LinkFragment.INSERT_DIALOG}. Shows a dialog that will have its fields
             * populated with either data parsed from a URL when the getDisplayInfo key is equal to {@link LinkFragment.DISPLAY_INSERT_INFO}.
             * Or the dialog's fields will be empty and the user can input their own URL.
             */
            case LinkFragment.INSERT_DIALOG:
                builder.setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String emptyUrl = ((EditText) alertDialog.findViewById(R.id.edit_web_url)).getText().toString();
                        if (!emptyUrl.equals("")) { // If the URL dialog field is empty, don't do anything.
                            ContentValues values = new ContentValues();
                            int num = 0;

                            //makes sure to add https when user inserts a URL starting with www.
                            String urlFix = ((EditText) alertDialog.findViewById(R.id.edit_web_url)).getText().toString();
                            if (!urlFix.startsWith("http://") && !urlFix.startsWith("https://")) {
                                urlFix = "https://" + urlFix;
                            }

                            // Insert the contents of the dialog field into the database.
                            values.put(LinkContract.LinkEntry.COLUMN_LINK_DATA_TYPE, num); // TO DO. Used when folders are added.
                            values.put(LinkContract.LinkEntry.COLUMN_LINK_TITLE, ((EditText) alertDialog.findViewById(R.id.edit_web_title)).getText().toString());
                            values.put(LinkContract.LinkEntry.COLUMN_LINK_URL, urlFix);
                            values.put(LinkContract.LinkEntry.COLUMN_LINK_IMAGE, getArguments().getByteArray("getByteArray"));

                            try {
                                getActivity().getContentResolver().insert(LinkContract.LinkEntry.CONTENT_URI, values);
                            }
                            catch(Exception e) {
                                Toast.makeText(view.getContext(),
                                        e.getMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }
                            // Empty the dialog fields.
                            dialogThumbnail.setImageBitmap(null);
                            dialogTitle.setText("");
                            dialogUrl.setText("");
                            sharedUrl.sharedText(false);

                        } else { // If the URL field is empty, ask the user to input a URL.
                            Toast.makeText(view.getContext(),
                                    "Please enter a URL.", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });

                // Clear out the dialog fields if the user decides to cancel the dialog.
                builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogThumbnail.setImageBitmap(null);
                        dialogTitle.setText("");
                        dialogUrl.setText("");
                        sharedUrl.sharedText(false);
                    }
                });
                break;
            /**
             * Match value is {@link LinkFragment.QUERY_UPDATE_DIALOG}. Shows a dialog that will have its fields
             * populated with the database entries of the item clicked.
             */
            case LinkFragment.QUERY_UPDATE_DIALOG:
                builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String emptyUrl = ((EditText) alertDialog.findViewById(R.id.edit_web_url)).getText().toString();
                        // If the user deletes the URL, then the dialog will not update.
                        if (!emptyUrl.equals("")) {
                            ContentValues values = new ContentValues();
                            values.put(LinkContract.LinkEntry.COLUMN_LINK_TITLE, ((EditText) alertDialog.findViewById(R.id.edit_web_title)).getText().toString());
                            values.put(LinkContract.LinkEntry.COLUMN_LINK_URL, ((EditText) alertDialog.findViewById(R.id.edit_web_url)).getText().toString());

                            try {
                                getActivity().getContentResolver().update(ContentUris.withAppendedId(LinkContract.LinkEntry.CONTENT_URI, getArguments().getLong("getID")), values, null, null);
                            }
                            catch(Exception e) {
                                Toast.makeText(view.getContext(),
                                        e.getMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }
                            // Clears the dialog fields.
                            dialogThumbnail.setImageBitmap(null);
                            dialogTitle.setText("");
                            dialogUrl.setText("");
                            sharedUrl.sharedText(false);
                        }
                    }
                });
                // Deletes the item from the database.
                builder.setNeutralButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            getActivity().getContentResolver().delete(ContentUris.withAppendedId(LinkContract.LinkEntry.CONTENT_URI, getArguments().getLong("getID")), null, null);
                        }
                        catch(Exception e) {
                            Toast.makeText(view.getContext(),
                                    e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogThumbnail.setImageBitmap(null);
                        dialogTitle.setText("");
                        dialogUrl.setText("");
                        sharedUrl.sharedText(false);
                    }
                });
                break;
            default:
                throw new IllegalArgumentException("Unable to show dialog"); // Will crash the application intentionally.
        }
        builder.setTitle(R.string.dialog_title);
        builder.setCancelable(true);
        builder.create();

        //Builds the dialog.
        alertDialog = builder.show();

        //Sets the window size constraint of the dialog.
        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogThumbnail = (ImageView) alertDialog.findViewById(R.id.edit_web_thumbnail);
        dialogTitle = (EditText) alertDialog.findViewById(R.id.edit_web_title);
        dialogUrl = (EditText) alertDialog.findViewById(R.id.edit_web_url);

        //Populates the dialog fields.
        fillDialog();

        return alertDialog;
    }

    /**
     * If the key:getDisplayInfo is equal to LinkFragment.DISPLAY_INSERT_INFO. The dialog's fields will
     * be populated with data from the URL that has been parsed.
     * If the key:getDisplayInfo is equal to LinkFragment.DISPLAY_QUERY_UPDATE_INFO. The dialog's fields will
     * be populated with data from the database.
     * TO DO: Make it into methods.
     */
    public void fillDialog() {
        if (getArguments().getInt("getDisplayInfo") == LinkFragment.DISPLAY_INSERT_INFO) {
            if( this.getArguments().containsKey("getByteArray")) {
                dialogThumbnail.setImageBitmap(LinkInfo.convertToBitmap(this.getArguments().getByteArray("getByteArray")));
            }
            dialogTitle.setText(this.getArguments().getString("getTitle"));
            dialogUrl.setText(this.getArguments().getString("getUrl"));

        } else if(getArguments().getInt("getDisplayInfo") == LinkFragment.DISPLAY_QUERY_UPDATE_INFO) {

            String[] projection = {
                    LinkContract.LinkEntry.COLUMN_LINK_URL,
                    LinkContract.LinkEntry.COLUMN_LINK_TITLE,
                    LinkContract.LinkEntry.COLUMN_LINK_IMAGE,
            };
            Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(LinkContract.LinkEntry.CONTENT_URI, getArguments().getLong("getID")), projection, null, null, null);

            if (cursor.moveToFirst()) {
                int columnImage = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_IMAGE);
                int columnTitle = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_TITLE);
                int columnUrl = cursor.getColumnIndex(LinkContract.LinkEntry.COLUMN_LINK_URL);

                byte[] byteImage = cursor.getBlob(columnImage);
                String urlTitle = cursor.getString(columnTitle);
                String actualUrl = cursor.getString(columnUrl);

                if(byteImage != null) {
                    dialogThumbnail.setImageBitmap(LinkInfo.convertToBitmap(byteImage));
                }
                cursor.close();
                dialogTitle.setText(urlTitle);
                dialogUrl.setText(actualUrl);
            }
        }
    }

    /**
     * Verifies whether {@link com.example.android.linker.MainActivity} has implemented {@link LinkFragment.SharedText} interface.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sharedUrl = (LinkFragment.SharedText) context;
        }
        catch ( ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interface DataBundle.");
        }
    }
}
