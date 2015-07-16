package com.pandora_escape.javier.pandora_escape.message_db;


import android.support.annotation.Nullable;

/**
 * Message is an object that contains the Title and Body strings
 *
 * Created by Javier on 15/06/2015.
 */
public class Message {
    // Variables
    private String mCode;
    private String mTitle;
    private String mBody;


    // Constructors
    public Message(String code,String title, String body){
        this.mCode  = code;
        this.mTitle = title;
        this.mBody  = body;
    }


    // Functions
    // Dynamic
    @Nullable
    public String getCode(){
        return mCode;
    }

    @Nullable
    public String getTitle(){
        return mTitle;
    }

    @Nullable
    public String getBody(){
        return mBody;
    }


    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Message)){ return false; }

        String oTitle = ((Message) o).getTitle();

        return mTitle==null ? (oTitle==null) : mTitle.equals(oTitle);
    }

    @Override
    public String toString(){
        return getTitle();
    }
}


