package com.sachinshinde.wordlearner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        findViewById(R.id.buttonAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyActivity.this, AddWords.class));
            }
        });

        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
                builder.setTitle("Select Session");
                builder.setAdapter(new ArrayAdapter<String>(MyActivity.this, android.R.layout.select_dialog_item, new String[] {"New session", "Resume old session"}), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            startActivity(new Intent(MyActivity.this, TestWords.class).putExtra("file", Utils.WordsFile));
                        } else {
                            startActivity(new Intent(MyActivity.this, TestWords.class).putExtra("file", Utils.SessionFile));
                        }
                    }
                });
                File mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner/" + Utils.SessionFile);
                if(mFile.exists())
                    builder.show();
                else
                    startActivity(new Intent(MyActivity.this, TestWords.class).putExtra("file", Utils.WordsFile));
            }
        });

        File mDir = new File(Environment.getExternalStorageDirectory().getPath() + "/WordLearner");
        File mFile = new File(mDir.getPath() + "/" + Utils.WordsFile);
        if(!mFile.exists()){
            ArrayList<String> list = SerialPreference.retPrefs(getBaseContext());
            if(list != null){
                Utils.writeListToFile(list, Utils.WordsFile);
            }
        }

    }
}
