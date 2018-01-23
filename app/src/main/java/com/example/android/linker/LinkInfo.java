package com.example.android.linker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;

/**
 * HTML Parse a URL to extract the URL title and thumbnail.
 */
public class LinkInfo {

    /** Tag identifies the originating class of the log output. */
    private static final String LOG_TAG = LinkInfo.class.getSimpleName();

    /** Holds the URL to be parsed. */
    private String url;

    /** Holds the URL title. */
    private String title;

    /** Holds the constructor argument. */
    private String passedData;

    /** Holds the byte array to be converted to bitmap. */
    private byte[] blob;

    /** Holds the converted bitmap. */
    private Bitmap myBitmap;

    /**
     * Text is first parsed for a URL, then parsed for the URL title and thumbnail, then converted to a byte array.
     * @param data
     */
    public LinkInfo(String data){
        passedData = data;
        convertToByte(linkParser(textParser(passedData)));
    }

    /**
     * Tokenize the string and extract the token that is a URL.
     * @param data The constructor argument.
     * @return String - A url if pattern matching is successful, or the original argument if it fails.
     */
    private String textParser(String data) {
        url = data;
        StringTokenizer string = new StringTokenizer(data, " \"\n"); // Tokenize the String in an attempt to remove quotation marks.

        while(string.hasMoreTokens()) {
            String str = string.nextToken();
            Pattern pattern = Pattern.compile("www.*|http.*");
            Matcher match = pattern.matcher(str); // Makes sure the URL starts with either www or http.

            if (match.matches()) {
                url = str;
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
        }
        return url;
    }

    /**
     * Extracts the title and thumbnail of the matched URL. Otherwise, nothing happens.
     * @param url Either the matched URl or the original constructor argument.
     * @return Bitmap - Returns the parsed URL thumbnail and initialized the URL title.
     */
    private Bitmap linkParser(String url) {
        String imageUrl = "";

        /**
         * If the URL equals passedData, than that means pattern matching failed in {@link LinkInfo#textParser(String)} and the constructor argument, data, holds no URL.
         * There is no need for HTML parsing at this point.
         */
        if (url.equals(passedData)) {
            try {
                Document doc = Jsoup.connect(url).timeout(3000).get();
                title = doc.title();

                Element image = doc.select("meta[property=og:image]").first(); //Uses Open Graph Protocol.
                if (image != null) {
                    imageUrl = image.attr("content");
                    if (imageUrl.equals("")) {
                        image = doc.select("img[src~=.*(png|jpe?g).*]").first(); //Searches in the img element for any text that ends in png, jpg, or jpeg.
                        imageUrl = image.attr("abs:src");
                        imageUrl = imageUrl.replace("&amp;", "&");
                    }
                }
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "https://" + imageUrl;
                }

                if (!imageUrl.equals("")) {
                    URL sUrl = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) sUrl.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    myBitmap = BitmapFactory.decodeStream(input);
                }
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
        return myBitmap;
    }

    /**
     * Takes bitmap and converts it to a byte array in order to be inserted into the database.
     * Temporary measure. Will replace method with one that saves the bitmap to disk and passes filepath to the database.
     * @param myBitmap
     */
    private void convertToByte(Bitmap myBitmap) {
        if (myBitmap!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            blob = stream.toByteArray();
        }
    }

    /**
     * Converts a byte array back to bitmap.
     * @param array
     * @return bitmap
     */
    public static Bitmap convertToBitmap(byte[] array) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeByteArray(array, 0, array.length, options);
    }

    /** Returns the matched URL or original constructor argument. */
    public String getUrl() {
        return url;
    }

    /** Returns the bitmap converted to byte array. */
    public byte[] getImageBlob() {
        return blob;
    }

    /** Returns the URL title. */
    public String getTitle() {
        return title;
    }

    /** Returns the bitmap. */
    public Bitmap getBitmap() {
        return myBitmap;
    }
}




