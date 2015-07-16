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

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    // Create table for all messages
    public static final String SQL_CREATE_ENTRIES_ALL = "CREATE TABLE " +
            MessagesContract.MessagesAll.TABLE_NAME + " (" +
            MessagesContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MessagesContract.MessagesAll.COLUMN_NAME_CODE +
                MessagesContract.MessagesAll.COLUMN_TYPE_CODE + " NOT NULL UNIQUE," +
            MessagesContract.MessagesAll.COLUMN_NAME_TITLE +
                MessagesContract.MessagesAll.COLUMN_TYPE_TITLE + " NOT NULL," +
            MessagesContract.MessagesAll.COLUMN_NAME_BODY +
                MessagesContract.MessagesAll.COLUMN_TYPE_BODY + "," +
            MessagesContract.MessagesAll.COLUMN_NAME_DISC_AT +
                MessagesContract.MessagesAll.COLUMN_TYPE_DISC_AT +
            ")";


    // Static variables
//    private static Context sContext = null;
    private static MessagesDBHelper sInstance = null;
    private static String sLocale = null;


    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     */
    private MessagesDBHelper(Context context) {
        super(context, MessagesContract.DATABASE_NAME, null, DATABASE_VERSION);
        sLocale = null;
    }


    public static synchronized MessagesDBHelper getInstance(Context context){
 //       sContext = context;
        if(sInstance ==null){                              // If helper not created yet
            sInstance = new MessagesDBHelper(context);    // makes a new one
        }
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
    public synchronized void initialize(Context context,String locale){
        SQLiteDatabase db = getWritableDatabase();     // Get the database

        // If the locale is the same no need to initialize
        if(sLocale!=null && sLocale.equals(locale)) { return; }

        // If the db wasn't initialized or the locales don't match, populate the db

        // Check if the database is empty
        Cursor c = db.query(MessagesContract.MessagesAll.TABLE_NAME,null,null,null,null,null,null);
        boolean dbEmpty = !(c.getCount()>0);
        c.close();
        //db.delete(MessagesContract.MessagesAll.TABLE_NAME,null,null);

        // Fetch values from application resources
        String[] MESSAGE_CODES = context.getResources().getStringArray(R.array.clue_id_array);
        String[] MESSAGE_TITLES = context.getResources().getStringArray(R.array.clue_title_array);
        String[] MESSAGE_BODIES  = context.getResources().getStringArray(R.array.clue_body_array);
        // Populate the All Messages table
        for(int i=0;i<MESSAGE_CODES.length;i++) {
            // Create new entry from each set of items from resources
            ContentValues values = new ContentValues();
            values.put(MessagesContract.MessagesAll.COLUMN_NAME_TITLE,MESSAGE_TITLES[i]);
            values.put(MessagesContract.MessagesAll.COLUMN_NAME_BODY, MESSAGE_BODIES[i]);
            if(dbEmpty) {   // If the db is empty,
                // add the code field
                values.put(MessagesContract.MessagesAll.COLUMN_NAME_CODE,MESSAGE_CODES[i]);
                // and insert the new entries
                db.insert(MessagesContract.MessagesAll.TABLE_NAME, null, values);
            }else{  // If the db is already populated
                // Update the values
                db.update(MessagesContract.MessagesAll.TABLE_NAME,
                        values,
                        MessagesContract.MessagesAll.COLUMN_NAME_CODE + " = ? ",
                        new String[]{MESSAGE_CODES[i]});
            }
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
        String whereClause  = MessagesContract.COLUMN_NAME_CODE + " = ? AND " +
                                MessagesContract.COLUMN_NAME_DISC_AT + " IS NULL";
        String[] whereArgs  = new String[]{code};
        // Update the db
        db.update(MessagesContract.MessagesAll.TABLE_NAME,
                values,
                whereClause,
                whereArgs);
    }


    /**
     * Sets all messages in the table as discovered
     *
     */
    public void addAllMessages(){
        SQLiteDatabase db = getWritableDatabase();

        // Set link new values to columns
        ContentValues values = new ContentValues();
        values.put(MessagesContract.COLUMN_NAME_DISC_AT, System.currentTimeMillis());
        // Update the db
        db.update(MessagesContract.MessagesAll.TABLE_NAME,
                values,
                null,   // Apply to all entries
                null);
    }

    /**
     * Sets all messages in the table as discovered
     *
     */
    public void removeAllMessages(){
        SQLiteDatabase db = getWritableDatabase();

        // Set link new values to columns
        ContentValues values = new ContentValues();
        values.putNull(MessagesContract.COLUMN_NAME_DISC_AT);
        // Update the db
        db.update(MessagesContract.MessagesAll.TABLE_NAME,
                values,
                null,   // Apply to all rows
                null);
    }

    /**
     * Provides a cursor with all the messages discovered so far, ordered form oldest to newest
     *
     * @return Cursor with messages
     */
    public Cursor getDiscoveredMessages(){
        // Get the db
        SQLiteDatabase db = getReadableDatabase();
        // Fill the cursor with the Titles and Bodies of the discovered messages
        String projection[] = new String[]{MessagesContract._ID,
                                MessagesContract.COLUMN_NAME_TITLE,
                                MessagesContract.COLUMN_NAME_BODY};
        // Select only entries where discovered_at is not null, i.e. have been discovered
        String selection        = MessagesContract.COLUMN_NAME_DISC_AT + " IS NOT NULL";
        // Sort them from oldest to newest
        String sortOrder = MessagesContract.COLUMN_NAME_DISC_AT + " ASC";
        // Make query with previous parameters
        return db.query(MessagesContract.MessagesAll.TABLE_NAME,
                                    projection,
                                    selection,
                                    null,
                                    null,
                                    null,
                                    sortOrder);
    }


    /**
     * Generate a Message object containing the Title and Body linked to the identifying code
     *
     * @param code Code that identifies the message
     * @return  Message containing the Title and Body
     */
    public static Message getMessage(String code){
        // If the helper is not initialized yet, return null
        if(sInstance==null||code==null||code.equals("")){ return null; }

        // If not get the database to get the
        SQLiteDatabase db = sInstance.getReadableDatabase();

        // Set link new values to columns
        String columns[] = new String[]{MessagesContract.COLUMN_NAME_TITLE,
                                        MessagesContract.COLUMN_NAME_BODY};
        // Set the WHERE arguments
        String whereClause  = MessagesContract.COLUMN_NAME_CODE + " = ?";
        String[] whereArgs  = new String[]{code};
        // Update the db

        try (Cursor cursor = db.query(MessagesContract.MessagesAll.TABLE_NAME,
                columns,
                whereClause,
                whereArgs,
                null,
                null,
                null)) {

            cursor.moveToFirst();
            int indexTitle = cursor.getColumnIndex(MessagesContract.COLUMN_NAME_TITLE);
            int indexBody = cursor.getColumnIndex(MessagesContract.COLUMN_NAME_BODY);

            return new Message(code,cursor.getString(indexTitle), cursor.getString(indexBody));
        } catch (Exception e) {
            return null;
        }

    }

}
