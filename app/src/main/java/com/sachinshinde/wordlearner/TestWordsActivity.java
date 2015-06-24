package com.sachinshinde.wordlearner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestWordsActivity extends AppCompatActivity {

    ArrayList<String> words = new ArrayList<String>();
    TextView tvWord;
    GetMeaning getMeaning;
    private TextToSpeech ttobj;

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
        Utils.writeListToFile(words, Utils.SessionFile);
        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
        toSave.remove(word);
        Collections.sort(toSave);
        Utils.writeListToFile(toSave, Utils.WordsFile);
        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
    }

    private void addWord(String word) {
        words.add(word);
        Utils.writeListToFile(words, Utils.SessionFile);
        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
        toSave.add(word);
        Collections.sort(toSave);
        Utils.writeListToFile(toSave, Utils.WordsFile);
        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
    }

    @Override
    protected void onRestart() {
        words = Utils.loadListFromFile(Utils.SessionFile);
        tvWord.setText(words.get(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("LastIndex", new Random().nextInt((words.size())))));
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Utils.writeListToFile(words, Utils.SessionFile);
        if (words.size() != 0)
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("LastIndex", words.indexOf(tvWord.getText().toString())).commit();
        else
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("LastIndex", 0).commit();
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        words = Utils.loadListFromFile(getIntent().getStringExtra("file"));

//                SerialPreference.retPrefs(getBaseContext());
        if (words == null || words.isEmpty()) {
            Toast.makeText(getBaseContext(), "Sorry, no words. Add words first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvWord = (TextView) findViewById(R.id.tvWord);
        if (getIntent().getStringExtra("file").trim().equals(Utils.WordsFile))
            tvWord.setText(getRandomWord());
        else
            try {
                tvWord.setText(words.get(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("LastIndex", new Random().nextInt((words.size())))));
            } catch (Exception ex) {
                tvWord.setText(getRandomWord());
            }
//        ((TextView) findViewById(R.id.tvMeaning)).setText("");

        ((FrameLayout)findViewById(R.id.pbMeaning)).addView(Utils.getProgressView(TestWordsActivity.this));

        findViewById(R.id.pbMeaning).setVisibility(View.GONE);

        findViewById(R.id.flWordsContainer).setVisibility(View.GONE);

        findViewById(R.id.bNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.bLinkify).setVisibility(View.GONE);
                tvWord.setText(getRandomWord());
//                ((TextView) findViewById(R.id.tvMeaning)).setText("");
                findViewById(R.id.flWordsContainer).setVisibility(View.GONE);
                findViewById(R.id.bLinkify).setBackgroundResource(R.drawable.main_button_green);
            }
        });

        findViewById(R.id.bYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.bLinkify).setVisibility(View.GONE);
                words.remove(tvWord.getText().toString());
                tvWord.setText(getRandomWord());
//                ((TextView) findViewById(R.id.tvMeaning)).setText("");
                findViewById(R.id.flWordsContainer).setVisibility(View.GONE);
                Utils.writeListToFile(words, Utils.SessionFile);
                ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
                findViewById(R.id.bLinkify).setBackgroundResource(R.drawable.main_button_green);
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

        findViewById(R.id.bSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ttobj.setSpeechRate(0.8f);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    ttobj.speak(tvWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
                } else {
                    ttobj.speak(tvWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });


        findViewById(R.id.bEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        Utils.writeListToFile(words, Utils.SessionFile);
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
                        Utils.writeListToFile(words, Utils.SessionFile);
                        ArrayList<String> toSave = Utils.loadListFromFile(Utils.WordsFile);
                        toSave.remove(oldWord);
                        Collections.sort(toSave);
                        Utils.writeListToFile(toSave, Utils.WordsFile);
                        tvWord.setText(getRandomWord());
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
                        mDialog.cancel();
                    }
                });


                mDialog = builder.create();
                mDialog.show();
            }
        });

//
//        findViewById(R.id.bLinkify).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                findViewById(R.id.bLinkify).setBackgroundResource(isLink ? R.drawable.main_button_green : R.drawable.main_button_red);
////                setText(((TextView) findViewById(R.id.tvMeaning)).getText().toString(), !isLink);
//            }
//        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    AlertDialog mDialog;

    public String getRandomWord() {
        if (words.size() == 0 || words.isEmpty()) {
            Toast.makeText(getBaseContext(), "You have finished all the words, Congratulations!! :)", Toast.LENGTH_LONG).show();
            findViewById(R.id.bSpeak).setVisibility(View.GONE);
            findViewById(R.id.bEdit).setVisibility(View.GONE);
            findViewById(R.id.bMeaning).setVisibility(View.GONE);
            findViewById(R.id.bYes).setVisibility(View.GONE);
            findViewById(R.id.bNo).setVisibility(View.GONE);
            return "Congratulations!!";
        }
        return words.get(new Random().nextInt((words.size())));
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
//            setText(s, false);
//            if (!s.isEmpty() || s != null) {
//                findViewById(R.id.bLinkify).setVisibility(View.VISIBLE);
//            }
//            findViewById(R.id.tvMeaning).setVisibility(View.VISIBLE);

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
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        super.onBackPressed();
    }

}
