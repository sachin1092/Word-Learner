package com.sachinshinde.wordlearner.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.ListAdapter;
import com.sachinshinde.wordlearner.utils.Constants;
import com.sachinshinde.wordlearner.utils.NetworkUtils;
import com.sachinshinde.wordlearner.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


public class AddWordsActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et;
    View bAdd;
    ListView lvWords;
    ArrayList<String> words;
    ListAdapter mAdapter;
    TextToSpeech ttobj;
    AlertDialog dialog;
    boolean done = false;
    ArrayList<String> sessionList = new ArrayList<String>();
    android.support.v7.app.AlertDialog mDialog;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equalsIgnoreCase(Utils.INTENT_ADD_WORD)) {
                    Toast.makeText(getBaseContext(), "Added " + intent.getStringExtra("WORD"), Toast.LENGTH_LONG).show();
                    addWord(intent.getStringExtra("WORD"), false);
                } else if (intent.getAction().equalsIgnoreCase(Utils.INTENT_DELETE_WORD)) {

                    deleteWord(intent.getStringExtra("WORD"));
                } else if (intent.getAction().equalsIgnoreCase(Utils.INTENT_REFRESH)) {
                    words.clear();
                    words = Utils.loadListFromFile(Utils.WordsFile);
                    if (words == null) {
                        words = new ArrayList<String>();
                    }

                    Collections.sort(words);
                    mAdapter = new ListAdapter(getBaseContext(), words);
                    lvWords.setAdapter(mAdapter);

                    ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + words.size() + " words");
                }
            }
        }
    };
    private ShowcaseView showcaseView;
    private boolean mIsPremium;

    public void addWord(String wordStr, boolean openDialog) {
        wordStr = wordStr.toLowerCase(Locale.US).trim();
        words.add(wordStr);
        Collections.sort(words);
        ((ListAdapter) lvWords.getAdapter()).addToList(wordStr);
        ((ListAdapter) lvWords.getAdapter()).notifyDataSetChanged();
        Utils.writeListToFile(mAdapter.getList(), Utils.WordsFile);
        addToSessionList(wordStr);
        if (openDialog)
            try {
                performItemClick(mAdapter.getPosition(wordStr));
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("Error", ex.getMessage());
            }
        ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
    }

    public void deleteWord(String wordStr) {
        Utils.deleteWord(wordStr);
        mAdapter.mList.remove(wordStr);
        words.remove(wordStr);
        mAdapter.notifyDataSetChanged();
        Utils.writeListToFile(mAdapter.getList(), Utils.WordsFile);
        ((TextView) findViewById(R.id.tvCount)).setText("Total Count: " + mAdapter.getList().size() + " words");
    }

    public void performLongItemClick(final int pos) {
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
                deleteWord(mAdapter.getItem(pos));
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                mDialog.cancel();
            }
        });


        mDialog = builder.create();
        mDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ab.setHomeAsUpIndicator(R.drawable.word_learner);
            ab.setTitle("Words List");
//        ab.setDisplayHomeAsUpEnabled(true);
        } catch (Exception ex) {
        }
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

        if(words == null) words = new ArrayList<>();

        if(words.size() == 0){
            findViewById(R.id.tvNoWords).setVisibility(View.VISIBLE);
            findViewById(R.id.wordListContainer).setVisibility(View.GONE);
        } else {
            findViewById(R.id.tvNoWords).setVisibility(View.GONE);
            findViewById(R.id.wordListContainer).setVisibility(View.VISIBLE);

            showcaseView = new ShowcaseView.Builder(this).withMaterialShowcase()
                    .setTarget(Target.NONE).singleShot(10920)
                    .setOnClickListener(this)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .build();
            showcaseView.setContentTitle("Words");
            showcaseView.setContentText("Click on a word to see it's meaning.");
            showcaseView.setButtonText("Next");
            
//            AttributeSet attr = apiUtils.
            RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            mLayoutParams.setMargins(50, 0, 0, 200);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
            mLayoutParams.setMargins(margin, margin, margin, margin);
            showcaseView.setButtonPosition(mLayoutParams);
        }

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
//                if (et.getText().toString() != null && !et.getText().toString().isEmpty()) {
//                    addWord(et.getText().toString().trim(), true);
//                    et.setText("");
//                }
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddWordsActivity.this);
                mBuilder.setTitle("Add Word");
//                LinearLayout ll = new LinearLayout(AddWordsActivity.this);
//                ll.setPadding(50,50,50,50);
                final EditText etAdd = new EditText(AddWordsActivity.this);
//                ll.addView(etAdd);
                etAdd.setPadding(50, 50, 50, 50);
                mBuilder.setView(etAdd);
                mBuilder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etAdd.getText() != null && !etAdd.getText().toString().isEmpty()) {
                            addWord(etAdd.getText().toString().toLowerCase(Locale.US).trim(), true);
                            Toast.makeText(AddWordsActivity.this, "Added " + etAdd.getText().toString().trim(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                mBuilder.setNegativeButton("CANCEL", null);
                mBuilder.show();
            }
        });

        lvWords.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                try {
                    performItemClick(i);
                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }
        });


        lvWords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                try {
                    performLongItemClick(pos);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                if (et.getText().length() > 0) {
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

        mIsPremium = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext())
                .getString("re", "ezq").equalsIgnoreCase("qze");

        if(mIsPremium) {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }else{

            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            findViewById(R.id.adView).setVisibility(View.VISIBLE);

        }

    }

    public void performItemClick(final int position) {

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddWordsActivity.this);

        builder.setView(Utils.getProgressView(AddWordsActivity.this, "Loading..."));
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

                    final AlertDialog mNewDialog = builder.create();

                    buttonBar.findViewById(R.id.bEdit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            performLongItemClick(position);
                            mNewDialog.cancel();
                        }
                    });

                    buttonBar.findViewById(R.id.bSpeak).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                ttobj.speak(mAdapter.getItem(position), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
                            } else {
                                ttobj.speak(mAdapter.getItem(position), TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });


                    ((ImageButton) buttonBar.findViewById(R.id.bCancelorDelete)).setImageResource(R.drawable.ic_delete);
                    buttonBar.findViewById(R.id.bCancelorDelete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteWord(mAdapter.getItem(position));
                            mNewDialog.cancel();
                        }
                    });


                    buttonBar.findViewById(R.id.bDone).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mNewDialog.cancel();
                        }
                    });

                    mNewDialog.show();

                } else {
                    Toast.makeText(AddWordsActivity.this, "Oops! An error occurred. Try Again.", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    Utils.deleteWord(mAdapter.getItem(position));
                    return;
                }

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
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.INTENT_ADD_WORD);
        intentFilter.addAction(Utils.INTENT_DELETE_WORD);
        intentFilter.addAction(Utils.INTENT_REFRESH);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
        if (mFile.exists())
            Utils.writeListToFile(sessionList, Utils.SessionFile);
        super.onPause();
        unregisterReceiver(broadcastReceiver);
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
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(AddWordsActivity.this, MainActivity.class));
                return true;
//            case R.id.action_AtoZ:
//                item.setChecked(true);
//                mAdapter.setSortMode(0);
//                return true;
//            case R.id.action_ZtoA:
//                item.setChecked(true);
//                mAdapter.setSortMode(1);
//                return true;
//            case R.id.action_MostRevisedFirst:
//                item.setChecked(true);
//                mAdapter.setSortMode(3);
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_words, menu);
        return true;
    }*/

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(AddWordsActivity.this, MainActivity.class));
        super.onBackPressed();
    }


    private int counter = 0;
    @Override
    public void onClick(View view) {
        switch (counter) {
            case 0:
//                try {
//                    showcaseView.setShowcase(new ViewTarget(et), true);
//                }catch (Exception ex){
                    showcaseView.setShowcase(new ViewTarget(et), false);
//                }
                showcaseView.setContentTitle("Search");
                showcaseView.setContentText("Search words here.");
                break;

            case 1:
//                try {
//                    showcaseView.setShowcase(Target.NONE, true);
//                }catch (Exception ex){
                    showcaseView.setShowcase(Target.NONE, false);
//                }
                showcaseView.setContentTitle("More Options");
                showcaseView.setContentText("Long press on a word to get more options.");
                break;

            case 2:
//                try {
//                    showcaseView.setShowcase(new ViewTarget(bAdd), true);
//                }catch (Exception ex) {
                    showcaseView.setShowcase(new ViewTarget(bAdd), false);
//                }
                showcaseView.setContentTitle("Add Words");
                showcaseView.setContentText("Click here to Add new words.");
                showcaseView.setButtonText("GOT IT");
                break;

            case 3:
                showcaseView.hide();
                break;
        }
        counter++;
    }
}
