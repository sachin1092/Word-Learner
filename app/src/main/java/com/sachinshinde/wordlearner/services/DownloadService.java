package com.sachinshinde.wordlearner.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.sachinshinde.wordlearner.MainActivity;
import com.sachinshinde.wordlearner.NetworkUtils;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadService extends IntentService {

    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.sachinshinde.wordlearner.receiver";
    public static final String TOTAL = "total";
    public static final String PROGRESS = "progress";
    int total = 100;
    private String TAG = "WORD_LEARNER";

    public DownloadService() {
        super("DownloadService");
    }

    // will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Downloading definitions")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.word_learner_notification)
                .setOngoing(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.word_learner_notification2))
                .setContentIntent(PendingIntent.getActivity(getBaseContext(),
                        1092,
                        new Intent(DownloadService.this,
                                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));


        final ArrayList<String> newWordList = parseFile(intent.getStringExtra(FILEPATH));

        total = newWordList.size();

        Log.d(TAG, "onHandleIntent " + total);

        publishProgress(0, total);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                importWords(newWordList, 0);
//            }
//        }).start();



    }

    int id = 10921;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;

    public void publishProgress(int progress, int total) {
        mBuilder.setProgress(total, progress, false);
        mNotifyManager.notify(id, mBuilder.build());
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(TOTAL, total);
        intent.putExtra(PROGRESS, progress);
        intent.putExtra(RESULT, RESULT_CANCELED);
        sendBroadcast(intent);
    }

    public void onDownloadComplete() {
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Downloading definitions")
                .setContentText("Download complete")
                .setSmallIcon(R.drawable.word_learner_notification)
                .setOngoing(false)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.word_learner_notification2))
                .setContentIntent(PendingIntent.getActivity(getBaseContext(),
                        1092,
                        new Intent(DownloadService.this,
                                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        mNotifyManager.notify(id, mBuilder.build());
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(TOTAL, total);
        intent.putExtra(PROGRESS, total);
        intent.putExtra(RESULT, RESULT_OK);
        sendBroadcast(intent);
        Toast.makeText(DownloadService.this, "Import Successful", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<String> parseFile(String path) {
        String pattern = "\\.txt$";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(path);
        if (!m.find()) {
            Toast.makeText(getBaseContext(), "Only *.txt files are allowed", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }
        ArrayList<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(" ")) {
                    String[] lines = line.split(" ");
                    for (String line1 : lines)
                        if (checkWord(line)) list.add(line1.toLowerCase(Locale.US));
                } else {
                    if (checkWord(line))
                        list.add(line.toLowerCase(Locale.US));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean checkWord(String str) {

        String pattern = "[a-zA-Z]*";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(str);
        return m.find() && !str.isEmpty() && !Utils.hasWord(str);
    }


    public void importWords(ArrayList<String> list, int processedWords) {

        Log.d(TAG, list.size() + ", " + processedWords);

        boolean completed = false;

        ArrayList<String> response = new ArrayList<>();

        try {
            if (list.size() > 0) {
                ArrayList<String> to_downloadList = new ArrayList<>();
                to_downloadList.addAll(list.subList(0, list.size() > 50 ? 49 : list.size() - 1));
                list.removeAll(to_downloadList);

                if (to_downloadList.size() == 0) {
                    completed = true;
                    onDownloadComplete();
                    return;
                }

                JSONArray jsonArray = new JSONArray();
                for (String word : to_downloadList) {
                    jsonArray.put(word);
                }


                JSONObject jsonObject = new JSONObject(NetworkUtils.POST(jsonArray.toString(), getBaseContext(), 0));


                Iterator<String> itr = jsonObject.keys();
                while (itr.hasNext()) {
                    try {
                        String word = itr.next().toLowerCase(Locale.US).trim();
                        Utils.saveWord(word, jsonObject.getJSONObject(word).toString());
                        response.add(word);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }


            } else {
                completed = true;
                onDownloadComplete();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!completed) {
            processedWords += response.size();

            ArrayList<String> oldList = Utils.loadListFromFile(Utils.WordsFile);
            if (oldList != null)
                response.addAll(oldList);

            Utils.writeListToFile(response, Utils.WordsFile);

            importWords(list, processedWords);
            publishProgress(processedWords, total);
        }

    }
} 