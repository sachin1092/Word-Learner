package com.sachinshinde.wordlearner;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sachinshinde.wordlearner.module.Meaning;
import com.sachinshinde.wordlearner.module.SubWords;
import com.sachinshinde.wordlearner.module.Word;
import com.sachinshinde.wordlearner.utils.ProcessWord;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sachin on 21/10/14.
 */
public class Utils {

    public static final String WordsFile = "WordList";
    public static final String SessionFile = "Session";
    public static final String FINAL_URL_SINGLE = "http://my-dictionary-api.appspot.com/getMeaning?word=";
    public static final String FINAL_URL_MULTIPLE = "http://my-dictionary-api.appspot.com/getMultiMeaning";
    public static final String INTENT_ADD_WORD = "action.wordlearner.ADDWORD";
    public static final String INTENT_DELETE_WORD = "action.wordlearner.DELETEWORD";


    public static void writeListToFile(ArrayList<String> list, String FileName) {

        Set<String> set = new TreeSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);

//        for(int i = 0 ; i < list.size() ; i++){
//            String item = list.get(i).toLowerCase(Locale.US);
//            list.remove(i);
//            list.add(item);
//
//        }

        if (null == FileName)
            throw new RuntimeException("FileName is null!");

        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner");
        if(!mDir.exists()){
            mDir.mkdirs();
        }

        File mFile = new File(mDir.getPath() + "/" + FileName);
        try {
            if (mFile.exists() || mFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(mFile);//mContext.openFileOutput(FileName, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(list);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> loadListFromFile(String FileName) {
        if (null == FileName)
            return null;
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner");
        if(!mDir.exists()){
            mDir.mkdirs();
        }
        ArrayList<String> list = new ArrayList<String>();
        File mFile = new File(mDir.getPath() + "/" + FileName);
        try {
            if (mFile.exists()) {
                FileInputStream fis = new FileInputStream(mFile);//mContext.openFileInput(FileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                list = (ArrayList<String>) ois.readObject();
                fis.close();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Set<String> set = new TreeSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    public static String jsonToWordNikString(String jsonString) {
        String string = "";
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                string = string + (i + 1) + ". " + jsonArray.getJSONObject(i).getString("text") + "\n";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("WordLearner", string);
        return string;
    }

    public static final String URL1 = "http://api.wordnik.com/v4/word.json/";
    public static final String URL2 = "/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key="+Constants.API_KEY;

    public static boolean hasWord(String word) {
        word = word.trim().toLowerCase(Locale.getDefault());
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Words");
        if(!mDir.exists()){
            mDir.mkdirs();
        }
        File mFile = new File(mDir.getPath() + "/" + word.trim().toLowerCase(Locale.getDefault()));
        return mFile.exists();
    }

    public static boolean deleteWord(String word) {
        word = word.trim().toLowerCase(Locale.getDefault());
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Words");
        if(!mDir.exists()){
            mDir.mkdirs();
        }
        File mFile = new File(mDir.getPath() + "/" + word.trim().toLowerCase(Locale.getDefault()));
        return mFile.delete();
    }

    public static String getDefinition(String word) {
        word = word.trim().toLowerCase(Locale.getDefault());
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Words");
        if(!mDir.exists()){
            mDir.mkdirs();
        }
        File mFile = new File(mDir.getPath() + "/" + word);
        String definition = "";
        try {
            if (mFile.exists()) {
                FileInputStream fis = new FileInputStream(mFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                definition = (String) ois.readObject();
                fis.close();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }

    public static String getWordJSON(String word) {
        word = word.trim().toLowerCase(Locale.getDefault());
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Words");
        if(!mDir.exists()){
            mDir.mkdirs();
        }
        File mFile = new File(mDir.getPath() + "/" + word);
        String definition = "";
        try {
            if (mFile.exists()) {
                FileInputStream fis = new FileInputStream(mFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                definition = (String) ois.readObject();
                fis.close();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }

    public static String saveWord(String word, String definition) {
        word = word.trim().toLowerCase(Locale.getDefault());
        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Words");
        if(!mDir.exists()){
            mDir.mkdirs();
        }
        File mFile = new File(mDir.getPath() + "/" + word);
        try {
            if (mFile.exists() || mFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(mFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(definition);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }

    public static View getMeaningsView(String definition, Activity mContext){

        try {
            Word word = ProcessWord.getDefinition(definition);

            View mainView = LayoutInflater.from(mContext).inflate(R.layout.meaning_dialog, null);
            LinearLayout wordsContainer = (LinearLayout) mainView.findViewById(R.id.wordsContainer);

            ArrayList<SubWords> subWords = word.getWords();

            for (int x = 0 ; x < subWords.size() ; x++) {

                SubWords subWord = subWords.get(x);

                View meaningsViewContainer = LayoutInflater.from(mContext).inflate(R.layout.meaning_container, null);

                ((TextView) meaningsViewContainer.findViewById(R.id.tvWord)).setText(Html.fromHtml(subWord.getWord()));
                ((TextView) meaningsViewContainer.findViewById(R.id.tvWordType)).setText(Html.fromHtml(subWord.getType()));
                ((TextView) meaningsViewContainer.findViewById(R.id.tvWordForm)).setText(Html.fromHtml(subWord.getForm()));

                LinearLayout meaningsContainer = (LinearLayout) meaningsViewContainer.findViewById(R.id.meaningsContainer);

                ArrayList<Meaning> meanings = subWord.getMeanings();
                for (int i = 0; i < meanings.size(); i++) {
                    Meaning meaning = meanings.get(i);
                    View meaningItemView = LayoutInflater.from(mContext).inflate(R.layout.meaning_item, null);

                    ((TextView) meaningItemView.findViewById(R.id.tvWordIndex)).setText((i + 1) + ".");
                    ((TextView) meaningItemView.findViewById(R.id.tvWordMeaning)).setText(Html.fromHtml(meaning.getMeaning() != null ? meaning.getMeaning() : ""));

                    if (meaning.getExamples() != null)
                        ((TextView) meaningItemView.findViewById(R.id.tvWordSentence)).setText(Html.fromHtml(meaning.getExamples().toString().replace("[", "").replace("]", "")));
                    else
                        ((TextView) meaningItemView.findViewById(R.id.tvWordSentence)).setText("");

                    if (meaning.getSynonyms() != null)
                        setText(mContext, (TextView) meaningItemView.findViewById(R.id.tvSynonyms),Html.fromHtml(meaning.getSynonyms().toString().replace("[", "").replace("]", "")), true);
                    else
                        meaningItemView.findViewById(R.id.synonymsContainer).setVisibility(View.GONE);

                    if (meaning.getAntonyms() != null)
                        setText(mContext, (TextView) meaningItemView.findViewById(R.id.tvAntonyms),Html.fromHtml(meaning.getAntonyms().toString().replace("[", "").replace("]", "")), true);
                    else
                        meaningItemView.findViewById(R.id.antonymsContainer).setVisibility(View.GONE);

                    meaningsContainer.addView(meaningItemView);
                }


                if (x == subWords.size() - 1) {
                    meaningsViewContainer.findViewById(R.id.divider).setVisibility(View.GONE);
                }

                wordsContainer.addView(meaningsViewContainer);
            }

            return mainView;


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error Utils", e.getMessage());
        }

        return null;

    }


    public static View getProgressView(Activity activity){
        final View view = LayoutInflater.from(activity).inflate(
                R.layout.progress_dialog, null);
        View img1 = view.findViewById(R.id.pd_circle1);
        View img2 = view.findViewById(R.id.pd_circle2);
        View img3 = view.findViewById(R.id.pd_circle3);
        int ANIMATION_DURATION = 400;
        Animator anim1 = setRepeatableAnim(activity, img1, ANIMATION_DURATION, R.animator.growndisappear);
        Animator anim2 = setRepeatableAnim(activity, img2, ANIMATION_DURATION, R.animator.growndisappear);
        Animator anim3 = setRepeatableAnim(activity, img3, ANIMATION_DURATION, R.animator.growndisappear);
        setListeners(img1, anim1, anim2, ANIMATION_DURATION);
        setListeners(img2, anim2, anim3, ANIMATION_DURATION);
        setListeners(img3, anim3, anim1, ANIMATION_DURATION);
        anim1.start();
        return view;
    }

    public static int dpToPx(int i, Context mContext) {

        DisplayMetrics displayMetrics = mContext.getResources()
                .getDisplayMetrics();
        return (int) ((i * displayMetrics.density) + 0.5);

    }

    private static Animator setRepeatableAnim(Activity activity, View target, final int duration, int animRes){
        final Animator anim = AnimatorInflater.loadAnimator(activity, animRes);
        anim.setDuration(duration);
        anim.setTarget(target);
        return anim;
    }

    private static void setListeners(final View target, Animator anim, final Animator animator, final int duration){
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animat) {
                if(target.getVisibility() == View.INVISIBLE){
                    target.setVisibility(View.VISIBLE);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                }, duration - 100);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public static class ClickableURLSpan extends URLSpan {

        private TextToSpeech ttobj;

        public ClickableURLSpan(String url) {
            super(url);
        }

        Activity mActivity;

        public ClickableURLSpan(Activity mActivity, String url) {
            super(url);
            this.mActivity = mActivity;
            ttobj = new TextToSpeech(mActivity, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                }
            });
            ttobj.setLanguage(Locale.US);

            ttobj.setSpeechRate(0.8f);
        }

        android.support.v7.app.AlertDialog dialog;



        @Override
        public void onClick(View widget) {
            final String clickedText = getURL().trim().toLowerCase(Locale.getDefault());
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

            builder.setView(Utils.getProgressView(mActivity));

            final boolean hasWord = Utils.hasWord(clickedText);

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



                    View mView = Utils.getMeaningsView(s, mActivity);
                    if (mView != null) {



                        View buttonBar = LayoutInflater.from(mActivity).inflate(R.layout.button_bar, null);
                        FrameLayout fl = (FrameLayout) buttonBar.findViewById(R.id.flButtonBarContainer);
                        fl.addView(mView);

                        builder.setView(buttonBar);

                        if(hasWord){
                            builder.setCancelable(true);
                            ((ImageButton) buttonBar.findViewById(R.id.bCancelorDelete)).setImageResource(R.drawable.ic_delete);
                        } else {
                            builder.setCancelable(false);
                            ((ImageButton) buttonBar.findViewById(R.id.bDone)).setImageResource(R.drawable.ic_plus);
                        }

                        final AlertDialog mDialog = builder.create();

                        buttonBar.findViewById(R.id.bEdit).setVisibility(View.GONE);

                        buttonBar.findViewById(R.id.bSpeak).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                    ttobj.speak(clickedText, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
                                } else {
                                    ttobj.speak(clickedText, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });



                        buttonBar.findViewById(R.id.bCancelorDelete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(hasWord) {
                                    Intent intent = new Intent();
                                    intent.setAction(INTENT_DELETE_WORD);
                                    intent.putExtra("WORD", clickedText);
                                    mActivity.sendBroadcast(intent);
                                }
                                Utils.deleteWord(clickedText);
                                mDialog.cancel();
                            }
                        });



                        buttonBar.findViewById(R.id.bDone).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(!hasWord) {
                                    Intent intent = new Intent();
                                    intent.setAction(INTENT_ADD_WORD);
                                    intent.putExtra("WORD", clickedText);
                                    mActivity.sendBroadcast(intent);
                                }
                                mDialog.cancel();
                            }
                        });

                        mDialog.show();

                    }
                    else {
                        Toast.makeText(mActivity, "Oops! An error occurred. Try Again.", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        Utils.deleteWord(clickedText);
                        return;
                    }

                    dialog.cancel();
                    super.onPostExecute(s);
                }
            }.execute(clickedText);
            dialog = builder.create();
            dialog.show();
        }
    }

    public static void setText(Activity mActivity, TextView tv, Spanned input, boolean linkify) {

        if (linkify) {
            SpannableStringBuilder builder = new SpannableStringBuilder(input);

            Pattern pattern = Pattern.compile("[a-zA-Z]+");
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                String text = input.subSequence(start, end).toString();

                ClickableURLSpan url = new ClickableURLSpan(mActivity, text);
                builder.setSpan(url, start, end, 0);
            }

            tv.setText(builder);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static void launchMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        myAppLinkToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("http://play.google.com/store/apps/details?id="
                            + packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}
