package com.sachinshinde.wordlearner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
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

}
