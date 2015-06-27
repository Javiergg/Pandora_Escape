package com.pandora_escape.javier.qrescape;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Javier on 15/06/2015.
 */
public class Clue implements Parcelable {

    // Variables
    public static Map<String,Integer> QR_TO_INDEX = new HashMap<>();
    public static Map<Integer,String> INDEX_TO_TITLE   = new HashMap<>();
    public static Map<Integer,String> INDEX_TO_BODY    = new HashMap<>();

    int index;


    // Constructors
    public Clue(String code){
        this.index = QR_TO_INDEX.get(code);
    }

    public Clue(int index){
        this.index = index;
    }

    public Clue(Parcel in){
        index = in.readInt();
    }


    // Functions
    // Static
    public static void initialize(Context context){
        String[] CLUE_CODES = context.getResources().getStringArray(R.array.clue_id_array);
        String[] CLUE_TITLES = context.getResources().getStringArray(R.array.clue_title_array);
        String[] CLUE_BODIES  = context.getResources().getStringArray(R.array.clue_body_array);

        QR_TO_INDEX     = new HashMap<>();
        INDEX_TO_TITLE  = new HashMap<>();
        INDEX_TO_BODY   = new HashMap<>();

        for(int i=0;i< CLUE_CODES.length;i++) {
            QR_TO_INDEX.put(CLUE_CODES[i], i);
            INDEX_TO_TITLE.put(i,CLUE_TITLES[i]);
            INDEX_TO_BODY.put(i,CLUE_BODIES[i]);
        }
    }

    public static ArrayList<Clue> getAllClues(){
         Iterator<Integer> allIndexes = QR_TO_INDEX.values().iterator();

        ArrayList<Clue> allClues = new ArrayList<>();
        for(int i=0;allIndexes.hasNext();i++){
            allClues.add(new Clue(allIndexes.next()));
        }

        return allClues;
    }


    // Dynamic
    public String getTitle(){
        return INDEX_TO_TITLE.get(index);
    }

    public String getBody(){
        return INDEX_TO_BODY.get(index);
    }


    @Override
    public boolean equals(Object o){
        if(o instanceof Clue) {
            return ((Clue) o).index == this.index;
        }else{
            return false;
        }
    }

    @Override
    public String toString(){
        return getTitle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
    }
}


