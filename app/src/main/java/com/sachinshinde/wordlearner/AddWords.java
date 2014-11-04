package com.sachinshinde.wordlearner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.internal.widget.ProgressBarCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


public class AddWords extends Activity {

    EditText et;
    Button bAdd;
    ListView lvWords;
    ArrayList<String> words;
    ListAdapter mAdapter;
    public static final String API_KEY = "YOUR_API_KEY"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words);
        et = (EditText) findViewById(R.id.etAddWords);
        bAdd = (Button) findViewById(R.id.bAddWords);
        lvWords = (ListView) findViewById(R.id.lvWords);


        words = Utils.loadListFromFile(Utils.WordsFile);
        sessionList = Utils.loadListFromFile(Utils.SessionFile);
//        SerialPreference.retPrefs(getBaseContext());
        if (words == null) {
            words = new ArrayList<String>();
        }

        Collections.sort(words);


        mAdapter = new ListAdapter(getBaseContext(), words);
        lvWords.setAdapter(mAdapter);

        ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + words.size() + " words");

        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et.getText().toString() != null && !et.getText().toString().isEmpty()) {
                    words.add(et.getText().toString().trim());
                    Collections.sort(words);
                    ((ListAdapter)lvWords.getAdapter()).addToList(et.getText().toString().trim());
                    ((ListAdapter) lvWords.getAdapter()).notifyDataSetChanged();
//                    SerialPreference.savePrefs(getBaseContext(), words);
                    Utils.writeListToFile(mAdapter.getList(),Utils.WordsFile);
                    addToSessionList(et.getText().toString().trim());
                    et.setText("");
                    ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
                }
            }
        });

        lvWords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            AlertDialog dialog;
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddWords.this);
                builder.setTitle(mAdapter.getItem(i));
                ProgressBar pb = new ProgressBar(AddWords.this);
                pb.setIndeterminate(true);

                builder.setView(pb);
                builder.setPositiveButton("Got It!", null);
                new AsyncTask<String, Void, String>(){

                    @Override
                    protected String doInBackground(String... strings) {
                        if(Utils.hasWord(strings[0])){
                            Log.d("WordLearner", "Loading from memory");
                            return Utils.getDefinition(strings[0]);
                        }
                        Log.d("WordLearner", "Loading from wordnik");
                        return Utils.saveWord(strings[0], Utils.jsonToString(NetworkUtils.GET(Utils.URL1 + strings[0].trim().toLowerCase(Locale.US) + Utils.URL2)));
                    }

                    @Override
                    protected void onPostExecute(String s) {

                        builder.setView(null);
                        builder.setMessage(s);
                        builder.setNeutralButton("Speak", null);
                        final AlertDialog mDialog = builder.create();
                        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {



                            @Override
                            public void onShow(DialogInterface dialog) {

                                final Button b = mDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

                                final Handler handler = new Handler();
                                final Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.removeCallbacks(this);
                                        if(done){
                                            b.setText("Speak");
                                            return;
                                        }
                                        handler.postDelayed(this, 250);
                                        if(!b.getText().toString().contains(".")){
                                            b.setText(".");
                                        } else {
                                            if(b.getText().toString().length() == 3){
                                                b.setText(".");
                                            } else {
                                                b.setText(b.getText().toString() + ".");
                                            }
                                        }
                                    }
                                };

                                b.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {

                                        done = false;

                                        handler.post(r);

                                        new AsyncTask<String, Void, String>() {
                                            @Override
                                            protected void onPreExecute() {
                                                super.onPreExecute();
                                            }

                                            @Override
                                            protected String doInBackground(String... strings) {
                                                String jsonString = NetworkUtils.GET("http://api.wordnik.com:80/v4/word.json/" + strings[0].trim().toLowerCase(Locale.getDefault()) + "/audio?useCanonical=false&limit=50&api_key=" + API_KEY);
                                                try {
                                                    JSONArray jsonArray = new JSONArray(jsonString);
                                                    if (jsonArray.length() == 1) {
                                                        return jsonArray.getJSONObject(0).getString("fileUrl");
                                                    } else if(jsonArray.length() > 1){
                                                        return jsonArray.getJSONObject(1).getString("fileUrl");
                                                    } else {
                                                        return null;
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(String s) {
                                                super.onPostExecute(s);

                                                if (s == null) {
                                                    done = true;
                                                    Toast.makeText(getBaseContext(), "No audio found", Toast.LENGTH_LONG).show();
                                                } else {
                                                    final MediaPlayer mp = new MediaPlayer();
                                                    try {
                                                        mp.setDataSource(s);
                                                        mp.prepareAsync();
                                                        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                            @Override
                                                            public void onPrepared(MediaPlayer mp) {
                                                                done = true;
                                                                mp.start();
                                                            }
                                                        });
                                                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                            @Override
                                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                                if (mp != null)
                                                                    mp.release();
                                                            }
                                                        });

                                                    } catch (IOException e) {
                                                    }
                                                }


                                            }
                                        }.execute(mAdapter.getItem(i));

                                    }
                                });
                            }
                        });
                        mDialog.show();
                        dialog.cancel();
                        super.onPostExecute(s);
                    }
                }.execute(mAdapter.getItem(i));
                dialog = builder.create();
                dialog.show();
            }
        });



        lvWords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddWords.this);
//                builder.setTitle("Edit");
                final EditText edit = new EditText(AddWords.this);
                edit.setText(mAdapter.getItem(pos));
                builder.setView(edit);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAdapter.mList.remove(mAdapter.getItem(pos));
                        words.remove(mAdapter.getItem(pos));
                        ((ListAdapter)lvWords.getAdapter()).addToList(edit.getText().toString());
                        words.add(edit.getText().toString());
                        Collections.sort(words);
                        mAdapter.notifyDataSetChanged();
                        Utils.writeListToFile(mAdapter.getList(),Utils.WordsFile);
                        ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                    }
                });

                builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAdapter.mList.remove(mAdapter.getItem(pos));
                        words.remove(mAdapter.getItem(pos));
                        mAdapter.notifyDataSetChanged();
                        Utils.writeListToFile(mAdapter.getList(),Utils.WordsFile);
                        ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                    }
                });

                builder.show();

                return true;
            }
        });

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mAdapter.filter(et.getText().toString().toLowerCase(Locale.getDefault()));
            }
        });



    }

    boolean done = false;

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
        if(mFile.exists())
            Utils.writeListToFile(sessionList, Utils.SessionFile);
        super.onPause();
    }

    public void addToSessionList(String wordtoadd){
        File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
        if(mFile.exists()){
            sessionList.add(wordtoadd);
        }
    }

    ArrayList<String> sessionList = new ArrayList<String>();
}
