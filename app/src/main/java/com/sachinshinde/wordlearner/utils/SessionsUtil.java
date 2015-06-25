package com.sachinshinde.wordlearner.utils;

import android.os.Environment;

import com.sachinshinde.wordlearner.module.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by sachin on 25/6/15.
 */
public class SessionsUtil {

    private static final String MASTERED_FILE = "mastered";
    private static final String TO_REVISE = "to_revise";
    private static final String SESSIONS_PATH = Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Sessions";

    public static void saveSession(Session session) {

        try {
            ArrayList<String> masteredList = session.getMastered();
            ArrayList<String> toRevise = session.getToRevise();
            String path = SESSIONS_PATH + "/" + session.getSessionName();
            saveList(masteredList, path, MASTERED_FILE);
            saveList(toRevise, path, TO_REVISE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static Session getSession(String sessionName) {
        Session session = new Session();
        try {
            String path = SESSIONS_PATH + "/" + sessionName;
            ArrayList<String> masteredList = loadList(path, MASTERED_FILE);
            ArrayList<String> toRevise = loadList(path, TO_REVISE);

            File mFile = new File(path + "/" + TO_REVISE);

            session.setMastered(masteredList);
            session.setToRevise(toRevise);
            session.setSessionName(sessionName);
            session.setLastUsed(mFile.lastModified());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return session;
    }

    public static ArrayList<Session> getSessionList() {
        File mDir = new File(SESSIONS_PATH);
        if (!mDir.exists()) {
            mDir.mkdirs();
            return new ArrayList<>();
        }
        ArrayList<Session> sessionsList = new ArrayList<>();

        File[] dirList = mDir.listFiles();
        for (int i = 0; i < dirList.length; i++) {
            sessionsList.add(getSession(dirList[i].getName()));
        }

        return sessionsList;
    }

    private static void saveList(ArrayList<String> list, String path, String fileName) {
        Set<String> set = new TreeSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);

        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i).toLowerCase(Locale.US);
            list.remove(i);
            if (!item.isEmpty())
                list.add(item);

        }


        if (null == fileName)
            throw new RuntimeException("FileName is null!");

        File mDir = new File(path);
        if (!mDir.exists()) {
            mDir.mkdirs();
        }

        File mFile = new File(path + "/" + fileName);
        try {
            if (mFile.exists() || mFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(mFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(list);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> loadList(String path, String fileName) {
        if (null == fileName)
            return null;
        File mDir = new File(path);
        if (!mDir.exists()) {
            mDir.mkdirs();
        }
        ArrayList<String> list = new ArrayList<String>();
        File mFile = new File(path + "/" + fileName);
        try {
            if (mFile.exists()) {
                FileInputStream fis = new FileInputStream(mFile);//mContext.openFileInput(FileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                list = (ArrayList<String>) ois.readObject();
                fis.close();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Set<String> set = new TreeSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);

        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i).toLowerCase(Locale.US);
            list.remove(i);
            if (!item.isEmpty())
                list.add(item);

        }
        return list;
    }

}
