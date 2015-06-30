package com.pandora_escape.javier.pandora_escape;


import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.support.annotation.Nullable;

/**
 * Created by Javier on 15/06/2015.
 */
public class Message implements Parcelable {

    // Variables
    // Static
    public static String URI_CODE_KEY   = "code";
    //public static String[] codes        = {"One","Two","Three","Four","five"};
    public static Map<String,Integer> CODE_TO_INDEX = new HashMap<>();
/*    static{
        for(int i=0;i<codes.length;i++){
            CODE_TO_INDEX.put(codes[i], i);
        }
    }*/
    public static Map<Integer,String> INDEX_TO_TITLE    = new HashMap<>();
    public static Map<Integer,String> INDEX_TO_BODY     = new HashMap<>();

    // Dynamic
    Integer index;


    // Constructors
    public Message(String code){
        Integer integer = CODE_TO_INDEX.get(code);
        if(integer!=null) {
            this.index = integer;
        }else{
            this.index = null;
        }
    }

    private Message(int index){
        
        
        if (INDEX_TO_TITLE.get(index) != null){
            this.index = index;
        }
        else{
            this.index = null;
        }
    }

    public Message(Parcel in){
        Integer integer = in.readInt();
        if(INDEX_TO_TITLE.get(index) != null){
            this.index = integer;
        } else {
            this.index = null;
        }
    }


    // Functions
    // Static
    public static void initialize(Context context){
        String[] MESSAGE_CODES = context.getResources().getStringArray(R.array.clue_id_array);
        String[] MESSAGE_TITLES = context.getResources().getStringArray(R.array.clue_title_array);
        String[] MESSAGE_BODIES  = context.getResources().getStringArray(R.array.clue_body_array);

        CODE_TO_INDEX = new HashMap<>();
        INDEX_TO_TITLE  = new HashMap<>();
        INDEX_TO_BODY   = new HashMap<>();

        for(int i=0;i< MESSAGE_CODES.length;i++) {
            CODE_TO_INDEX.put(MESSAGE_CODES[i], i);
            INDEX_TO_TITLE.put(i,MESSAGE_TITLES[i]);
            INDEX_TO_BODY.put(i,MESSAGE_BODIES[i]);
        }
    }


    public static ArrayList<Message> getAllClues(){
         Iterator<Integer> allIndexes = CODE_TO_INDEX.values().iterator();

        ArrayList<Message> allMessages = new ArrayList<>();
        while(allIndexes.hasNext()){
            allMessages.add(new Message(allIndexes.next()));
        }

        return allMessages;
    }

    @Nullable
    public static Message createFromIndex(int index){
        if(INDEX_TO_BODY.containsKey(index)){
            return new Message(index);
        }else{
            return null;
        }
    }

    @Nullable
    public static Message parsePandoraUri(Uri uri){
        if(uri==null) {
            return null;
        }

        String code = uri.getQueryParameter(URI_CODE_KEY);
        if(code!=null){
            return new Message(code);
        }

        return null;
    }


    // Dynamic
    @Nullable
    public String getTitle(){
        return INDEX_TO_TITLE.get(index);
    }

    @Nullable
    public String getBody(){
        return INDEX_TO_BODY.get(index);
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Message && ((Message) o).index.equals(this.index);
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


