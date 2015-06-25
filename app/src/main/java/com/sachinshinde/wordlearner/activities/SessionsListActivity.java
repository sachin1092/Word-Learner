package com.sachinshinde.wordlearner.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.RecyclerViewAdapter;

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

        final ActionBar ab = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ab.setHomeAsUpIndicator(R.drawable.word_learner);
        ab.setTitle("Sessions List");
//        ab.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lvSessions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Item Decorator:
//        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
//        recyclerView.setItemAnimator(new FadeInLeftAnimator());
        ArrayList<String> sessionsList = new ArrayList<String>();
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("ABC Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("XYZ Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("PQR Session");
        sessionsList.add("---sep---");
//        SessionListAdapter sessionListAdapter = new SessionListAdapter(getBaseContext(), sessionsList);
        RecyclerViewAdapter sessionListAdapter = new RecyclerViewAdapter(getBaseContext(), sessionsList);
        recyclerView.setAdapter(sessionListAdapter);

    }
}
