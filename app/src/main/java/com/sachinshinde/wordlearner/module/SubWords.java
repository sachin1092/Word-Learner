package com.sachinshinde.wordlearner.module;

import java.util.ArrayList;

/**
 * Created by sachin on 23/6/15.
 */
public class SubWords {

    private String form;
    private String word;
    private String type;
    private ArrayList<Meaning> meanings;

    public SubWords(String form, String word, String type, ArrayList<Meaning> meanings) {
        this.form = form;
        this.word = word;
        this.type = type;
        this.meanings = meanings;
    }

    public SubWords(){

    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<Meaning> meanings) {
        this.meanings = meanings;
    }

}
