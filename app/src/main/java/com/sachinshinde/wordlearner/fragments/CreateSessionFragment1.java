package com.sachinshinde.wordlearner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.ListAdapter;
import com.sachinshinde.wordlearner.utils.Utils;

import java.util.ArrayList;

/**
 * Created by sachin on 26/6/15.
 */
public class CreateSessionFragment1 extends Fragment{

    public static CreateSessionFragment1 newInstance() {
        CreateSessionFragment1 fragment = new CreateSessionFragment1();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CreateSessionFragment1(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.new_session_page1, container, false);

        ((EditText)view.findViewById(R.id.etSessionName)).setText("Session_" + Utils.getTime(System.currentTimeMillis(), "EE_MM_dd_HH_mm"));

        return view;
    }

    public String getSessionName(){
        return ((EditText)view.findViewById(R.id.etSessionName)).getText().toString();
    }

    public int getSortMode(){
        return ((Spinner)view.findViewById(R.id.spSessionSortMode)).getSelectedItemPosition();
    }


}
