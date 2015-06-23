package com.sachinshinde.wordlearner.module;

import java.util.ArrayList;

/**
 * Created by sachin on 23/6/15.
 */
public class Word {

    private ArrayList<SubWords> words;
//    private String word;

    public Word(){

    }

    public Word(ArrayList<SubWords> words) {
        this.words = words;
//        this.word = word;
    }

    public ArrayList<SubWords> getWords() {
        return words;
    }

    public void setWords(ArrayList<SubWords> words) {
        this.words = words;
    }

//    public String getWord() {
//        return word;
//    }
//
//    public void setWord(String word) {
//        this.word = word;
//    }
}
