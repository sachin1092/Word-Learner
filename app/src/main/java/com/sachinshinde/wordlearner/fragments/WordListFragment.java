package com.sachinshinde.wordlearner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.adapters.ListAdapter;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class WordListFragment extends Fragment implements AbsListView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    private OnFragmentInteractionListener mListener;
    private OnFragmentLongPressedInteractionListener mLongListener;
    public ArrayList<String> mList;
    int type;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    public static WordListFragment newInstance(ArrayList<String> mList, int type) {
        WordListFragment fragment = new WordListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("list", mList);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WordListFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mList = getArguments().getStringArrayList("list");
            type = getArguments().getInt("type", 0);
        }

//        mAdapter = new ArrayAdapter<>(getActivity(),
//                android.R.layout.simple_list_item_1, android.R.id.text1, mList);
//        mAdapter = new ListAdapter(getActivity(), mList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_string, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
//        (mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setFastScrollAlwaysVisible(true);

        setHasOptionsMenu(true);

        return view;
    }

    public ListAdapter getAdapter(){
        return mAdapter;
    }

    public void setAdapter(ArrayList<String> mList){
        this.mList = mList;
        mAdapter = new ListAdapter(getActivity(), this.mList);
        (mListView).setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mLongListener = (OnFragmentLongPressedInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mLongListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(mList.get(position));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(null != mLongListener){
            return mLongListener.onFragmentLongPressInteraction(view, mList.get(i), type);
        }
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public interface OnFragmentLongPressedInteractionListener {
        // TODO: Update argument type and name
        public boolean onFragmentLongPressInteraction(View view, String word, int type);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter = new ListAdapter(getActivity(), mList);
        (mListView).setAdapter(mAdapter);
    }


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.start_session, menu);
//    }
//
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        menu.findItem(R.id.action_start_session).setTitle("Start Session of " + (type == 0 ? "TODO" : "Mastered") + " Words");
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()){
//            case R.id.action_start_session:
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }


}
