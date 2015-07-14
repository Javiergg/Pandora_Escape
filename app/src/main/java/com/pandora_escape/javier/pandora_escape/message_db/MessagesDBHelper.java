package com.pandora_escape.javier.pandora_escape.message_db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pandora_escape.javier.pandora_escape.R;

/**
 * Helper to handle the messages database
 *
 * Created by Javier on 11/07/2015.
 */
public class MessagesDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MessagesDB";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    // Create table for all messages
    public static final String SQL_CREATE_ENTRIES_ALL = "CREATE TABLE " +
            MessagesContract.MessagesAll.TABLE_NAME + "(" +
            MessagesContract.MessagesAll.COLUMN_NAME_CODE +
                MessagesContract.MessagesAll.COLUMN_TYPE_CODE + " NOT NULL UNIQUE," +
            MessagesContract.MessagesAll.COLUMN_NAME_TITLE +
                MessagesContract.MessagesAll.COLUMN_TYPE_TITLE + " NOT NULL," +
            MessagesContract.MessagesAll.COLUMN_NAME_BODY +
                MessagesContract.MessagesAll.COLUMN_TYPE_BODY + "," +
            MessagesContract.MessagesAll.COLUMN_NAME_BODY +
                MessagesContract.MessagesAll.COLUMN_TYPE_BODY + "," +
            MessagesContract.MessagesAll.COLUMN_NAME_DISC_AT +
                MessagesContract.MessagesAll.COLUMN_TYPE_DISC_AT +
            ")";
    // Code to create discovered
/*    public static final String SQL_CREATE_ENTRIES_DISCOVERED = "CREATE TABLE " +
            MessagesContract.MessagesAll.TABLE_NAME + "(" +
            MessagesContract.MessagesAll.COLUMN_NAME_TITLE + TEXT_TYPE + " NOT NULL," +
            MessagesContract.MessagesAll.COLUMN_NAME_BODY + TEXT_TYPE +
            ")";*/


    // Static variables
    private static MessagesDBHelper sInstance = null;
    private static String sLocale = null;


    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     */
    private MessagesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sLocale = null;
    }


    public static synchronized MessagesDBHelper getInstance(Context context){
        if(sInstance ==null){                              // If helper not created yet
            sInstance = new MessagesDBHelper(context);    // makes a new one
        }
        sInstance.initialize(context);    // Populate the db
        return sInstance;
    }


    // Functions

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_ALL);
        //db.execSQL(SQL_CREATE_ENTRIES_DISCOVERED);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE + MessagesContract.MessagesAll.TABLE_NAME);
        //db.execSQL(DROP_TABLE + MessagesContract.MessagesDiscovered.TABLE_NAME);

        onCreate(db);
    }

    /**
     * Populate database with original values
     *
     * @param context   The context to get the message strings resources from
     */
    private synchronized void initialize(Context context){
        SQLiteDatabase db = getWritableDatabase();     // Get the database

        if(sLocale!=null) { // If the db has been initialized
            if (sLocale.equals(context.getString(R.string.locale))) {
                            // And the locale matches
                return;     // no need to initialize
            } else {                                                  // If the locale doesn't match
                this.onUpgrade(db,DATABASE_VERSION,DATABASE_VERSION); // Rebuild the database
            }
        } // If the db wasn't initialized or the locales don't match, populate the db

        // Fetch values from application resources
        String[] MESSAGE_CODES = context.getResources().getStringArray(R.array.clue_id_array);
        String[] MESSAGE_TITLES = context.getResources().getStringArray(R.array.clue_title_array);
        String[] MESSAGE_BODIES  = context.getResources().getStringArray(R.array.clue_body_array);
        // Populate the All Messages table
        for(int i=0;i<MESSAGE_CODES.length;i++) {
            // Create new entry from each set of items from resources
            ContentValues values = new ContentValues();
            values.put(MessagesContract.MessagesAll.COLUMN_NAME_CODE,MESSAGE_CODES[i]);
            values.put(MessagesContract.MessagesAll.COLUMN_NAME_TITLE,MESSAGE_TITLES[i]);
            values.put(MessagesContract.MessagesAll.COLUMN_NAME_BODY, MESSAGE_BODIES[i]);
            // Insert the new entry
            db.insert(MessagesContract.MessagesAll.TABLE_NAME,null,values);
        }

        // Update current database locale
        sLocale = context.getString(R.string.locale);
    }

    /**
     *  When a new message is discovered, the "discovered_at" value of that message entry is
     *  updated to the time of discovery (these keeps list in chronological order).
     *
     * @param code Identifier to locate the message entry
     */
    public void addDiscoveredMessage(String code){

        SQLiteDatabase db = getWritableDatabase();

        // Set link new values to columns
        ContentValues values = new ContentValues();
        values.put(MessagesContract.COLUMN_NAME_DISC_AT, System.currentTimeMillis());
        // Set the WHERE arguments
        String whereClause  = "?=\"?\" AND ? IS NOT NULL";
        String[] whereArgs  = {MessagesContract.COLUMN_NAME_CODE,
                                code,
                                MessagesContract.COLUMN_NAME_DISC_AT};
        // Update the db
        db.update(MessagesContract.MessagesAll.TABLE_NAME,
                values,
                whereClause,
                whereArgs);
        // Close the db after you are done
        db.close();
    }


    public Cursor getDiscoveredMessages(){
        // Get the db
        SQLiteDatabase db = getReadableDatabase();
        // Fill the cursor with the Titles and Bodies of the discovered messages
        String projection[] = new String[]{MessagesContract.COLUMN_NAME_TITLE,
                                MessagesContract.COLUMN_NAME_BODY};
        // Select only entries where discovered_at is not null, i.e. have been discovered
        String selection        = "? IS NOT NULL";
        String[] selectionArgs  = new String[]{MessagesContract.COLUMN_NAME_DISC_AT};
        // Sort them from oldest to newest
        String sortOrder = MessagesContract.COLUMN_NAME_DISC_AT + " ASC";
        // Make query with previous parameters
        Cursor cursor = db.query(MessagesContract.MessagesAll.TABLE_NAME,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    null,
                                    null,
                                    sortOrder);
        // Once the query is done, close the db
        db.close();

        return cursor;
    }


}
