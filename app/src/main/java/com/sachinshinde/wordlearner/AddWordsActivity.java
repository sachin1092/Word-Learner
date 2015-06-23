package com.sachinshinde.wordlearner;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


public class AddWordsActivity extends AppCompatActivity {

    public static final String API_KEY = Constants.API_KEY;
    EditText et;
    View bAdd;
    ListView lvWords;
    ArrayList<String> words;
    ListAdapter mAdapter;
    TextToSpeech ttobj;
    AlertDialog dialog;
    boolean done = false;
    ArrayList<String> sessionList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ab.setHomeAsUpIndicator(R.drawable.word_learner);
        ab.setTitle("Words List");
//        ab.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        et = (EditText) findViewById(R.id.etAddWords);
        bAdd = findViewById(R.id.bAddWords);
        lvWords = (ListView) findViewById(R.id.lvWords);


        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        ttobj.setLanguage(Locale.US);

        ttobj.setSpeechRate(0.8f);

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
                    ((ListAdapter) lvWords.getAdapter()).addToList(et.getText().toString().trim());
                    ((ListAdapter) lvWords.getAdapter()).notifyDataSetChanged();
//                    SerialPreference.savePrefs(getBaseContext(), words);
                    Utils.writeListToFile(mAdapter.getList(), Utils.WordsFile);
                    addToSessionList(et.getText().toString().trim());
                    try {
                        performItemClick(mAdapter.getPosition(et.getText().toString()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e("Error Addwords 114", ex.getMessage());
                    }
                    et.setText("");
                    ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
                }
            }
        });

        lvWords.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                try {
                    performItemClick(i);
                } catch (Exception ex) {
                }
            }
        });


        lvWords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {

                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddWordsActivity.this);

                builder.setTitle("Edit Word");

                View mView = LayoutInflater.from(AddWordsActivity.this).inflate(R.layout.edit_word, null);

                final EditText edit = (EditText) mView.findViewById(R.id.etEditWord);
                edit.setText(mAdapter.getItem(pos));

                builder.setView(mView);

                mView.findViewById(R.id.fabDone).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAdapter.mList.remove(mAdapter.getItem(pos));
                        words.remove(mAdapter.getItem(pos));
                        ((ListAdapter) lvWords.getAdapter()).addToList(edit.getText().toString());
                        words.add(edit.getText().toString());
                        Collections.sort(words);
                        mAdapter.notifyDataSetChanged();
                        Utils.writeListToFile(mAdapter.getList(), Utils.WordsFile);
                        ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        mDialog.cancel();
                    }
                });

                mView.findViewById(R.id.fabRemove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.deleteWord(mAdapter.getItem(pos));
                        mAdapter.mList.remove(mAdapter.getItem(pos));
                        words.remove(mAdapter.getItem(pos));
                        mAdapter.notifyDataSetChanged();
                        Utils.writeListToFile(mAdapter.getList(), Utils.WordsFile);
                                ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        mDialog.cancel();
                    }
                });


                mDialog = builder.create();
                mDialog.show();

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
                if(et.getText().length() > 0){
                    findViewById(R.id.ivAddWords).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.ivAddWords).setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.ivAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et.setText("");
            }
        });


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    android.support.v7.app.AlertDialog mDialog;

    public void performItemClick(final int position) {

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddWordsActivity.this);
//        builder.setTitle(mAdapter.getItem(position));
//        ProgressBar pb = new ProgressBar(AddWords.this);
//        pb.setIndeterminate(true);

//        View mView = LayoutInflater.from(AddWords.this).inflate(R.layout.meaning_dialog, null);
//        builder.setView(mView);

        builder.setView(Utils.getProgressView(AddWordsActivity.this));
//        builder.setPositiveButton("Got It!", null);
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... strings) {
                if (Utils.hasWord(strings[0])) {
                    Log.d("WordLearner", "Loading from memory");
                    return Utils.getWordJSON(strings[0]);
                }
                Log.d("WordLearner", "Loading from my dictionary");
                return Utils.saveWord(strings[0], NetworkUtils.GET(Utils.FINAL_URL_SINGLE + strings[0].trim().toLowerCase(Locale.US)));
            }

            @Override
            protected void onPostExecute(String s) {

                builder.setView(null);

                View mView = Utils.getMeaningsView(s, AddWordsActivity.this);
                if (mView != null) {

                    View buttonBar = LayoutInflater.from(AddWordsActivity.this).inflate(R.layout.button_bar, null);
                    FrameLayout fl = (FrameLayout) buttonBar.findViewById(R.id.flButtonBarContainer);
                    fl.addView(mView);

                    builder.setView(buttonBar);
                }
                else {
                    Toast.makeText(AddWordsActivity.this, "Oops! An error occurred. Try Again.", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    Utils.deleteWord(mAdapter.getItem(position));
                    return;
                }


//                builder.setMessage(s);
//                builder.setNeutralButton("Speak", null);
                final AlertDialog mDialog = builder.create();
//
//                mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//
//                        final Button b = mDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//
//                        b.setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(View view) {
//
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//                                    ttobj.speak(mAdapter.getItem(position), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
//                                } else {
//                                    ttobj.speak(mAdapter.getItem(position), TextToSpeech.QUEUE_FLUSH, null);
//                                }
//
//                            }
//                        });
//                    }
//                });
                mDialog.show();
                dialog.cancel();
                super.onPostExecute(s);
            }
        }.execute(mAdapter.getItem(position));
        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
        if (mFile.exists())
            Utils.writeListToFile(sessionList, Utils.SessionFile);
        super.onPause();
    }

    public void addToSessionList(String wordtoadd) {
        File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
        if (mFile.exists()) {
            sessionList.add(wordtoadd);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_words, menu);
        return true;
    }
}
