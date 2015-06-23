package com.sachinshinde.wordlearner.module;

import java.util.ArrayList;

/**
 * Created by sachin on 23/6/15.
 */
public class Meaning {

    private String meaning;
    private ArrayList<String> synonyms;
    private ArrayList<String> antonyms;
    private ArrayList<String> examples;
    private ArrayList<Meaning> subMeaning;

    public Meaning(){

    }

    public Meaning(String meaning, ArrayList<String> synonyms, ArrayList<String> antonyms, ArrayList<String> examples, ArrayList<Meaning> subMeaning) {
        this.meaning = meaning;
        this.synonyms = synonyms;
        this.antonyms = antonyms;
        this.examples = examples;
        this.subMeaning = subMeaning;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public ArrayList<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(ArrayList<String> synonyms) {
        this.synonyms = synonyms;
    }

    public ArrayList<String> getAntonyms() {
        return antonyms;
    }

    public void setAntonyms(ArrayList<String> antonyms) {
        this.antonyms = antonyms;
    }

    public ArrayList<String> getExamples() {
        return examples;
    }

    public void setExamples(ArrayList<String> examples) {
        this.examples = examples;
    }

    public ArrayList<Meaning> getSubMeaning() {
        return subMeaning;
    }

    public void setSubMeaning(ArrayList<Meaning> subMeaning) {
        this.subMeaning = subMeaning;
    }
}
