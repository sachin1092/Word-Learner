package com.sachinshinde.wordlearner.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.RecyclerViewAdapter;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.utils.SessionsUtil;

import java.util.ArrayList;

/**
 * Created by sachin on 25/6/15.
 */
public class SessionsListActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerViewAdapter sessionListAdapter;
    private ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sessions_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Sessions");
        } catch (Exception ex){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lvSessions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Session> sessionsList = SessionsUtil.getSessionList();
        if(sessionsList.size() != 0) {
            Session session = new Session();
            session.setSessionName("---sep---");
            session.setLastUsed(0);
            sessionsList.add(session);
            findViewById(R.id.tvNoSessions).setVisibility(View.GONE);
            findViewById(R.id.lvSessions).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tvNoSessions).setVisibility(View.VISIBLE);
            findViewById(R.id.lvSessions).setVisibility(View.GONE);
        }

        sessionListAdapter = new RecyclerViewAdapter(getBaseContext(), sessionsList, SessionsListActivity.this);
        recyclerView.setAdapter(sessionListAdapter);

        findViewById(R.id.bNewSession).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SessionsListActivity.this, CreateSessionActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE).singleShot(10921)
                .setOnClickListener(this)
                .setStyle(R.style.CustomShowcaseTheme)
                .build();
        showcaseView.setContentTitle("Sessions");
        showcaseView.setContentText("Click on a session to see\n" +
                "TODO and mastered words.\n\n" +
                "Click on the start button to Start Session\n\n" +
                "Swipe left to see more options.");
        showcaseView.setButtonText("Next");

//            AttributeSet attr = apiUtils.
        RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mLayoutParams.setMargins(50, 0, 0, 200);
        showcaseView.setButtonPosition(mLayoutParams);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(SessionsListActivity.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(SessionsListActivity.this, MainActivity.class));
        super.onBackPressed();
    }

    public static final String INTENT_REFRESH = "com.sachinshinde.WORD_LEARNER.REFRESH_SESSIONS";
    public static final String INTENT_REFRESH_VIEW = "com.sachinshinde.WORD_LEARNER.REFRESH_SESSIONS_VIEW";

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_REFRESH);
        intentFilter.addAction(INTENT_REFRESH_VIEW);
        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            unregisterReceiver(mBroadcastReceiver);
        }catch (Exception ex){}
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(INTENT_REFRESH)){
                ArrayList<Session> sessionsList = SessionsUtil.getSessionList();
                sessionListAdapter.setDataList(sessionsList);
                sessionListAdapter.notifyDataSetChanged();
            }

            if(sessionListAdapter.getItemCount() == 1){
                findViewById(R.id.tvNoSessions).setVisibility(View.VISIBLE);
                findViewById(R.id.lvSessions).setVisibility(View.GONE);
            } else {
                findViewById(R.id.tvNoSessions).setVisibility(View.GONE);
                findViewById(R.id.lvSessions).setVisibility(View.VISIBLE);
            }

        }
    };

    private int counter = 0;
    @Override
    public void onClick(View view) {
        switch (counter) {
            case 0:
                showcaseView.setShowcase(new ViewTarget(findViewById(R.id.bNewSession)), true);
                showcaseView.setContentTitle("Create Sessions");
                showcaseView.setContentText("Click here to Create new Session.");
                showcaseView.setButtonText("GOT IT");
                break;

            case 1:
                showcaseView.hide();
                break;
        }
        counter++;
    }
}
