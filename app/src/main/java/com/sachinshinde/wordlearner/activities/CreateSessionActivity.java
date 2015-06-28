package com.sachinshinde.wordlearner.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.fragments.CreateSessionFragment1;
import com.sachinshinde.wordlearner.fragments.CreateSessionFragment2;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.utils.SessionsUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateSessionActivity extends AppCompatActivity {

    private CreateSessionFragment1 frag1;
    private CreateSessionFragment2 frag2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setTitle("Create Session");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        findViewById(R.id.bCreateSessionNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (frag1.getEditText() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(frag1.getEditText().getWindowToken(), 0);
                }
                if (SessionsUtil.checkIfSessionNameExist(frag1.getSessionName())) {
                    Toast.makeText(CreateSessionActivity.this, "Session name already exists.", Toast.LENGTH_SHORT).show();
                } else if (viewPager != null)
                    if (viewPager.getCurrentItem() == 0) {
                        if (frag1.getSessionName().isEmpty()) {
                            Toast.makeText(CreateSessionActivity.this, "Session Name can't be empty.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        viewPager.setCurrentItem(1, true);
                    } else {
                        if (frag2.getTODOList().size() == 0) {
                            Toast.makeText(CreateSessionActivity.this, "Select at least a word.", Toast.LENGTH_SHORT).show();
                        } else {
                            Session session = new Session();
                            session.setToRevise(frag2.getTODOList());
                            session.setMastered(new ArrayList<String>());
                            session.setSessionName(frag1.getSessionName());
                            session.setSortOrder(frag1.getSortMode());
                            SessionsUtil.saveSession(session);

                            sendBroadcast(new Intent(SessionsListActivity.INTENT_REFRESH));

                            Intent intent = new Intent(CreateSessionActivity.this, TestWordsActivity.class);
                            intent.putExtra(TestWordsActivity.SESSION_NAME, session.getSessionName());
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                            finish();
                        }
                    }
            }
        });

        findViewById(R.id.bCreateSessionPrev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager != null)
                    viewPager.setCurrentItem(0, true);
            }
        });

        findViewById(R.id.bCreateSessionPrev).setEnabled(false);

    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        frag1 = CreateSessionFragment1.newInstance();
        frag2 = CreateSessionFragment2.newInstance();
        adapter.addFragment(frag1, "TODO");
        adapter.addFragment(frag2, "Mastered");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((Button) findViewById(R.id.bCreateSessionNext)).setText(position == 0 ? "NEXT" : "CREATE");
                findViewById(R.id.bCreateSessionPrev).setEnabled(position == 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(CreateSessionActivity.this, SessionsListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(new Intent(CreateSessionActivity.this, SessionsListActivity.class));
        super.onBackPressed();
    }

}
