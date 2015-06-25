package com.sachinshinde.wordlearner.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.utils.NetworkUtils;
import com.sachinshinde.wordlearner.utils.SessionsUtil;
import com.sachinshinde.wordlearner.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;


public class TestWordsActivity extends AppCompatActivity {

    public static final String SESSION_NAME = "session_name";

    ArrayList<String> words = new ArrayList<String>();
    ArrayList<String> mastered = new ArrayList<String>();
    TextView tvWord;
    GetMeaning getMeaning;
    private TextToSpeech ttobj;
    int index = 0;
    Session currentSession;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null) {
                if (intent.getAction().equalsIgnoreCase(Utils.INTENT_ADD_WORD)) {
                    Toast.makeText(getBaseContext(), "Added " + intent.getStringExtra("WORD"), Toast.LENGTH_LONG).show();
                    addWord(intent.getStringExtra("WORD"));
                } else if (intent.getAction().equalsIgnoreCase(Utils.INTENT_DELETE_WORD)) {

                    deleteWord(intent.getStringExtra("WORD"));
                }
            }
        }
    };

    private void deleteWord(String word) {
        Utils.deleteWord(word);
        words.remove(word);
        saveSession();
        sortWords();
        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
        toSave.remove(word);
        Collections.sort(toSave);
        Utils.writeListToFile(toSave, Utils.WordsFile);
        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
    }

    private void addWord(String word) {
        words.add(word);
        saveSession();
        sortWords();
        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
        toSave.add(word);
        Collections.sort(toSave);
        Utils.writeListToFile(toSave, Utils.WordsFile);
        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
    }

    @Override
    protected void onRestart() {
        saveSession();
        tvWord.setText(words.get(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("LastIndex_" + currentSession.getSessionName(), 0)));
        super.onRestart();
    }

    @Override
    protected void onPause() {
        saveSession();
        if (words.size() != 0)
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("LastIndex_" + currentSession.getSessionName(), words.indexOf(tvWord.getText().toString())).commit();
        else
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("LastIndex_" + currentSession.getSessionName(), 0).commit();
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.INTENT_ADD_WORD);
        intentFilter.addAction(Utils.INTENT_DELETE_WORD);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_words);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            ab.setTitle("");
        } catch (Exception ex){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        String sessionName = getIntent().getStringExtra(SESSION_NAME);
        currentSession = SessionsUtil.getSession(sessionName);

        words = currentSession.getToRevise();
        mastered = currentSession.getMastered();

//                SerialPreference.retPrefs(getBaseContext());
        if (words == null || words.isEmpty()) {
            Toast.makeText(getBaseContext(), "Sorry, no words. Add words first.", Toast.LENGTH_LONG).show();
            finish();
            startActivity(new Intent(TestWordsActivity.this, SessionsListActivity.class));
            return;
        }
        sortWords();

        tvWord = (TextView) findViewById(R.id.tvWord);

        try {
            tvWord.setText(words.get(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("LastIndex_" + sessionName, 0)));
        } catch (Exception ex) {
            tvWord.setText(getNextWord(currentSession.getSortOrder()));
        }
//        ((TextView) findViewById(R.id.tvMeaning)).setText("");

        ((FrameLayout)findViewById(R.id.pbMeaning)).addView(Utils.getProgressView(TestWordsActivity.this, "Loading..."));

        findViewById(R.id.pbMeaning).setVisibility(View.GONE);

        findViewById(R.id.flWordsContainer).setVisibility(View.GONE);

        findViewById(R.id.bNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastItem = tvWord.getText().toString();
                index = (index + 1) % words.size();
                sortWords();
                tvWord.setText(getNextWord(currentSession.getSortOrder()));
//                ((TextView) findViewById(R.id.tvMeaning)).setText("");
                findViewById(R.id.flWordsContainer).setVisibility(View.GONE);
                setUndoVisibility(true);
                addAgain = false;
            }
        });

        findViewById(R.id.bYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastItem = tvWord.getText().toString();
                words.remove(tvWord.getText().toString());
                mastered.add(tvWord.getText().toString());
                saveSession();
                sortWords();
                tvWord.setText(getNextWord(currentSession.getSortOrder()));
//                ((TextView) findViewById(R.id.tvMeaning)).setText("");
                findViewById(R.id.flWordsContainer).setVisibility(View.GONE);
//                Utils.writeListToFile(words, Utils.SessionFile);
                ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
                setUndoVisibility(true);
                addAgain = true;
            }
        });


        findViewById(R.id.bMeaning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getMeaning != null)
                    if (getMeaning.getStatus() == AsyncTask.Status.PENDING || getMeaning.getStatus() == AsyncTask.Status.RUNNING) {
                        getMeaning.cancel(true);
                    }
                getMeaning = new GetMeaning();
                getMeaning.execute(tvWord.getText().toString());
            }
        });

        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");


        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        ttobj.setLanguage(Locale.US);


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void sortWords() {
        switch (currentSession.getSortOrder()){
            case Session.ASCENDING:
                Collections.sort(words);
                break;
            case Session.DECENDING:
                Collections.sort(words);
                Collections.reverse(words);
                break;
            case Session.RANDOM:
                break;
        }
    }

    private void saveSession() {
        currentSession.setToRevise(words);
        currentSession.setMastered(mastered);
        SessionsUtil.saveSession(currentSession);
    }

    AlertDialog mDialog;
    String lastItem;
    boolean addAgain = false;

    public void undoLastAction(){
        tvWord.setText(lastItem);
        if(addAgain){
            words.add(lastItem);
            sortWords();
            findViewById(R.id.flWordsContainer).setVisibility(View.GONE);
            mastered.remove(lastItem);
            saveSession();
            ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
        }
        setUndoVisibility(false);
    }

    public String getNextWord(int sortOrder){
        if (words.size() == 0 || words.isEmpty()) {
            Toast.makeText(getBaseContext(), "You have finished all the words, Congratulations!! :)", Toast.LENGTH_LONG).show();
            findViewById(R.id.bMeaning).setVisibility(View.GONE);
            findViewById(R.id.bYes).setVisibility(View.GONE);
            findViewById(R.id.bNo).setVisibility(View.GONE);
            return "Congratulations!!";
        }
        switch (sortOrder){
            case Session.ASCENDING:
            case Session.DECENDING:
            default:
                Log.d("Index", index + "");
                Log.d("Words", words.toString());
                return words.get(index);
            case Session.RANDOM:
                return getRandomWord();
        }
    }

    public String getRandomWord() {

        return words.get(new Random().nextInt((words.size())));
    }

    public void setUndoVisibility(boolean undoVisibility) {
        if(undoItem != null){
            undoItem.setVisible(undoVisibility);
        }
    }

    public class GetMeaning extends AsyncTask<String, Void, String> {

        String word;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.pbMeaning).setVisibility(View.VISIBLE);
            (findViewById(R.id.flWordsContainer)).setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            findViewById(R.id.pbMeaning).setVisibility(View.GONE);
            (findViewById(R.id.flWordsContainer)).setVisibility(View.VISIBLE);

            View mView = Utils.getMeaningsView(s, TestWordsActivity.this);
            findViewById(R.id.flWordsContainer).postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((ScrollView) findViewById(R.id.flWordsContainer)).fullScroll(ScrollView.FOCUS_UP);
                }
            }, 50);
            ((ScrollView)findViewById(R.id.flWordsContainer)).removeAllViews();

            if(mView != null) {
                ((ScrollView) findViewById(R.id.flWordsContainer)).addView(mView);

            } else {
                Toast.makeText(TestWordsActivity.this, "Oops! An error occurred. Try Again.", Toast.LENGTH_LONG).show();
                Utils.deleteWord(this.word);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            this.word = strings[0];
            if (Utils.hasWord(strings[0])) {
                Log.d("WordLearner", "Loading from memory");
                return Utils.getWordJSON(strings[0]);
            }
            Log.d("WordLearner", "Loading from my dictionary");
            return Utils.saveWord(strings[0], NetworkUtils.GET(Utils.FINAL_URL_SINGLE + strings[0].trim().toLowerCase(Locale.US)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_words, menu);


        return true;
    }

    MenuItem undoItem;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        undoItem = menu.findItem(R.id.action_undo).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        startActivity(new Intent(TestWordsActivity.this, SessionsListActivity.class));
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                startActivity(new Intent(TestWordsActivity.this, SessionsListActivity.class));
                return true;
            case R.id.action_undo:
                undoLastAction();
                return true;
            case R.id.action_edit:
                final AlertDialog.Builder builder = new AlertDialog.Builder(TestWordsActivity.this);

                builder.setTitle("Edit Word");
                final int index = words.indexOf(tvWord.getText().toString());
                final String oldWord = tvWord.getText().toString();

                View mView = LayoutInflater.from(TestWordsActivity.this).inflate(R.layout.edit_word, null);

                final EditText edit = (EditText) mView.findViewById(R.id.etEditWord);
                edit.setText(tvWord.getText().toString());

                builder.setView(mView);

                mView.findViewById(R.id.fabDone).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        words.remove(index);
                        words.add(index, edit.getText().toString());
                        saveSession();
                        sortWords();
                        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
                        toSave.remove(oldWord);
                        toSave.add(edit.getText().toString());
                        Collections.sort(toSave);
                        Utils.writeListToFile(toSave, Utils.WordsFile);
                        tvWord.setText(edit.getText().toString());
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        mDialog.cancel();
                    }
                });

                mView.findViewById(R.id.fabRemove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.deleteWord(words.get(index));
                        words.remove(index);
                        saveSession();
                        sortWords();
                        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
                        toSave.remove(oldWord);
                        Collections.sort(toSave);
                        Utils.writeListToFile(toSave, Utils.WordsFile);
                        tvWord.setText(getNextWord(currentSession.getSortOrder()));
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
                        mDialog.cancel();
                    }
                });


                mDialog = builder.create();
                mDialog.show();
                return true;
            case R.id.action_speak:
                ttobj.setSpeechRate(0.8f);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    ttobj.speak(tvWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
                } else {
                    ttobj.speak(tvWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
