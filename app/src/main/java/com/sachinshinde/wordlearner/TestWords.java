package com.sachinshinde.wordlearner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestWords extends Activity {

    ArrayList<String> words = new ArrayList<String>();
    TextView tvWord;
    GetMeaning getMeaning;

    @Override
    protected void onRestart() {
        words = Utils.loadListFromFile(Utils.SessionFile);
        tvWord.setText(words.get(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt("LastIndex", new Random().nextInt((words.size())))));
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Utils.writeListToFile(words, Utils.SessionFile);
        if(words.size() != 0)
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("LastIndex", words.indexOf(tvWord.getText().toString())).commit();
        else
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("LastIndex", 0).commit();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_words);

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
        }catch(Exception ex){
            tvWord.setText(getRandomWord());
        }
        ((TextView) findViewById(R.id.tvMeaning)).setText("");
        findViewById(R.id.pbMeaning).setVisibility(View.GONE);

        findViewById(R.id.bNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.bLinkify).setVisibility(View.GONE);
                tvWord.setText(getRandomWord());
                ((TextView) findViewById(R.id.tvMeaning)).setText("");
                isLink = false;
                findViewById(R.id.bLinkify).setBackgroundResource(R.drawable.main_button_green);
            }
        });

        findViewById(R.id.bYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.bLinkify).setVisibility(View.GONE);
                words.remove(tvWord.getText().toString());
                tvWord.setText(getRandomWord());
                ((TextView) findViewById(R.id.tvMeaning)).setText("");
                Utils.writeListToFile(words, Utils.SessionFile);
                ((TextView) findViewById(R.id.tvCount)).setText(words.size() + " WORDS TO GO.");
                isLink = false;
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

        findViewById(R.id.bSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected void onPreExecute() {
                        ((ImageButton) findViewById(R.id.bSpeak)).setImageResource(0);
                        findViewById(R.id.pbVoice).setVisibility(View.VISIBLE);
                        super.onPreExecute();
                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        String jsonString = NetworkUtils.GET("http://api.wordnik.com:80/v4/word.json/" + strings[0].trim().toLowerCase(Locale.getDefault()) + "/audio?useCanonical=false&limit=50&api_key=" + AddWords.API_KEY);
                        try {
                            JSONArray jsonArray = new JSONArray(jsonString);
                            if (jsonArray.length() == 1) {
                                return jsonArray.getJSONObject(0).getString("fileUrl");
                            } else if (jsonArray.length() > 1) {
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
                            ((ImageButton) findViewById(R.id.bSpeak)).setImageResource(R.drawable.speaker);
                            findViewById(R.id.pbVoice).setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(), "No audio found", Toast.LENGTH_LONG).show();
                        } else {
                            final MediaPlayer mp = new MediaPlayer();
                            try {
                                mp.setDataSource(s);
                                mp.prepareAsync();
                                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        ((ImageButton) findViewById(R.id.bSpeak)).setImageResource(R.drawable.speaker);
                                        findViewById(R.id.pbVoice).setVisibility(View.GONE);
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
                }.execute(tvWord.getText().toString());
            }
        });

        findViewById(R.id.bEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TestWords.this);
//                builder.setTitle("Edit");
                final int index = words.indexOf(tvWord.getText().toString());
                final String oldWord = tvWord.getText().toString();
                final EditText edit = new EditText(TestWords.this);
                edit.setText(tvWord.getText().toString());
                builder.setView(edit);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

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
                    }
                });

                builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                    }
                });

                builder.show();
            }
        });


        findViewById(R.id.bLinkify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.bLinkify).setBackgroundResource(isLink ? R.drawable.main_button_green : R.drawable.main_button_red);
                setText(((TextView) findViewById(R.id.tvMeaning)).getText().toString(),!isLink);
            }
        });

    }

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


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.pbMeaning).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvMeaning)).setText("");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            findViewById(R.id.pbMeaning).setVisibility(View.GONE);
            isLink = false;
            setText(s, false);
            if(!s.isEmpty() || s != null){
                findViewById(R.id.bLinkify).setVisibility(View.VISIBLE);
            }
            findViewById(R.id.tvMeaning).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (Utils.hasWord(strings[0])) {
                Log.d("WordLearner", "Loading from memory");
                return Utils.getDefinition(strings[0]);
            }
            Log.d("WordLearner", "Loading from wordnik");
            return Utils.saveWord(strings[0], Utils.jsonToString(NetworkUtils.GET(Utils.URL1 + strings[0].trim().toLowerCase(Locale.US) + Utils.URL2)));
        }
    }


    boolean isLink = false;

    public class ClickableURLSpan extends URLSpan {
        public ClickableURLSpan(String url) {
            super(url);
        }
        AlertDialog dialog;
        @Override
        public void onClick(View widget) {
            final String clickedText = getURL().trim().toLowerCase(Locale.getDefault());
            final AlertDialog.Builder builder = new AlertDialog.Builder(TestWords.this);
            builder.setTitle(clickedText.toUpperCase(Locale.getDefault()));
            ProgressBar pb = new ProgressBar(TestWords.this);
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
                                            String jsonString = NetworkUtils.GET("http://api.wordnik.com:80/v4/word.json/" + strings[0].trim().toLowerCase(Locale.getDefault()) + "/audio?useCanonical=false&limit=50&api_key=" + AddWords.API_KEY);
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
                                    }.execute(clickedText);

                                }
                            });
                        }
                    });
                    mDialog.show();
                    dialog.cancel();
                    super.onPostExecute(s);
                }
            }.execute(clickedText);
            dialog = builder.create();
            dialog.show();
        }
    }

    public void setText(String input, boolean linkify){

        if(linkify) {
            isLink = true;
            SpannableStringBuilder builder = new SpannableStringBuilder(input);

            Pattern pattern = Pattern.compile("[a-zA-Z]+");
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                String text = input.subSequence(start, end).toString();

                ClickableURLSpan url = new ClickableURLSpan(text);
                builder.setSpan(url, start, end, 0);
            }

            ((TextView) findViewById(R.id.tvMeaning)).setText(builder);
            ((TextView) findViewById(R.id.tvMeaning)).setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            isLink = false;
            ((TextView) findViewById(R.id.tvMeaning)).setText(input);
        }
    }

    boolean done = false;
}
