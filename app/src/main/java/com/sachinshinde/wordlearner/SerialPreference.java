package com.sachinshinde.wordlearner;

/**
 * Created by sachin on 21/10/14.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.IOException;
import java.util.ArrayList;

public class SerialPreference {

    public final static String SHARED_PREFS_FILE = "prefs";
    public final static String TASKS = "words";

    @SuppressWarnings("unchecked")
    public static ArrayList<String> retPrefs(Context mContext) {
        ArrayList<String> currentTasks = null;
        if (null == currentTasks) {
            currentTasks = new ArrayList<String>();
        }

        // load tasks from preference
        SharedPreferences prefs = mContext.getSharedPreferences(
                SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            currentTasks = (ArrayList<String>) ObjectSerializer
                    .deserialize(prefs.getString(TASKS,
                            ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return currentTasks;
    }

    public static void savePrefs(Context mContext, ArrayList<String> currentTasks) {
        assert (null != currentTasks);
        if (null == currentTasks) {
            currentTasks = new ArrayList<String>();
        }
        SharedPreferences prefs = mContext.getSharedPreferences(
                SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        try {
            editor.putString(TASKS, ObjectSerializer.serialize(currentTasks));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

}
