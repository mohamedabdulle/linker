package com.example.android.linker;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * LinkInfo's network connection needs to be done in a background thread so we extend the {@link AsyncTaskLoader}.
 */
public class LinkInfoTask extends AsyncTaskLoader<LinkInfo> {

    /** Tag identifies the originating class of the log output. */
    public static final String LOG_TAG = LinkInfoTask.class.getSimpleName();

    /** Will hold the string received through the {@link LinkFragment#sharedUrl} interface. */
    private String text;

    public LinkInfoTask(Context context, String data) {
        super(context);
        text = data;
    }

    /** {@link com.example.android.linker.LinkInfo} is performed in the background. */
    @Override
    public LinkInfo loadInBackground() {
        return new LinkInfo(text);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
