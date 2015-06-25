package com.sachinshinde.wordlearner.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.RecyclerViewAdapter;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.utils.SessionsUtil;

import java.util.ArrayList;

/**
 * Created by sachin on 25/6/15.
 */
public class SessionsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sessions_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Sessions List");
        } catch (Exception ex){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lvSessions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Session> sessionsList = SessionsUtil.getSessionList();
        if(sessionsList.size() == 0) {
            Session session = new Session();
            session.setSessionName("---sep---");
            session.setLastUsed(0);
            sessionsList.add(session);
        }
        RecyclerViewAdapter sessionListAdapter = new RecyclerViewAdapter(getBaseContext(), sessionsList);
        recyclerView.setAdapter(sessionListAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        super.onBackPressed();
    }
}
