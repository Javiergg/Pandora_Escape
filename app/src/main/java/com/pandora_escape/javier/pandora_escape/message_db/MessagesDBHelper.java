package com.pandora_escape.javier.pandora_escape.message_db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pandora_escape.javier.pandora_escape.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Helper to handle the messages_raw database
 *
 * Created by Javier on 11/07/2015.
 */
public class MessagesDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    // Create table for all messages_raw
    public static final String SQL_CREATE_ENTRIES_ALL = "CREATE TABLE " +
            MessagesContract.MessagesAll.TABLE_NAME + " (" +
            MessagesContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MessagesContract.MessagesAll.COLUMN_NAME_LANG +
                MessagesContract.MessagesAll.COLUMN_TYPE_LANG + " NOT NULL, " +
            MessagesContract.MessagesAll.COLUMN_NAME_CODE +
                MessagesContract.MessagesAll.COLUMN_TYPE_CODE + " NOT NULL, " +
            MessagesContract.MessagesAll.COLUMN_NAME_TITLE +
                MessagesContract.MessagesAll.COLUMN_TYPE_TITLE + " NOT NULL, " +
            MessagesContract.MessagesAll.COLUMN_NAME_BODY +
                MessagesContract.MessagesAll.COLUMN_TYPE_BODY + ", " +
            MessagesContract.MessagesAll.COLUMN_NAME_DISC_AT +
                MessagesContract.MessagesAll.COLUMN_TYPE_DISC_AT + ", " +
            "UNIQUE (" + MessagesContract.COLUMN_NAME_LANG + "," +
                            MessagesContract.COLUMN_NAME_CODE + ") ON CONFLICT REPLACE" +
            ");";

    // Logging values
    public static final String TESTING_LOG = "XML_Testing.";


    // Static variables
//    private static Context sContext = null;
    private static MessagesDBHelper sInstance = null;
    // Only used to initialize the DB
    private static Context sContext = null;
    private static SQLiteDatabase sInitializationDB = null;
    private static boolean sCreating = false;


    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     */
    private MessagesDBHelper(Context context) {
        super(context, MessagesContract.DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized MessagesDBHelper getInstance(Context context){
        sContext = context;
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
        sCreating = true;

        db.execSQL(SQL_CREATE_ENTRIES_ALL);

        // Populate database from XML file
        sInitializationDB = db;
        XmlResourceParser xrp = sContext.getResources().getXml(R.xml.messages);

        ParserXMLToDB parserXMLToDB = new ParserXMLToDB(this);
        try {
            parserXMLToDB.parseXML(xrp);
        } catch (XmlPullParserException e) {
            Log.e(TESTING_LOG,"Problem with XML structure.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TESTING_LOG,"Problem reading XML file.");
            e.printStackTrace();
        }

        sInitializationDB = null;
        sCreating = false;
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


    protected void insertMessage(String language, Message message){
        Log.d(TESTING_LOG, "Trying to insert Message: Lang = " + language +
                " Code = " + message.getCode() +
                " Title = " + message.getTitle() +
                " Body = " + message.getBody() + ".");

        SQLiteDatabase db;
        if(sCreating) {
            if (sInitializationDB == null) {
                Log.e(TESTING_LOG, "Database was null.");
                return;
            } else if (!sInitializationDB.isOpen()) {
                Log.e(TESTING_LOG, "Database was not open.");
                return;
            } else {
                db = sInitializationDB;
            }
        }else{
            db = getWritableDatabase();
        }

        // Insert the message into the database
        ContentValues values = new ContentValues();
        values.put(MessagesContract.COLUMN_NAME_LANG,language);
        values.put(MessagesContract.COLUMN_NAME_CODE,message.getCode());
        values.put(MessagesContract.COLUMN_NAME_TITLE,message.getTitle());
        values.put(MessagesContract.COLUMN_NAME_BODY, message.getBody());
        // and insert the new entries
        db.insert(MessagesContract.MessagesAll.TABLE_NAME, null, values);
        Log.d(TESTING_LOG, "Insert successful");
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
     * Sets all messages_raw in the table as discovered
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
     * Sets all messages_raw in the table as discovered
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
     * Provides a cursor with all the messages_raw discovered so far, ordered form oldest to newest
     *
     * @return Cursor with messages_raw
     */
    public Cursor getDiscoveredMessages(){
        // Get the db
        SQLiteDatabase db = getReadableDatabase();
        // Fill the cursor with the Titles and Bodies of the discovered messages_raw
        String projection[] = new String[]{MessagesContract._ID,
                                MessagesContract.COLUMN_NAME_TITLE,
                                MessagesContract.COLUMN_NAME_BODY};
        // Select only entries where discovered_at is not null, i.e. have been discovered
        // and match the current locale
        String selection = MessagesContract.COLUMN_NAME_LANG + " = ? AND " +
                MessagesContract.COLUMN_NAME_DISC_AT + " IS NOT NULL";
        String selectionArgs[] = new String[]{sContext.getString(R.string.language)};
        // Sort them from oldest to newest
        String sortOrder = MessagesContract.COLUMN_NAME_DISC_AT + " ASC";
        // Make query with previous parameters
        return db.query(MessagesContract.MessagesAll.TABLE_NAME,
                                    projection,
                                    selection,
                                    selectionArgs,
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
        String whereClause = MessagesContract.COLUMN_NAME_LANG + " = ? AND " +
                MessagesContract.COLUMN_NAME_CODE + " = ?";
        String[] whereArgs = new String[]{sContext.getString(R.string.language),code};

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
