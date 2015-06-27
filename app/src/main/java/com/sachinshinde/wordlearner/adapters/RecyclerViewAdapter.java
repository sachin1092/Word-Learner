package com.sachinshinde.wordlearner.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.daimajia.androidanimations.library.Techniques;
//import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.sachinshinde.wordlearner.R;
import com.sachinshinde.wordlearner.activities.SessionDetailsActivity;
import com.sachinshinde.wordlearner.activities.SessionsListActivity;
import com.sachinshinde.wordlearner.activities.TestWordsActivity;
import com.sachinshinde.wordlearner.module.Session;
import com.sachinshinde.wordlearner.utils.SessionsUtil;
import com.sachinshinde.wordlearner.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        TextView name;
        TextView lastUsed;
        View resume;
        View delete;
        View llShowSessionDetails;
        View edit;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            name = (TextView) itemView.findViewById(R.id.tvSessionName);
            lastUsed = (TextView) itemView.findViewById(R.id.tvSessionLastUsed);
            resume = itemView.findViewById(R.id.ibResumeSession);
            delete = itemView.findViewById(R.id.trash);
            edit = itemView.findViewById(R.id.edit);
            llShowSessionDetails = itemView.findViewById(R.id.llShowSessionDetails);
//
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.d(getClass().getSimpleName(), "onItemSelected: " + textViewData.getText().toString());
//                    Toast.makeText(view.getContext(), "onItemSelected: " + textViewData.getText().toString(), Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    private Context mContext;
    private ArrayList<Session> mDataset;
    private SessionsListActivity sessionsListActivity;

    //protected SwipeItemRecyclerMangerImpl mItemManger = new SwipeItemRecyclerMangerImpl(this);

    public RecyclerViewAdapter(Context context, ArrayList<Session> objects, SessionsListActivity sessionsListActivity) {
        this.mContext = context;
        this.mDataset = objects;
        this.sessionsListActivity = sessionsListActivity;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sessions_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {

        Session item = mDataset.get(position);

        if(item.getSessionName().equalsIgnoreCase("---sep---")){
            viewHolder.swipeLayout.setVisibility(View.GONE);
        } else {
            viewHolder.swipeLayout.setVisibility(View.VISIBLE);
        }

        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
            }
        });
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                undoSession = mDataset.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mDataset.size());
                mItemManger.closeAllItems();
                SessionsUtil.deleteSession(viewHolder.name.getText().toString());
                index = PreferenceManager.getDefaultSharedPreferences(sessionsListActivity).getInt("LastIndex_" + viewHolder.name.getText().toString(), 0);
                PreferenceManager.getDefaultSharedPreferences(sessionsListActivity).edit().remove("LastIndex_" + viewHolder.name.getText().toString()).commit();

                Snackbar sbar = Snackbar.make(view, "Deleted " + viewHolder.name.getText().toString(), Snackbar.LENGTH_LONG);
                sbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDataset.add(position, undoSession);
                        SessionsUtil.saveSession(undoSession);
                        notifyItemInserted(position);
                        notifyItemRangeChanged(position, mDataset.size());
                        mItemManger.closeAllItems();
                    }
                });
                sbar.show();
            }
        });

        viewHolder.edit.setTag(position);

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemManger.closeAllItems();
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(sessionsListActivity);
                View mView = LayoutInflater.from(sessionsListActivity).inflate(R.layout.new_session_page1, null);
                final EditText etNewName = (EditText) mView.findViewById(R.id.etSessionName);
                final Spinner spSortMode = (Spinner) mView.findViewById(R.id.spSessionSortMode);
                final int pos = (Integer) view.getTag();
                etNewName.setText(mDataset.get(pos).getSessionName());
                spSortMode.setSelection(mDataset.get(pos).getSortOrder());
                mBuilder.setView(mView);
                mBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Session session = mDataset.get(pos);
                        SessionsUtil.renameSession(session, etNewName.getText().toString());
                        session.setSessionName(etNewName.getText().toString());
                        session.setSortOrder(spSortMode.getSelectedItemPosition());
                        SessionsUtil.saveSession(session);
                        notifyDataSetChanged();
                    }
                });

                mBuilder.setNegativeButton("CANCEL", null);
                mBuilder.show();

            }
        });

        viewHolder.resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sessionsListActivity, TestWordsActivity.class);
                intent.putExtra(TestWordsActivity.SESSION_NAME, String.valueOf(view.getTag()));
                sessionsListActivity.startActivity(intent);
                sessionsListActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                sessionsListActivity.finish();
            }
        });


        viewHolder.llShowSessionDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sessionsListActivity, SessionDetailsActivity.class);
                intent.putExtra(TestWordsActivity.SESSION_NAME, String.valueOf(view.getTag()));
                sessionsListActivity.startActivity(intent);
                sessionsListActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                sessionsListActivity.finish();
            }
        });

        viewHolder.resume.setTag(item.getSessionName());
        viewHolder.llShowSessionDetails.setTag(item.getSessionName());
        viewHolder.lastUsed.setText(Utils.getTime(item.getLastUsed(), "EEEE, MMM dd, hh:mm a"));
        viewHolder.name.setText(item.getSessionName());
        mItemManger.bindView(viewHolder.itemView, position);
    }

    Session undoSession;
    int index;

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
}