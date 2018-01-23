package com.example.android.linker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements LinkFragment.ShowDialog, LinkFragment.ShowFolderFragment, LinkFragment.SharedText {

    /** Tag identifies the originating class of the log output */
    public final static String LOG_TAG = MainActivity.class.getSimpleName();

    /** Variable will hold the URL that is shared to the Linker app. */
    private String sharedUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);

        /** Creates the fragment. */
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_fragment_layout, LinkFragment.newInstance(), "frag")
                    .commit();
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        /** Receives the URL when a browser shares it to this app. */
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
    }

    /** Interface to show a dialog. */
    @Override
    public void showDialog(Bundle bundle) {
        LinkInfoDialogFragment.newInstance(bundle).show(getSupportFragmentManager(), "editDialog");
    }

    /** Interface to create a fragment that will serve as folder to organize links. */
    @Override
    public void showFolderFragment(long id) {
        // TO DO

    }

    /** Designed to either return the URL or dereference the variable. */
    @Override
    public String sharedText(boolean state) {
        if(state) { return sharedUrl; }
        else if (!state) { sharedUrl = null; }
        return sharedUrl;
    }
}
