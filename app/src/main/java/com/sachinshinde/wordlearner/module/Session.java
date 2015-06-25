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

    public Session(){

    }

    public Session(String sessionName, ArrayList<String> mastered, ArrayList<String> toRevise, long lastUsed){

        this.sessionName = sessionName;
        this.mastered = mastered;
        this.toRevise = toRevise;
        this.lastUsed = lastUsed;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public ArrayList<String> getMastered() {
        return mastered;
    }

    public void setMastered(ArrayList<String> mastered) {
        this.mastered = mastered;
    }

    public ArrayList<String> getToRevise() {
        return toRevise;
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
