package com.sachinshinde.wordlearner;

/**
 * Created by sachin on 21/10/14.
 */

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ListAdapter extends ArrayAdapter<String> implements SectionIndexer {

    private static String sections = "abcdefghilmnopqrstuvwxyz".toUpperCase(Locale.getDefault());
    HashMap<String, Integer> mapIndex;
    //    String[] sections;
    List<String> fruits;
    ArrayList<String> mList;

    public ListAdapter(Context context, List<String> fruitList) {
        super(context, R.layout.list_item, fruitList);

        this.fruits = fruitList;
        this.mList = new ArrayList<String>();
        this.mList.addAll(this.fruits);
        mapIndex = new LinkedHashMap<String, Integer>();

        for (int x = 0; x < fruits.size(); x++) {
            try {
                String fruit = fruits.get(x);
                String ch = fruit.substring(0, 1);
                ch = ch.toUpperCase(Locale.US);

                // HashMap will prevent duplicates
                mapIndex.put(ch, x);
            } catch (Exception ex){}
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Log.d("sectionList", sectionList.toString());
        Collections.sort(sectionList);

//        sections = new String[sectionList.size()];

//        sectionList.toArray(sections);
    }


    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        fruits.clear();
        if (charText.length() == 0) {
            fruits.addAll(mList);
        } else {
            for (String wp : mList) {
                if (wp.toLowerCase(Locale.getDefault()).contains(charText)) {
                    fruits.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<String> getList(){
        return this.mList;
    }


    public void addToList(String object) {
        mList.add(object);
        Collections.sort(mList);
    }

    //    public Object[] getSections() {
//        return sections;
//    }
//
//    public int getPositionForSection(int section) {
//        Log.d("section", "" + section);
//        return mapIndex.get(sections[section]);
//    }
//
//    public int getSectionForPosition(int position) {
//        Log.d("position", "" + position);
//        return 0;
//    }

    @Override
    public int getPositionForSection(int section) {
        Log.d("ListView", "Get position for section");
        try {
            for (int i = 0; i < this.getCount(); i++) {
                String item = this.getItem(i).toUpperCase(Locale.getDefault());
                if (item.charAt(0) == sections.charAt(section))
                    return i;
            }
        }catch (Exception ex){}
        return 0;
    }


    @Override
    public int getSectionForPosition(int arg0) {
        Log.d("ListView", "Get section");
        return 0;
    }

    @Override
    public Object[] getSections() {
        Log.d("ListView", "Get sections");
        String[] sectionsArr = new String[sections.length()];
        for (int i = 0; i < sections.length(); i++)
            sectionsArr[i] = "" + sections.charAt(i);

        return sectionsArr;

    }
}