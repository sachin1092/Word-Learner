package com.sachinshinde.wordlearner.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.module.Meaning;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.module.SubWords;
import com.sachinshinde.wordlearner.module.Word;
import com.sachinshinde.wordlearner.module.Words_1454;
import com.sachinshinde.wordlearner.module.Words_3817;
import com.sachinshinde.wordlearner.module.Words_4178;
import com.sachinshinde.wordlearner.services.DownloadService;
import com.sachinshinde.wordlearner.util.IabHelper;
import com.sachinshinde.wordlearner.util.IabResult;
import com.sachinshinde.wordlearner.util.Inventory;
import com.sachinshinde.wordlearner.util.Purchase;
import com.sachinshinde.wordlearner.utils.Constants;
import com.sachinshinde.wordlearner.utils.FilePath;
import com.sachinshinde.wordlearner.utils.NetworkUtils;
import com.sachinshinde.wordlearner.utils.ProcessWord;
import com.sachinshinde.wordlearner.utils.SerialPreference;
import com.sachinshinde.wordlearner.utils.SessionsUtil;
import com.sachinshinde.wordlearner.utils.Utils;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements
        DirectoryChooserFragment.OnFragmentInteractionListener {

    private static final int FILE_SELECT_CODE = 0;
    private static final String FIRST_TIME_HELP = "first_time";
    View mExportDialogView;
    View importDialogView;
    private DirectoryChooserFragment mDialog;
    private android.support.v7.app.AlertDialog mImportDialog;
    BroadcastReceiver mProgressReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(DownloadService.PROGRESS, 10);
            int total = intent.getIntExtra(DownloadService.TOTAL, 100);
            if (importDialogView != null) {
                ((TextView) importDialogView.findViewById(R.id.tvProgress)).setText(Html.fromHtml(progress + " / " + total));
            }

            if (intent.getIntExtra(DownloadService.RESULT, DownloadService.RESULT_CANCELED) == DownloadService.RESULT_OK) {
                mImportDialog.dismiss();
            }
        }
    };
    private android.support.v7.app.AlertDialog mExportDialog;
    private boolean alreadyOpened = false;
    private boolean firsttime = false;


    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
                cursor.close();
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public class OneTimeThing extends AsyncTask<Void, Void, Void> {


        private Context mContext;

        public OneTimeThing(Context mContext) {
            this.mContext = mContext;
        }

        private Animation createHintSwitchAnimation() {
            Animation animation = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            animation.setStartOffset(0);
            animation.setDuration(1300);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.RESTART);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setFillAfter(true);

            return animation;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.rlsplash).setVisibility(View.VISIBLE);
            findViewById(R.id.containerMain).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.ivsplashedit))
                    .setImageBitmap(Utils.loader(mContext));
            (findViewById(R.id.ivsplashedit))
                    .startAnimation(createHintSwitchAnimation());
        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Utils.unZipIt(getAssets().open("word_definitions.zip"), Utils.WORDS_PATH);
                Utils.unZipIt(Utils.WORDS_PATH + File.separator + "Words.zip", Utils.WORDS_PATH + File.separator + "Words");
                File mFile = new File(Utils.WORDS_PATH + File.separator + "Words.zip");
                mFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            findViewById(R.id.rlsplash).setVisibility(View.GONE);
            findViewById(R.id.containerMain).setVisibility(View.VISIBLE);
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(
                    FIRST_TIME_HELP, false).commit();

            initialise();

            findViewById(R.id.buttonImport).performClick();
            Toast.makeText(MainActivity.this, "Please import a word list to continue.", Toast.LENGTH_LONG).show();

        }

    }

    public void initialise() {
        mDialog = DirectoryChooserFragment.newInstance("Word Learner", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            ab.setTitle("");
        } catch (Exception ex) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        findViewById(R.id.buttonAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddWordsActivity.class));
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                finish();
            }
        });

        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, SessionsListActivity.class));
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                finish();
            }
        });

        findViewById(R.id.buttonExport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                android.support.v7.app.AlertDialog.Builder mExportDialogBuilder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                mExportDialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.export_dialog, null);

                String dir = Environment.getExternalStorageDirectory().getAbsolutePath();

                ((TextView) mExportDialogView.findViewById(R.id.tvExportDirectory)).setText(dir);


                mExportDialogView.findViewById(R.id.bExport).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = ((TextView) mExportDialogView.findViewById(R.id.tvExportDirectory)).getText().toString();
                        boolean index = ((CheckBox) mExportDialogView.findViewById(R.id.cbIndex)).isChecked();
                        boolean meaning = ((CheckBox) mExportDialogView.findViewById(R.id.cbMeaning)).isChecked();
                        boolean exampleSentence = ((CheckBox) mExportDialogView.findViewById(R.id.cbSentence)).isChecked();
                        boolean synonyms = ((CheckBox) mExportDialogView.findViewById(R.id.cbSynonyms)).isChecked();
                        boolean antonyms = ((CheckBox) mExportDialogView.findViewById(R.id.cbAntonyms)).isChecked();
                        new ExportWords(path, index, meaning, exampleSentence, synonyms, antonyms).execute();

                    }
                });

                mExportDialogView.findViewById(R.id.containerDirectory).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.show(getFragmentManager(), null);
                    }
                });

                mExportDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mExportDialogView = null;
                        mExportDialog = null;
                    }
                });

                mExportDialogBuilder.setView(mExportDialogView);
                mExportDialog = mExportDialogBuilder.show();
            }
        });

        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner");
        File mFile = new File(mDir.getPath() + "/" + Utils.WordsFile);
        if (!mFile.exists()) {
            ArrayList<String> list = SerialPreference.retPrefs(getBaseContext());
            if (list != null) {
                Utils.writeListToFile(list, Utils.WordsFile);
            }
        }

        findViewById(R.id.buttonImport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.import_dialog, null);
                mBuilder.setView(mView);
                if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(
                        FIRST_TIME_HELP + "_import_dialog", true)) {
                    mBuilder.setCancelable(false);
                }

                mBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(
                                FIRST_TIME_HELP + "_import_dialog", false).commit();
                        alreadyOpened = false;


                    }
                });

                final AlertDialog mMyImportDialog = mBuilder.create();
                if (!alreadyOpened) {
                    mMyImportDialog.show();
                }
                alreadyOpened = true;

                mBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                            if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(
                                    FIRST_TIME_HELP + "_import_dialog", true)) {
                                Toast.makeText(MainActivity.this, "Please import a word list first.", Toast.LENGTH_SHORT).show();
                                return true;
                            }

                        }
                        mMyImportDialog.dismiss();
                        return false;
                    }
                });


                mView.findViewById(R.id.import1454).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<String> words = Utils.loadListFromFile(Utils.WordsFile);
                        words.addAll(new Words_1454().import1454);
                        Utils.writeListToFile(words, Utils.WordsFile);
                        Toast.makeText(MainActivity.this, "Words Added Successfully", Toast.LENGTH_SHORT).show();
                        if (SessionsUtil.getSessionCount() == 0 && words.size() > 0) {
                            Session session = new Session();
                            session.setSessionName("All Words");
                            session.setSortOrder(0);
                            session.setMastered(new ArrayList<String>());
                            session.setToRevise(Utils.loadListFromFile(Utils.WordsFile));
                            SessionsUtil.saveSession(session);
                        }
                        mMyImportDialog.dismiss();
                    }
                });

                mView.findViewById(R.id.import3817).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<String> words = Utils.loadListFromFile(Utils.WordsFile);
                        words.addAll(new Words_3817().import3817);
                        Utils.writeListToFile(words, Utils.WordsFile);
                        Toast.makeText(MainActivity.this, "Words Added Successfully", Toast.LENGTH_SHORT).show();
                        if (SessionsUtil.getSessionCount() == 0 && words.size() > 0) {
                            Session session = new Session();
                            session.setSessionName("All Words");
                            session.setSortOrder(0);
                            session.setMastered(new ArrayList<String>());
                            session.setToRevise(Utils.loadListFromFile(Utils.WordsFile));
                            SessionsUtil.saveSession(session);
                        }
                        mMyImportDialog.dismiss();
                    }
                });

                mView.findViewById(R.id.import4178).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<String> words = Utils.loadListFromFile(Utils.WordsFile);
                        words.addAll(new Words_4178().import4178);
                        Utils.writeListToFile(words, Utils.WordsFile);
                        Toast.makeText(MainActivity.this, "Words Added Successfully", Toast.LENGTH_SHORT).show();

                        if (SessionsUtil.getSessionCount() == 0 && words.size() > 0) {
                            Session session = new Session();
                            session.setSessionName("All Words");
                            session.setSortOrder(0);
                            session.setMastered(new ArrayList<String>());
                            session.setToRevise(Utils.loadListFromFile(Utils.WordsFile));
                            SessionsUtil.saveSession(session);

                        }
                        mMyImportDialog.dismiss();
                    }
                });

                mView.findViewById(R.id.importChoice).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mMyImportDialog.dismiss();

                        if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(
                                FIRST_TIME_HELP + "_import", true)) {
                            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(
                                    FIRST_TIME_HELP + "_import", false).commit();
                            AlertDialog.Builder mHelpBuilder = new AlertDialog.Builder(MainActivity.this);
                            mHelpBuilder.setView(R.layout.import_help);
                            mHelpBuilder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "Select a text file with words.", Toast.LENGTH_SHORT).show();
                                    showFileChooser();
                                }
                            });

                            mHelpBuilder.show();

                        } else {
                            Toast.makeText(MainActivity.this, "Select a text file with words.", Toast.LENGTH_SHORT).show();
                            showFileChooser();
                        }

                    }
                });

            }
        });



            findViewById(R.id.adView).setVisibility(View.GONE);
            findViewById(R.id.buttonRemoveAds).setVisibility(View.GONE);



        findViewById(R.id.buttonRemoveAds).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mDonateBuilder = new AlertDialog.Builder(MainActivity.this);
                mDonateBuilder.setTitle("Donation");
                final View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.donate_dialog, null);
                mDonateBuilder.setView(mView);
//                mDonateBuilder.setAdapter(new ArrayAdapter<String>(getBaseContext(),
//                        android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.donate_amount)), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        onUpgradeAppButtonClicked(i);
//                    }
//                });
                mDonateBuilder.setPositiveButton("DONATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int choice = 1;
                        switch(((RadioGroup)mView.findViewById(R.id.rgDonation)).getCheckedRadioButtonId()){
                            case R.id.rgDonation1:
                                choice = 0;
                                break;
                            case R.id.rgDonation2:
                                choice = 1;
                                break;
                            case R.id.rgDonation3:
                                choice = 2;
                                break;
                        }
                        onUpgradeAppButtonClicked(choice);
                    }
                });
                mDonateBuilder.setNegativeButton("CANCEL", null);
                mDonateBuilder.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(
                FIRST_TIME_HELP, true)) {
            new OneTimeThing(getBaseContext()).execute();
            firsttime = true;
        } else {
            initialise();
        }

        if (Utils.getAppCount(getBaseContext()) == PreferenceManager
                .getDefaultSharedPreferences(getBaseContext()).getInt("showAt",
                        10)) {

            showRateDialog();
        }
        Utils.incAppCount(getBaseContext());

        try {
            setUpBillings();
        } catch (Exception ex) {
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<String> wordsList = Utils.loadListFromFile(Utils.WordsFile);
        if (!firsttime) {

            if (wordsList == null || wordsList.size() == 0) {
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(
                        FIRST_TIME_HELP + "_import_dialog", true).commit();
                Toast.makeText(MainActivity.this, "Please import a word list to continue.", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.buttonImport).performClick();
                    }
                }, 200);

            }
        }

        if (wordsList != null)
            if (SessionsUtil.getSessionCount() == 0 && wordsList.size() > 0) {
                Session session = new Session();
                session.setSessionName("All Words");
                session.setSortOrder(0);
                session.setMastered(new ArrayList<String>());
                session.setToRevise(Utils.loadListFromFile(Utils.WordsFile));
                SessionsUtil.saveSession(session);
            }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
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
                        if (path == null) {
                            path = FilePath.getPath(getBaseContext(), uri);
                        }

                        registerReceiver(mProgressReceivers, new IntentFilter(DownloadService.NOTIFICATION));

                        mImportDialog = getProgressBuilder(MainActivity.this, "<b>Please wait...</b><br/>Downloading definitions", "- / -").show();

                        Intent intent = new Intent(this, DownloadService.class);
                        intent.putExtra(DownloadService.FILEPATH, path);
                        startService(intent);

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.d("Word Learner", "File Path: " + path);
                }
                break;
            case RC_REQUEST:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public android.support.v7.app.AlertDialog.Builder getProgressBuilder(Activity activity, String msg, String progress) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        importDialogView = LayoutInflater.from(activity).inflate(
                R.layout.import_progress_dialog, null);
        View img1 = importDialogView.findViewById(R.id.pd_circle1);
        View img2 = importDialogView.findViewById(R.id.pd_circle2);
        View img3 = importDialogView.findViewById(R.id.pd_circle3);
        int ANIMATION_DURATION = 400;
        Animator anim1 = Utils.setRepeatableAnim(activity, img1, ANIMATION_DURATION, R.animator.growndisappear);
        Animator anim2 = Utils.setRepeatableAnim(activity, img2, ANIMATION_DURATION, R.animator.growndisappear);
        Animator anim3 = Utils.setRepeatableAnim(activity, img3, ANIMATION_DURATION, R.animator.growndisappear);
        Utils.setListeners(img1, anim1, anim2, ANIMATION_DURATION);
        Utils.setListeners(img2, anim2, anim3, ANIMATION_DURATION);
        Utils.setListeners(img3, anim3, anim1, ANIMATION_DURATION);
        anim1.start();

        ((TextView) importDialogView.findViewById(R.id.tvMessage)).setText(Html.fromHtml(msg));
        ((TextView) importDialogView.findViewById(R.id.tvProgress)).setText(Html.fromHtml(progress));

        builder.setView(importDialogView);
        builder.setCancelable(false);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                importDialogView = null;
                mImportDialog = null;
                unregisterReceiver(mProgressReceivers);
            }
        });

        importDialogView.findViewById(R.id.bMinimise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImportDialog.dismiss();
            }
        });
        return builder;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            importDialogView = null;
            unregisterReceiver(mProgressReceivers);
        } catch (Exception ex) {
        }
        try {
            if (mHelper != null)
                mHelper.dispose();
            mHelper = null;
        } catch (Exception ex) {
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
            case R.id.action_about:
                startActivity(new Intent(getBaseContext(), AboutClass.class));
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                finish();
                return true;
            case R.id.action_morebydev:
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=sachin+shinde")));
                return true;
            case R.id.action_rate:
                Utils.launchMarket(getBaseContext(), getPackageName());
                return true;
            case R.id.action_share:
                startActivity(createShareIntent());
                return true;
            case R.id.action_reset:
                showResetDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResetDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Warning");
        mBuilder.setMessage("This will delete all Words and Sessions.\n\nAre you sure you want to reset everything?");
        mBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.deleteEverything(MainActivity.this);
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        mBuilder.setNegativeButton("CANCEL", null);

        mBuilder.show();
    }

    private Intent createShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(
                Intent.EXTRA_TEXT,
                "Checkout this Amazing App\nWord Learner\nGet it now from Playstore\n"
                        + Uri.parse("http://play.google.com/store/apps/details?id="
                        + getPackageName()));

        return Intent.createChooser(intent, "Share");
    }

    @Override
    public void onSelectDirectory(String path) {
//        new ExportWords(path).execute();
        if (mExportDialogView != null)
            ((TextView) mExportDialogView.findViewById(R.id.tvExportDirectory)).setText(path);
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }

    public class ExportWords extends AsyncTask<String, Void, String> {

        AlertDialog.Builder mBuilder;
        AlertDialog mDialog;
        private String path;
        private boolean index;
        private boolean showMeaning;
        private boolean exampleSentence;
        private boolean showSynonyms;
        private boolean showAntonyms;


        public ExportWords(String path, boolean index, boolean showMeaning, boolean exampleSentence, boolean synonyms, boolean antonyms) {
            this.path = path;
            this.index = index;
            this.showMeaning = showMeaning;
            this.exampleSentence = exampleSentence;
            this.showSynonyms = synonyms;
            this.showAntonyms = antonyms;
            mBuilder = new AlertDialog.Builder(MainActivity.this);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mExportDialog.dismiss();
            mBuilder.setView(Utils.getProgressView(MainActivity.this, "Exporting..."));
            mDialog = mBuilder.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getBaseContext(), "Export successful", Toast.LENGTH_LONG).show();
            mDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... strings) {
            ArrayList<String> word_list = Utils.loadListFromFile(Utils.WordsFile);
            Collections.sort(word_list);
            File mDir = new File(path);
            File mFile = new File(mDir.getPath() + "/Exported List.txt");
            try {
                // Assume default encoding.
                FileWriter fileWriter =
                        new FileWriter(mFile);

                // Always wrap FileWriter in BufferedWriter.
                BufferedWriter bufferedWriter =
                        new BufferedWriter(fileWriter);

                char[] sections = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                // Note that write() does not automatically
                // append a newline character.
                for (int i = 0; i < word_list.size(); i++) {

                    if ((showMeaning || exampleSentence || showSynonyms || showAntonyms)) {
                        bufferedWriter.newLine();
                        bufferedWriter.newLine();
                        bufferedWriter.write("--------------------------------------------------");
                        bufferedWriter.newLine();
                    }
                    if (index)
                        bufferedWriter.write((i + 1) + ") ");
                    bufferedWriter.write(word_list.get(i));
                    bufferedWriter.newLine();
                    if ((showMeaning || exampleSentence || showSynonyms || showAntonyms)) {
                        bufferedWriter.write("--------------------------------------------------");
                        bufferedWriter.newLine();
                    }
                    if (showMeaning || exampleSentence || showSynonyms || showAntonyms)
                        try {
                            if (!Utils.hasWord(word_list.get(i)))
                                Utils.saveWord(word_list.get(i), NetworkUtils.GET(Utils.FINAL_URL_SINGLE + word_list.get(i).trim().toLowerCase(Locale.US)));

                            Word word = ProcessWord.getDefinition(Utils.getWordJSON(word_list.get(i)));

                            ArrayList<SubWords> subWords = word.getWords();

                            for (int x = 0; x < subWords.size(); x++) {

                                SubWords subWord = subWords.get(x);

                                bufferedWriter.write(String.format(subWord.getWord()));
                                bufferedWriter.newLine();

                                ArrayList<Meaning> meanings = subWord.getMeanings();
                                for (int j = 0; j < meanings.size(); j++) {
                                    Meaning meaning = meanings.get(j);

                                    if (showMeaning) {
                                        bufferedWriter.write(sections[j % 26] + ". ");

                                        bufferedWriter.write(meaning.getMeaning() != null ? meaning.getMeaning() : "");
                                        bufferedWriter.newLine();
                                    }

                                    if (meaning.getExamples() != null && exampleSentence) {
                                        bufferedWriter.write("Example Sentences: \n");
                                        bufferedWriter.write(meaning.getExamples().toString().replace("[", "").replace("]", ""));
                                        bufferedWriter.newLine();
                                    }

                                    if (meaning.getSynonyms() != null && showSynonyms) {
                                        bufferedWriter.write("Synonyms: \n");
                                        bufferedWriter.write(meaning.getSynonyms().toString().replace("[", "").replace("]", ""));
                                        bufferedWriter.newLine();
                                    }

                                    if (meaning.getAntonyms() != null && showAntonyms) {
                                        bufferedWriter.write("Antonyms: \n");
                                        bufferedWriter.write(meaning.getAntonyms().toString().replace("[", "").replace("]", ""));
                                        bufferedWriter.newLine();
                                    }

                                }


                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Error Utils", e.getMessage());
                        }
                }
                Log.d("WordLearner", "Export completed");
                // Always close files.
                bufferedWriter.close();
            } catch (IOException ex) {
                System.out.println(
                        "Error writing to file '"
                                + "Exported List.txt" + "'");
                // Or we could just do this:
                // ex.printStackTrace();
            }

            return null;
        }
    }


    private void showRateDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                MainActivity.this);
        builder.setTitle("Rate");
        builder.setIcon(R.drawable.word_learner);
        builder.setMessage("Rate and support us.\nGreat reviews keep us going. :)");
        builder.setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                    Utils.launchMarket(getBaseContext(), getPackageName());

            }
        });

        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                        .putInt("showAt", 2 * getPrefs.getInt("showAt", 10))
                        .commit();
            }
        });
        builder.show();

    }

    //Billing related code premium upgrade (non-consumable) and gas
    // (consumable)
    static final String SKU_PREMIUM1 = "word_learner_2.99";
    static final String SKU_PREMIUM2 = "word_learner_4.99";
    static final String SKU_PREMIUM3 = "word_learner_9.99";

    // (arbitrary) request code for the purchase flcow
    static final int RC_REQUEST = 100011;
    static final int RC_REQUEST_299 = 100012;
    static final int RC_REQUEST_499 = 100013;

    private IabHelper mHelper;

    boolean mIsPremium = false;
    // SKUs for our products: the
    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked(final int choice) {
        Log.d("WordLearner", "Upgrade button clicked; launching purchase flow for upgrade.");
        String SKU_PREMIUM = SKU_PREMIUM1;
        switch(choice){
            case 0:
                SKU_PREMIUM = SKU_PREMIUM1;
                break;
            case 1:
                SKU_PREMIUM = SKU_PREMIUM2;
                break;
            case 2:
                SKU_PREMIUM = SKU_PREMIUM3;
                break;
        }

        try {

            mHelper.launchPurchaseFlow(MainActivity.this, SKU_PREMIUM,
                    RC_REQUEST, mPurchaseFinishedListener, generatePayLoad());
        } catch (Exception ex) {

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    try {
                        String SKU_PREMIUM = SKU_PREMIUM1;
                        switch(choice){
                            case 0:
                                SKU_PREMIUM = SKU_PREMIUM1;
                                break;
                            case 1:
                                SKU_PREMIUM = SKU_PREMIUM2;
                                break;
                            case 2:
                                SKU_PREMIUM = SKU_PREMIUM3;
                                break;
                        }
                        mHelper.launchPurchaseFlow(MainActivity.this,
                                SKU_PREMIUM, RC_REQUEST,
                                mPurchaseFinishedListener, generatePayLoad());
                    } catch (Exception e) {
                        try {
                            new Handler().postDelayed(this, 1000);
                        } catch (Exception ex) {
                            try {
                                Looper.prepare();
                                Toast.makeText(getBaseContext(),
                                        "Error!! Try Again.", Toast.LENGTH_LONG)
                                        .show();
                            } catch (Exception ex1) {

                            }
                        }
                    }
                }
            }, 1200);

        }
    }


    private String[] getAccountNames() {
        AccountManager mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType("com.google");
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    public String generatePayLoad() {

        StringBuilder build = new StringBuilder();
        String[] arr = getAccountNames();
        for (String anArr : arr) build.append(anArr).append("//");

        // new Logger(getBaseContext()).Toast(build.toString());
        return build.toString();
    }

    private void setUpBillings() {

        mHelper = new IabHelper(this, Constants.LICENSE_KEY);

        mHelper.enableDebugLogging(true, "WordLearner");

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("WordLearner", "Problem setting up In-app Billing: " + result);
                    // complain("Problem setting up in-app billing: " + result);
                    return;
                } // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                    Log.d("WordLearner", "Setup successful. Querying inventory.");

                // IAB is fully set up. Now, let's get an inventory of stuff we
                // own.
                Log.d("WordLearner", "Setup successful. Querying inventory.");

                mHelper.queryInventoryAsync(mGotInventoryListener);
                // Hooray, IAB is fully set up!

            }
        });

    }

    // Listener that's called when we finish querying the items and
    // subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            Log.d("WordLearner", "Query inventory finished.");


            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            // Is it a failure?
            if (result.isFailure()) {
                // complain("Failed to query inventory: " + result);
                return;
            }

            Log.d("WordLearner", "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

            // Do we have the premium upgrade?
            Purchase premiumPurchase1 = inventory.getPurchase(SKU_PREMIUM1);
            Purchase premiumPurchase2 = inventory.getPurchase(SKU_PREMIUM2);
            Purchase premiumPurchase3 = inventory.getPurchase(SKU_PREMIUM3);
            mIsPremium = (premiumPurchase1 != null && verifyDeveloperPayload(premiumPurchase1)) ||
                    (premiumPurchase2 != null && verifyDeveloperPayload(premiumPurchase2)) ||
                    (premiumPurchase3 != null && verifyDeveloperPayload(premiumPurchase3));
            Log.d("WordLearner", "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            new Handler().post(new Runnable() {

                @Override
                public void run() {

                    if (mIsPremium) {
                        PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext())
                                .edit().putString("re", "qze").commit();
                        findViewById(R.id.buttonRemoveAds).setVisibility(View.GONE);

                    } else {
                        PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext())
                                .edit().putString("re", "ezq").commit();
                        findViewById(R.id.buttonRemoveAds).setVisibility(
                                View.VISIBLE);

                            AdView mAdView = (AdView) findViewById(R.id.adView);
                            AdRequest adRequest = new AdRequest.Builder().build();
                            mAdView.loadAd(adRequest);
                            findViewById(R.id.adView).setVisibility(View.VISIBLE);
                            findViewById(R.id.buttonRemoveAds).setVisibility(View.VISIBLE);




                    }


                    Log.d("WordLearner", "Premium " + mIsPremium);

                }
            });

        }
    };



    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        String[] arr = getAccountNames();

        boolean isPayloadcorrect = false;

        for (int i = 0; i < arr.length; i++) {
            if (payload.contains(arr[i]))
                isPayloadcorrect = true;
        }

        // new Logger(getBaseContext()).Toast("Requested Payload was: "
        // + generatePayLoad() + " receieved payload is: " + payload
        // + " returning " + isPayloadcorrect);

        return isPayloadcorrect;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d("WordLearner", "Purchase finished: " + result + ", purchase: " + purchase);
            Toast.makeText(getBaseContext(), "Purchase finished: " + result
                    + ", purchase: " + purchase, Toast.LENGTH_LONG).show();

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                // complain("Error purchasing: " + result);
                // setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                // complain("Error purchasing. Authenticity verification failed.");
                // setWaitScreen(false);
                return;
            }

            if (purchase.getSku().equals(SKU_PREMIUM1) || purchase.getSku().equals(SKU_PREMIUM2) || purchase.getSku().equals(SKU_PREMIUM3)) {
                // bought the premium upgrade!
                Log.d("WordLearner", "Purchase successful.");
                Log.d("WordLearner", "Purchase is premium upgrade. Congratulating user.");
                Toast.makeText(getBaseContext(),
                        ("Thank you for donating. :)"),
                        Toast.LENGTH_LONG).show();
                mIsPremium = true;
//                findViewById(R.id.bgoPremium).setVisibility(View.GONE);
//                SwitchCompat sw = (SwitchCompat) findViewById(R.id.swEnableProf);
//                sw.setEnabled(true);
                // updateUi();
                // setWaitScreen(false);
            }
        }
    };

}
