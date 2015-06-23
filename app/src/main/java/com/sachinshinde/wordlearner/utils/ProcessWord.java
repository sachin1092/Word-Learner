package com.sachinshinde.wordlearner.utils;

import com.sachinshinde.wordlearner.module.Meaning;
import com.sachinshinde.wordlearner.module.Word;
import com.sachinshinde.wordlearner.module.SubWords;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sachin on 23/6/15.
 */
public class ProcessWord {

    public static Word getDefinition(String json) throws JSONException {

        JSONObject data = new JSONObject(json);

        return getDefinition(data);
    }

    public static Word getDefinition(JSONObject data) throws JSONException {

        Word word = new Word();

        ArrayList<SubWords> subWordsList = new ArrayList<>();

        if(data.has("definitionData")) {

            JSONArray jsonArray = data.getJSONArray("definitionData");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int j = 0; j < len; j++) {

                    JSONObject obj = jsonArray.getJSONObject(j);

                    SubWords subWords = new SubWords();
                    subWords.setMeanings(obj.has("meanings") ? getMeaning(obj.getJSONArray("meanings")) : null);
                    subWords.setWord(obj.has("word")?obj.getString("word"):null);
                    subWords.setType(obj.has("pos") ? obj.getString("pos") : null);
                    JSONArray wordForm = obj.has("wordForms") ? obj.getJSONArray("wordForms") : null;
                    String form = "";
                    if(wordForm != null)
                        for (int i = 0; i < wordForm.length(); i++) {
                            form += wordForm.getJSONObject(i).getString("form") + ": " + wordForm.getJSONObject(i).getString("word") + "; ";
                        }
                    subWords.setForm(form);

                    subWordsList.add(subWords);
                }
            }

            word.setWords(subWordsList);

        }
        return word;
    }

    private static ArrayList<Meaning> getMeaning(JSONArray meanings) throws JSONException {

        ArrayList<Meaning> meaningsList = new ArrayList<>();

        for(int i = 0 ; i < meanings.length() ; i++) {
            JSONObject obj = meanings.getJSONObject(i);
            ArrayList<String> synonyms = (obj.has("synonyms")) ? getArrayListFromJSON(obj.getJSONArray("synonyms")) : null;
            ArrayList<String> antonyms = (obj.has("antonyms")) ?getArrayListFromJSON(obj.getJSONArray("antonyms")) : null;
            ArrayList<String> examples = (obj.has("examples")) ?getExampleListFromJSON(obj.getJSONArray("examples")) : null;

            Meaning meaning = new Meaning();
            meaning.setAntonyms(antonyms);
            meaning.setSynonyms(synonyms);
            meaning.setExamples(examples);
            meaning.setMeaning(((obj.has("meaning")) ? obj.getString("meaning") : null));
            if(obj.has("submeanings"))
                meaning.setSubMeaning(getMeaning(obj.getJSONArray("submeanings")));

            meaningsList.add(meaning);
        }

        return meaningsList;
    }

    private static ArrayList<String> getArrayListFromJSON(JSONArray jsonArray) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int j=0;j<len;j++) {
                if(jsonArray.getJSONObject(j).has("nym"))
                    list.add(jsonArray.getJSONObject(j).getString("nym"));
            }
        }
        return list;
    }

    private static ArrayList<String> getExampleListFromJSON(JSONArray jsonArray) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int j=0;j<len;j++) {
                list.add(jsonArray.getString(j));
            }
        }
        return list;
    }

}
