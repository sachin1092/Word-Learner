package com.sachinshinde.wordlearner.utils;

import android.content.Intent;
import android.os.Environment;

import com.sachinshinde.wordlearner.module.Session;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final String SORT_FILE = "sort";
    private static final String SESSIONS_PATH = Environment.getExternalStorageDirectory().getPath() + "/WordLearner/Sessions";

    public static void saveSession(Session session) {

        try {
            ArrayList<String> masteredList = session.getMastered();
            ArrayList<String> toRevise = session.getToRevise();
            String path = SESSIONS_PATH + "/" + session.getSessionName();
            saveList(masteredList, path, MASTERED_FILE);
            saveList(toRevise, path, TO_REVISE);
            saveText(session.getSortOrder() + "", path, SORT_FILE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static boolean renameSession(Session session, String newName){
        String path = SESSIONS_PATH + "/" + session.getSessionName();
        File mDir = new File(path);
        return mDir.renameTo(new File(SESSIONS_PATH + "/" + newName));
    }

    public static boolean checkIfSessionNameExist(String sessionName){
        File mDir = new File(SESSIONS_PATH);
        for(File f : mDir.listFiles()){
            if(f.getName().equals(sessionName))
                return true;
        }
        return false;
    }

    public static int getSessionCount(){
        File mDir = new File(SESSIONS_PATH);
        if(!mDir.exists())
            return 0;
        return mDir.listFiles().length;

    }

    public static String[] getSessionNames(){
        File mDir = new File(SESSIONS_PATH);
        if(!mDir.exists())
            return new String[0];
        return mDir.list();
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
            try {
                session.setSortOrder(Integer.parseInt(loadText(path, SORT_FILE)));
            }catch (Exception ex){
                ex.printStackTrace();
                session.setSortOrder(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return session;
    }

    public static ArrayList<Session> getSessionList() {
        File mDir = new File(SESSIONS_PATH);
        try {
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
        } catch (Exception ex) {
            return new ArrayList<>();
        }


    }


    private static void saveList(ArrayList<String> list, String path, String fileName) {
        Set<String> set = new TreeSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);

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
        return list;
    }

    private static void saveText(String text, String path, String fileName) {
        try {
            File mFile = new File(path + "/" + fileName);
            FileWriter fileWriter =
                    new FileWriter(mFile);

            BufferedWriter bufferedWriter =
                    new BufferedWriter(fileWriter);

            bufferedWriter.write(text);
            bufferedWriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String loadText(String path, String fileName) {
        String finalString = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path + "/" + fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                finalString += line;
            }
            reader.close();
            return finalString;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "0";
    }

    public static void deleteSession(Session session) {
        deleteSession(session.getSessionName());
    }

    public static void deleteSession(String sessionName) {
        File directory = new File(SESSIONS_PATH + "/" + sessionName);
        if (directory.exists()) {
            try {
                Utils.deleteFile(directory);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteAllSessions(){
        String[] sessions = getSessionNames();
        for(int i = 0 ; i < sessions.length ; i++){
            deleteSession(sessions[i]);
        }
    }

}
