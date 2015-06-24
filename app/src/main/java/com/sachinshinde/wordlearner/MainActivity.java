package com.sachinshinde.wordlearner;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sachinshinde.wordlearner.activities.AboutClass;
import com.sachinshinde.wordlearner.services.DownloadService;
import com.sachinshinde.wordlearner.utils.FilePath;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements
        DirectoryChooserFragment.OnFragmentInteractionListener {

    private DirectoryChooserFragment mDialog;
    private android.support.v7.app.AlertDialog mImportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDialog = DirectoryChooserFragment.newInstance("Word Learner", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
//        ab.setHomeAsUpIndicator(R.drawable.word_learner);
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
                startActivity(new Intent(MainActivity.this, AddWordsActivity.class));
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
            }
        });

        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Session");
                builder.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item, new String[]{"New session", "Resume old session"}), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            startActivity(new Intent(MainActivity.this, TestWordsActivity.class).putExtra("file", Utils.WordsFile));
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        } else {
                            startActivity(new Intent(MainActivity.this, TestWordsActivity.class).putExtra("file", Utils.SessionFile));
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }
                    }
                });
                File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
                if (mFile.exists())
                    builder.show();
                else {
                    startActivity(new Intent(MainActivity.this, TestWordsActivity.class).putExtra("file", Utils.WordsFile));
                    overridePendingTransition(R.anim.slide_in_left,
                            R.anim.slide_out_right);
                }
            }
        });

        findViewById(R.id.buttonExport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show(getFragmentManager(), null);
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
                showFileChooser();
                Toast.makeText(MainActivity.this, "Select a text file with words.", Toast.LENGTH_SHORT).show();
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private static final int FILE_SELECT_CODE = 0;

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

                        mImportDialog = getProgressBuilder(MainActivity.this, "<b>Please wait...</b><br/>Downloading definitions", "0 / 100").show();

                        Intent intent = new Intent(this, DownloadService.class);
                        intent.putExtra(DownloadService.FILEPATH, path);
                        startService(intent);

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.d("Word Learner", "File Path: " + path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    BroadcastReceiver mProgressReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(DownloadService.PROGRESS, 10);
            int total = intent.getIntExtra(DownloadService.TOTAL, 100);
            if(importDialogView != null) {
                ((TextView) importDialogView.findViewById(R.id.tvProgress)).setText(Html.fromHtml(progress + " / " + total));
            }

            if(intent.getIntExtra(DownloadService.RESULT, DownloadService.RESULT_CANCELED) == DownloadService.RESULT_OK){
                mImportDialog.dismiss();
            }
        }
    };


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
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }



    View importDialogView;

    public android.support.v7.app.AlertDialog.Builder getProgressBuilder(Activity activity, String msg, String progress){
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
        }catch (Exception ex){}
    }

    public class ExportWords extends AsyncTask<String, Void, String> {

        String path;

        public ExportWords(String path) {
            this.path = path;
        }


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

                // Note that write() does not automatically
                // append a newline character.
                for (int i = 0; i < word_list.size(); i++) {
                    bufferedWriter.write(word_list.get(i));
//                    bufferedWriter.write(": ");
//                    if (Utils.hasWord(s)) {
//                        Log.d("WordLearner", "Loading from memory");
//                        try {
//                            bufferedWriter.write(Utils.getDefinition(s));
//                        }catch (Exception ex){
//                            ex.printStackTrace();
//                        }
//                    } else {
//                        Log.d("WordLearner", "Loading from wordnik");
//                        bufferedWriter.write(Utils.saveWord(s, NetworkUtils.GET(Utils.FINAL_URL_SINGLE + s.trim().toLowerCase(Locale.US))));
//                    }
                    bufferedWriter.newLine();
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
//            case R.id.action_gopro:
//                return true;
        }
        return super.onOptionsItemSelected(item);
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
        new ExportWords(path).execute();
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }

}
