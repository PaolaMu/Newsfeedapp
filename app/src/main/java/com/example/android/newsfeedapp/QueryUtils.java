package com.example.android.newsfeedapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper methods related to requesting and receiving news data from the guardian.
 */
public final class QueryUtils {

    private static final String TAG = QueryUtils.class.getSimpleName();


    private QueryUtils() {
    }

    public static List<News> fetchNews(String requestedUrl) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Log.e(TAG, "fetchNews: Problem making HTTP request", ie);
        }

        //Create URL
        URL newsUrl = createUrl(requestedUrl);

        //Perform httpRequest
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(newsUrl);
        } catch (IOException ioe) {
            Log.e(TAG, "FetchNews: Problem making HTTP request", ioe);
        }
        //Extract relevant data
        List<News> myNews = extractNewsFromJson(jsonResponse);

        return myNews;
    }

    private static String makeHttpRequest(URL newsUrl) throws IOException {

        String jsonResponse = "";
        //Check for null
        if (newsUrl == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        //Create the connection
        try {
            urlConnection = (HttpURLConnection) newsUrl.openConnection();
            urlConnection.setReadTimeout(80000);
            urlConnection.setConnectTimeout(16000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "makeHttpRequest: Error Code" + urlConnection.getResponseCode());
            }
        } catch (IOException ioe) {
            Log.e(TAG, "makeHttpRequest: Couldn't retrieve JSON", ioe);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static URL createUrl(String requestedUrl) {
        URL url = null;
        try {
            url = new URL(requestedUrl);
        } catch (MalformedURLException mue) {
            Log.e(TAG, "createUrl; Problem building URL", mue);
        }
        return url;
    }

    private static List<News> extractNewsFromJson(String jsonResponse) {
        String title;
        String author;
        String date;
        String urlSource;
        String thumbnail;

        //Check for JSON is null
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        List<News> myNews = new ArrayList<>();
        try {
            JSONObject baseJSONResponse = new JSONObject(jsonResponse);
            JSONObject baseJSONResponseResult = baseJSONResponse.getJSONObject("response");
            JSONArray currentNewsArticles = baseJSONResponseResult.getJSONArray("results");
            //make items
            for (int n = 0; n < currentNewsArticles.length(); n++) {
                JSONObject currentArticle = currentNewsArticles.getJSONObject(n);

                // For a given article, extract the JSONObject associated with the
                // key called "fields", which represents a list of all fields
                // for that article (i.e. thumbnail url, headline, etc.)
                JSONObject fields = currentArticle.getJSONObject("fields");

                title = currentArticle.getString("webTitle");
                String section = currentArticle.getString("sectionName");
                urlSource = currentArticle.getString("webUrl");
                date = currentArticle.getString("webPublicationDate");
                author = fields.getString("byline");
                thumbnail = fields.optString("thumbnail");


                News article = new News(title, section, author, date, urlSource, dlBitmap(thumbnail));
                myNews.add(article);

            }


        } catch (JSONException je) {
            Log.e(TAG, "extratNewsFromJson: Problem parsing results", je);

        }
        return myNews;
    }

    /**
     * Return thumbnail image from URL Use higher res if possible
     * and return {@link Bitmap} Credit to Mohammad Ali Fouani https://stackoverflow.com/q/51587354/9302422
     *
     * @param initialUrl is where the thumbnail is located via the API
     * @return Bitmap
     */

    private static Bitmap dlBitmap(String initialUrl) {
        Bitmap bitmap = null;
        if (!"".equals(initialUrl)) {
            String newUrl = initialUrl.replace
                    (initialUrl.substring(initialUrl.lastIndexOf("/")), "/1000.jpg");

            try {
                // Check for better image quality
                InputStream inputStream = new URL(newUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                try {
                    InputStream inputStream = new URL(initialUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception ignored) {

                }
            }
        }
        return bitmap;
    }


    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);
            String line = bufferReader.readLine();
            while (line != null) {
                output.append(line);
                line = bufferReader.readLine();
            }
        }
        return output.toString();
    }

}

