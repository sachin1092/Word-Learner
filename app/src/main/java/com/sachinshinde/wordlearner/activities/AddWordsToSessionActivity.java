package com.sachinshinde.wordlearner.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.utils.SessionsUtil;
import com.sachinshinde.wordlearner.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class AddWordsToSessionActivity extends AppCompatActivity {

    private ArrayList<String> mList;
    private ListView listView;
    private Session currentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words_to_session);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        setSupportActionBar(toolbar);
        try {
            final ActionBar ab = getSupportActionBar();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ab.setHomeAsUpIndicator(R.drawable.word_learner);
            ab.setTitle("Add Words");
//        ab.setDisplayHomeAsUpEnabled(true);
        } catch (Exception ex) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mList = Utils.loadListFromFile(Utils.WordsFile);

        String sessionName = getIntent().getStringExtra(TestWordsActivity.SESSION_NAME);
        currentSession = SessionsUtil.getSession(sessionName);

        mList.removeAll(currentSession.getMastered());
        mList.removeAll(currentSession.getToRevise());

        if(mList.size() == 0){
            findViewById(R.id.tvNoWords).setVisibility(View.VISIBLE);
            findViewById(R.id.wordListContainer).setVisibility(View.GONE);
        } else {
            findViewById(R.id.tvNoWords).setVisibility(View.GONE);
            findViewById(R.id.wordListContainer).setVisibility(View.VISIBLE);
        }

        Collections.sort(mList);

        final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_list_item_multiple_choice, mList);


        listView = (ListView) findViewById(R.id.lvWords);

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setAdapter(mAdapter);

        ((EditText) findViewById(R.id.etSearceWords)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mAdapter.getFilter().filter(((EditText) findViewById(R.id.etSearceWords)).getText().toString());
                if (((EditText) findViewById(R.id.etSearceWords)).getText().length() > 0) {
                    findViewById(R.id.ivAddWords).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.ivAddWords).setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.ivAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) findViewById(R.id.etSearceWords)).setText("");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                ((TextView) findViewById(R.id.tvCount)).setText(String.format("Selected Count: %d words", listView.getCheckedItemCount()));
                if(selectedItems.contains(mList.get(i)))
                    selectedItems.remove(mList.get(i));
                else
                    selectedItems.add(mList.get(i));
            }
        });

        findViewById(R.id.bDoneAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedItems.addAll(currentSession.getToRevise());
                currentSession.setToRevise(selectedItems);
                SessionsUtil.saveSession(currentSession);
                finish();
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
            }
        });
    }

    ArrayList<String> selectedItems = new ArrayList<>();


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_session_actions, menu);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mList.size() == 0){
            menu.findItem(R.id.action_select_all).setVisible(false);
            menu.findItem(R.id.action_select_none).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                return true;
            case R.id.action_select_all:
                for(int i = 0 ; i < listView.getCount() ; i++)
                    listView.setItemChecked(i, true);
                ((TextView) findViewById(R.id.tvCount)).setText(String.format("Selected Count: %d words", listView.getCheckedItemCount()));
                selectedItems.addAll(mList);
                return true;
            case R.id.action_select_none:
                for(int i = 0 ; i < listView.getCount() ; i++)
                    listView.setItemChecked(i, false);
                ((TextView) findViewById(R.id.tvCount)).setText(String.format("Selected Count: %d words", listView.getCheckedItemCount()));
                selectedItems.clear();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
