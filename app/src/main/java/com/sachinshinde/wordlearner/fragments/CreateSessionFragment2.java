package com.sachinshinde.wordlearner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.ListAdapter;
import com.sachinshinde.wordlearner.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by sachin on 26/6/15.
 */
public class CreateSessionFragment2 extends Fragment{

    public static CreateSessionFragment2 newInstance() {
        return new CreateSessionFragment2();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CreateSessionFragment2(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    ListView listView;
    View view;
    ArrayList<String> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.new_session_page2, container, false);

        mList = Utils.loadListFromFile(Utils.WordsFile);
        final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, mList);


        listView = (ListView)view.findViewById(R.id.lvWords);

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setAdapter(mAdapter);

        ((EditText)view.findViewById(R.id.etSearceWords)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mAdapter.getFilter().filter(((EditText)view.findViewById(R.id.etSearceWords)).getText().toString());
                if (((EditText)view.findViewById(R.id.etSearceWords)).getText().length() > 0) {
                    view.findViewById(R.id.ivAddWords).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.ivAddWords).setVisibility(View.GONE);
                }
            }
        });

        view.findViewById(R.id.ivAddWords).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText)view.findViewById(R.id.etSearceWords)).setText("");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                ((TextView)view.findViewById(R.id.tvCount)).setText(String.format("Selected Count: %d words", listView.getCheckedItemCount()));
                selectedItems.add(mList.get(i));
            }
        });


        setHasOptionsMenu(true);

        return view;
    }

    ArrayList<String> selectedItems = new ArrayList<>();

    public ArrayList<String> getTODOList(){
        return selectedItems;
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_session_actions, menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_select_all:
                for(int i = 0 ; i < listView.getCount() ; i++)
                    listView.setItemChecked(i, true);
                ((TextView)view.findViewById(R.id.tvCount)).setText(String.format("Selected Count: %d words", listView.getCheckedItemCount()));
                selectedItems.addAll(mList);
                return true;
            case R.id.action_select_none:
                for(int i = 0 ; i < listView.getCount() ; i++)
                    listView.setItemChecked(i, false);
                ((TextView)view.findViewById(R.id.tvCount)).setText(String.format("Selected Count: %d words", listView.getCheckedItemCount()));
                selectedItems.clear();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
