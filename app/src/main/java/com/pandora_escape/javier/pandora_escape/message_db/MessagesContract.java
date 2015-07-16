package com.pandora_escape.javier.pandora_escape.message_db;

import android.provider.BaseColumns;

/**
 * Contract of the messages database
 *
 * Created by Javier on 11/07/2015.
 */
public final class MessagesContract implements BaseColumns {

    public static String DATABASE_NAME = "MessagesDB";

    public static String TYPE_TEXT  = " TEXT";
    public static String TYPE_INT   = " INTEGER";

    public static String COLUMN_NAME_CODE       = "code";
    public static String COLUMN_NAME_TITLE      = "title";
    public static String COLUMN_NAME_BODY       = "body";
    public static String COLUMN_NAME_DISC_AT    = "discovered_at";

    public MessagesContract(){}

    public static abstract class MessagesAll implements BaseColumns {
        public static String TABLE_NAME             = "Messages";
        public static String COLUMN_NAME_CODE       = MessagesContract.COLUMN_NAME_CODE;
        public static String COLUMN_TYPE_CODE       = TYPE_TEXT;
        public static String COLUMN_NAME_TITLE      = MessagesContract.COLUMN_NAME_TITLE;
        public static String COLUMN_TYPE_TITLE      = TYPE_TEXT;
        public static String COLUMN_NAME_BODY       = MessagesContract.COLUMN_NAME_BODY;
        public static String COLUMN_TYPE_BODY       = TYPE_TEXT;
        public static String COLUMN_NAME_DISC_AT    = MessagesContract.COLUMN_NAME_DISC_AT;
        public static String COLUMN_TYPE_DISC_AT    = TYPE_INT;
    }

/*    public static abstract class MessagesDiscovered implements BaseColumns {
        public static String TABLE_NAME         = "discovered";
        public static String COLUMN_NAME_CODE   = MessagesContract.COLUMN_NAME_CODE;
    }*/

}
