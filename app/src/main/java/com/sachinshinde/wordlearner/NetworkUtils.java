package com.sachinshinde.wordlearner;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sachin.shinde on 4/9/14.
 */
public class NetworkUtils {

    public static String GET(String urlStr) {
        InputStream inputStream = null;
        Log.d("WordLearner", "Requesting: " + urlStr);
        String result = "";
        try {

            URL url = new URL(urlStr);
            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection();

            urlConnection.setUseCaches(false);

            urlConnection.setRequestMethod("GET");

            inputStream = urlConnection.getInputStream();

            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        Log.d("JSON request", result);
        return result;
    }
    public static String POST(String urlStr, String data) {
        InputStream inputStream = null;
        Log.d("WordLearner", "Requesting: " + urlStr);
        String result = "";
        try {

            URL url = new URL(urlStr);
            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream out = urlConnection.getOutputStream();
            byte[] postData = data.getBytes();
            out.write(postData);
            out.close();

            inputStream = urlConnection.getInputStream();


            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        Log.d("JSON request", result);
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
