package com.sachinshinde.wordlearner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.word_learner);
        ab.setTitle("");
//        ab.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        findViewById(R.id.buttonAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyActivity.this, AddWords.class));
            }
        });

        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
                builder.setTitle("Select Session");
                builder.setAdapter(new ArrayAdapter<String>(MyActivity.this, android.R.layout.select_dialog_item, new String[] {"New session", "Resume old session"}), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            startActivity(new Intent(MyActivity.this, TestWords.class).putExtra("file", Utils.WordsFile));
                        } else {
                            startActivity(new Intent(MyActivity.this, TestWords.class).putExtra("file", Utils.SessionFile));
                        }
                    }
                });
                File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
                if(mFile.exists())
                    builder.show();
                else
                    startActivity(new Intent(MyActivity.this, TestWords.class).putExtra("file", Utils.WordsFile));
            }
        });

        findViewById(R.id.buttonExport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExportWords().execute();
            }
        });

        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner");
        File mFile = new File(mDir.getPath() + "/" + Utils.WordsFile);
        if(!mFile.exists()){
            ArrayList<String> list = SerialPreference.retPrefs(getBaseContext());
            if(list != null){
                Utils.writeListToFile(list, Utils.WordsFile);
            }
        }

        findViewById(R.id.buttonImport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("Word Learner", "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = getPath(this, uri);
                        Toast.makeText(getBaseContext(), path, Toast.LENGTH_LONG).show();
                        ArrayList<String> newWordList = parseFile(path);
                        ArrayList<String> oldList = Utils.loadListFromFile(Utils.WordsFile);
                        if(oldList != null)
                            newWordList.addAll(oldList);


                        // add elements to al, including duplicates
                        Set<String> hs = new HashSet<>();
                        hs.addAll(newWordList);
                        newWordList.clear();
                        newWordList.addAll(hs);

                        Utils.writeListToFile(newWordList, Utils.WordsFile);

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.d("Word Learner", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public ArrayList<String> parseFile(String path){
        ArrayList<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null)
            {
                list.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public class ExportWords extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getBaseContext(), "Export successful", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            ArrayList<String> word_list = Utils.loadListFromFile(Utils.WordsFile);
            File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner");
            File mFile = new File(mDir.getPath() + "/Exported List.txt");
            try {
                // Assume default encoding.
                FileWriter fileWriter =
                        new FileWriter(mFile);

                // Always wrap FileWriter in BufferedWriter.
                BufferedWriter bufferedWriter =
                        new BufferedWriter(fileWriter);

                // Note that write() does not automatically
                // append a newline character.
                for(String s : word_list){
                    bufferedWriter.write(s);
                    bufferedWriter.write(": ");
                    if (Utils.hasWord(s)) {
                        Log.d("WordLearner", "Loading from memory");
                        bufferedWriter.write(Utils.getDefinition(s));
                    } else {
                        Log.d("WordLearner", "Loading from wordnik");
                        bufferedWriter.write(Utils.saveWord(s, Utils.jsonToString(NetworkUtils.GET(Utils.URL1 + s.trim().toLowerCase(Locale.US) + Utils.URL2))));
                    }
                    bufferedWriter.newLine();
                }
                Log.d("WordLearner", "Export completed");
                // Always close files.
                bufferedWriter.close();
            }
            catch(IOException ex) {
                System.out.println(
                        "Error writing to file '"
                                + "Exported List.txt" + "'");
                // Or we could just do this:
                // ex.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
