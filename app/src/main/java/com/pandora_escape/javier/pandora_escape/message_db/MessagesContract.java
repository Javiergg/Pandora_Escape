package com.pandora_escape.javier.pandora_escape.message_db;

import android.provider.BaseColumns;

/**
 * Contract of the messages_raw database
 *
 * Created by Javier on 11/07/2015.
 */
public final class MessagesContract implements BaseColumns {

    public static final String DATABASE_NAME = "MessagesDB";

    public static final String TYPE_TEXT  = " TEXT";
    public static final String TYPE_INT   = " INTEGER";

    public static final String COLUMN_NAME_LANG       = "language";
    public static final String COLUMN_NAME_CODE       = "code";
    public static final String COLUMN_NAME_TITLE      = "title";
    public static final String COLUMN_NAME_BODY       = "body";
    public static final String COLUMN_NAME_DISC_AT    = "discovered_at";

    public MessagesContract(){}

    public static abstract class MessagesAll implements BaseColumns {
        public static final String TABLE_NAME             = "Messages";
        public static final String COLUMN_NAME_LANG       = MessagesContract.COLUMN_NAME_LANG;
        public static final String COLUMN_TYPE_LANG       = TYPE_TEXT;
        public static final String COLUMN_NAME_CODE       = MessagesContract.COLUMN_NAME_CODE;
        public static final String COLUMN_TYPE_CODE       = TYPE_TEXT;
        public static final String COLUMN_NAME_TITLE      = MessagesContract.COLUMN_NAME_TITLE;
        public static final String COLUMN_TYPE_TITLE      = TYPE_TEXT;
        public static final String COLUMN_NAME_BODY       = MessagesContract.COLUMN_NAME_BODY;
        public static final String COLUMN_TYPE_BODY       = TYPE_TEXT;
        public static final String COLUMN_NAME_DISC_AT    = MessagesContract.COLUMN_NAME_DISC_AT;
        public static final String COLUMN_TYPE_DISC_AT    = TYPE_INT;
    }

/*    public static abstract class MessagesDiscovered implements BaseColumns {
        public static String TABLE_NAME         = "discovered";
        public static String COLUMN_NAME_CODE   = MessagesContract.COLUMN_NAME_CODE;
    }*/

}
