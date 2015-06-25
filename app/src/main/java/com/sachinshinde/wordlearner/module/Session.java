package com.sachinshinde.wordlearner.module;

import java.util.ArrayList;

/**
 * Created by sachin on 25/6/15.
 */
public class Session {

    private String sessionName;
    private ArrayList<String> mastered;
    private ArrayList<String> toRevise;
    private long lastUsed;
    private int sortOrder;
    public static final int ASCENDING = 0;
    public static final int DECENDING = 1;
    public static final int RANDOM = 2;

    public Session() {

    }

    public Session(String sessionName, ArrayList<String> mastered, ArrayList<String> toRevise, long lastUsed, int sortOrder) {

        this.sessionName = sessionName;
        this.mastered = mastered;
        this.toRevise = toRevise;
        this.lastUsed = lastUsed;
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public ArrayList<String> getMastered() {
        if (mastered != null)
            return mastered;
        return new ArrayList<>();
    }

    public void setMastered(ArrayList<String> mastered) {
        this.mastered = mastered;
    }

    public ArrayList<String> getToRevise() {
        if(toRevise != null)
            return toRevise;
        return new ArrayList<>();
    }

    public void setToRevise(ArrayList<String> toRevise) {
        this.toRevise = toRevise;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }


}
