package com.sachinshinde.wordlearner.utils;

import android.content.Context;
import android.preference.PreferenceManager;
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

    public static String[] POST_URLS = {
            "http://my-dictionary-api-1.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-2.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-3.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-4.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-5.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-6.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-7.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-8.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-9.appspot.com/getMultiMeaning",
            "http://my-dictionary-api-10-986.appspot.com/getMultiMeaning"};

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
    public static String POST(String data, Context mContext, int total) {
        int index = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("URL_INDEX", 0);
        String urlStr = POST_URLS[index];
        InputStream inputStream = null;
        Log.d("WordLearner", "Requesting: " + urlStr);
        Log.d("WordLearner", "Request data: " + data);

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

            try {

                inputStream = urlConnection.getInputStream();
            }catch (Exception ex){
                total++;
                if(total == POST_URLS.length)
                    return "Did not work!";
                index = (index + 1) % POST_URLS.length;
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("URL_INDEX", index).commit();
                return POST(data, mContext, total);
            }

            int status = urlConnection.getResponseCode()/100;

            Log.d("Word Learner", "Status " + urlConnection.getResponseCode());

            if(status == 5){
                total++;
                if(total == POST_URLS.length)
                    return "Did not work!";
                index = (index + 1) % POST_URLS.length;
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("URL_INDEX", index).commit();
                return POST(data, mContext, total);
            }

            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            e.printStackTrace();
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
