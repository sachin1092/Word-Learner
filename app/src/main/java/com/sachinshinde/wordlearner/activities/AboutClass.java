package com.sachinshinde.wordlearner.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.sachinshinde.wordlearner.utils.ChangeLog;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.utils.Utils;


public class AboutClass extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getSupportActionBar().hide();
        try {
            getSupportActionBar().hide();
        } catch (Exception ex) {
        }

        setContentView(R.layout.about);

        ImageView iv = (ImageView) findViewById(R.id.ivAboutRate);
        iv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Utils.launchMarket(getBaseContext(), getPackageName());

            }
        });
        Typeface tf = Typeface.createFromAsset(getAssets(), "cnlbold.ttf");
        TextView tv1 = (TextView) findViewById(R.id.tvAbout1);
        tv1.setTypeface(tf);
        TextView tv2 = (TextView) findViewById(R.id.tvAbout2);
        TextView tv5 = (TextView) findViewById(R.id.tvAboutName);
        tv2.setTypeface(tf);
        tv5.setTypeface(tf);
        setlinked((TextView) findViewById(R.id.tvAboutSendFeedback),
                "Send Feedback");
        findViewById(R.id.tvAboutSendFeedback)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        sendMail("Feedback - Word Learner");
                    }
                });

        findViewById(R.id.tvAboutReportProblem)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        sendMail("Reporting bug - Word Learner");
                    }
                });
        ((TextView) findViewById(R.id.tvAboutSendFeedback)).setTypeface(tf);
        setlinked((TextView) findViewById(R.id.tvAboutReportProblem),
                "Report a problem");
        ((TextView) findViewById(R.id.tvAboutReportProblem)).setTypeface(tf);
        ((TextView) findViewById(R.id.tvAboutCurVersion)).setTypeface(tf);
        ((TextView) findViewById(R.id.tvAboutTellus)).setTypeface(tf);
        ((TextView) findViewById(R.id.tvAboutLetusKnow)).setTypeface(tf);
        ((TextView) findViewById(R.id.tvAboutCurVersion)).setText("Version "
                + getVersion());

        setlinked((TextView) findViewById(R.id.tvAboutchangelog), "ChangeLog");
        ((TextView) findViewById(R.id.tvAboutchangelog)).setTypeface(tf);
        ((TextView) findViewById(R.id.tvAboutchangelog))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ChangeLog cl = new ChangeLog(AboutClass.this);

                        cl.getFullLogDialog().show();

                    }
                });

    }

    public void sendMail(String string) {
        String emailaddress[] = {"me@sachinshinde.com"};
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailaddress);

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, string);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent From:\n"

                + "Manufacturer: " + android.os.Build.MANUFACTURER + "\n" + "Model: "
                + android.os.Build.MODEL

                + "\nAndroid Version: " + Build.VERSION.RELEASE
                + "\nApplication Version: " + getLibraryVersion() + "\n\n");
        emailIntent.setType("plain/text");
        startActivity(emailIntent);
    }

    public String getLibraryVersion() {
        PackageManager manager = getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionCode + " ";
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getVersion() {
        PackageManager manager = getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.1";

    }

    private void setlinked(TextView textView, String sContent) {
        SpannableString content = new SpannableString(sContent);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);
    }

}
