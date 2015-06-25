package com.sachinshinde.wordlearner.services;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.sachinshinde.wordlearner.activities.MainActivity;
import com.sachinshinde.wordlearner.utils.NetworkUtils;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadService extends Service {

    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.sachinshinde.wordlearner.receiver";
    public static final String TOTAL = "total";
    public static final String PROGRESS = "progress";
    int total = 100;
    private String TAG = "WORD_LEARNER";

//    public DownloadService() {
//        super("DownloadService");
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        onHandleIntent(intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    // will be called asynchronously by Android
//    @Override
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
//                importWords(newWordList, 0);
//            }
//        }).start();

        new ImportWords(newWordList, 0).execute();



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

        Intent intent2 = new Intent(Utils.INTENT_REFRESH);
        sendBroadcast(intent2);
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

        Intent intent2 = new Intent(Utils.INTENT_REFRESH);
        sendBroadcast(intent2);


        Toast.makeText(DownloadService.this, "Import Successful", Toast.LENGTH_SHORT).show();
        stopSelf();
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


    public class ImportWords extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

        public ArrayList<String> list;
        private boolean completed = false;
        int processedWords;

        public ImportWords(ArrayList<String> list, int processedWords) {
            this.list = list;
            this.processedWords = processedWords;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... arrayLists) {
            try {
                if (this.list.size() > 0) {
                    ArrayList<String> to_downloadList = new ArrayList<>();
                    to_downloadList.addAll(this.list.subList(0, this.list.size() > 50 ? 49 : this.list.size() - 1));
                    this.list.removeAll(to_downloadList);

                    if (to_downloadList.size() == 0) {
                        completed = true;
                        return null;
                    }

                    JSONArray jsonArray = new JSONArray();
                    for (String word : to_downloadList) {
                        jsonArray.put(word);
                    }


                    JSONObject jsonObject = new JSONObject(NetworkUtils.POST(jsonArray.toString(), getBaseContext(), 0));

                    ArrayList<String> response = new ArrayList<>();

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

                    return response;


                } else {
                    completed = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> response) {
            super.onPostExecute(response);
            if (!completed) {
                if (response != null) {
                    processedWords += response.size();

                    ArrayList<String> oldList = Utils.loadListFromFile(Utils.WordsFile);
                    if (oldList != null)
                        response.addAll(oldList);

                    Utils.writeListToFile(response, Utils.WordsFile);

//                    importWords(list, processedWords);
                    DownloadService.this.publishProgress(processedWords, total);
                }
                new ImportWords(this.list, processedWords).execute();

            } else {
                onDownloadComplete();
            }
        }
    }


} 