package com.blubb.alubb.basics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Local database to store BlubbMessages and BlubbThreads to provide the possibility to
 * read messages even offline.
 * Created by Benjamin Richter on 30.05.2014.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    /**
     * Name of the class for logging purposes.
     */
    public static final String N = "DatabaseHandler";
    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 2;
    /**
     * Database name
     */
    private static final String DATABASE_NAME = "blubbDB";
    /**
     * messages table name
     */
    private static final String TABLE_MESSAGES = "messages";
    /**
     * threads table name
     */
    private static final String TABLE_THREADS = "threads";

    /**
     * Column names for messages
     */
    private static final String
            /** Id of the message. */
            M_ID = "mId",
    /**
     * Optional title.
     */
    M_TITLE = "mTitle",
    /**
     * Content.
     */
    M_CONTENT = "mContent",
    /**
     * Role of the user who created this message.
     */
    M_ROLE = "mRole",
    /**
     * Id of the creator of a message.
     */
    M_CREATOR = "mCreator",
    /**
     * Date when this message was created at the BeapDB.
     */
    M_DATE = "mDate",
    /**
     * Type of message.
     */
    M_TYPE = "mType",
    /**
     * Id of the thread within which the message has been created.
     */
    M_THREAD_ID = "mThreadId",
    /**
     * A link to another message, if not null the message is a reply.
     */
    M_LINK = "mLink",
    /**
     * True if the user has not yet seen this message.
     */
    M_IS_NEW = "mIsNew";

    /**
     * Column names for threads.
     */
    private static final String
            T_ID = "tId",
            T_TITLE = "tTitle",
            T_DESC = "tDescription",
            T_CREATOR = "tCreator",
            T_C_ROLE = "tCreatorRole",
            T_STATUS = "tStatus",
            T_DATE = "tDate",
            T_MSG_COUNT = "tMsgCount",
            T_TYPE = "tType",
            T_IS_NEW = "tIsNew",
            T_HAS_NEW_M = "tHasNewMsg";

    /**
     * Constructor for the DatabaseHandler.
     *
     * @param context within which the Sqlite database will be created.
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Deletes the hole database.
     *
     * @param context within which the Sqlite database is created and will be deleted.
     */
    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.v(N, "onCreate(sqlitedb)");
        // create the message table
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + M_ID + " TEXT PRIMARY KEY,"
                + M_TITLE + " TEXT,"
                + M_CONTENT + " TEXT,"
                + M_ROLE + " TEXT,"
                + M_CREATOR + " TEXT,"
                + M_DATE + " TEXT,"
                + M_TYPE + " TEXT,"
                + M_THREAD_ID + " TEXT,"
                + M_LINK + " TEXT,"
                + M_IS_NEW + " INTEGER)";
        Log.i("SQLiteDB", "Creating Message table with sql: \n" + CREATE_MESSAGES_TABLE);
        sqLiteDatabase.execSQL(CREATE_MESSAGES_TABLE);
        // create the threads table
        String CREATE_THREAD_TABLE = "CREATE TABLE " + TABLE_THREADS + "("
                + T_ID + " TEXT PRIMARY KEY,"
                + T_TITLE + " TEXT,"
                + T_DESC + " TEXT,"
                + T_CREATOR + " TEXT,"
                + T_C_ROLE + " TEXT,"
                + T_STATUS + " TEXT,"
                + T_DATE + " TEXT,"
                + T_MSG_COUNT + " INTEGER,"
                + T_TYPE + " TEXT,"
                + T_IS_NEW + " INTEGER,"
                + T_HAS_NEW_M + " INTEGER)";
        Log.i("SQLiteDB", "Creating thread table with sql: \n" + CREATE_THREAD_TABLE);
        sqLiteDatabase.execSQL(CREATE_THREAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        Log.i(N, "onUprade(sqLiteDB = " + sqLiteDatabase.getVersion() + ")");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_THREADS);
        onCreate(sqLiteDatabase);
    }

    /**
     * Add a single new BlubbMessage to the Sqlite database. The message id must be unique!
     *
     * @param message which will be stored in the database.
     */
    public void addMessage(BlubbMessage message) {
        Log.v(N, "addMessage(message = " + message.getmId() + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_ID, message.getmId());
        values.put(M_TITLE, message.getmTitle());
        values.put(M_CONTENT, message.getmContent().getStringRepresentation());
        values.put(M_ROLE, message.getmCreatorRole());
        values.put(M_CREATOR, message.getmCreator());
        values.put(M_DATE, message.getmDate());
        values.put(M_TYPE, message.getmType());
        values.put(M_THREAD_ID, message.getmThread());
        values.put(M_LINK, message.getmLink());
        int flag = (message.isNew()) ? 1 : 0;
        values.put(M_IS_NEW, flag);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    /**
     * Adds a single BlubbThread to the Sqlite database. The thread id must be unique!
     *
     * @param thread which will be stored in the database.
     */
    public void addThread(BlubbThread thread) {
        Log.v(N, "addThread(thread = " + thread.gettId() + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(T_ID, thread.gettId());
        values.put(T_TITLE, thread.getThreadTitle());
        values.put(T_DESC, thread.gettDesc());
        values.put(T_CREATOR, thread.gettCreator());
        values.put(T_C_ROLE, thread.gettCreatorRole());
        values.put(T_STATUS, thread.gettStatusString());
        values.put(T_DATE, thread.gettDate());
        values.put(T_MSG_COUNT, thread.gettMsgCount());
        values.put(T_TYPE, thread.gettType().name());
        int flagNew = (thread.isNew()) ? 1 : 0;
        int flagMsgs = (thread.hasNewMsgs()) ? 1 : 0;
        values.put(T_IS_NEW, flagNew);
        values.put(T_HAS_NEW_M, flagMsgs);

        db.insert(TABLE_THREADS, null, values);
        db.close();
    }

    /**
     * Get a BlubbMessage from the Sqlite database.
     *
     * @param mId Unique id of the message.
     * @return BlubbMessage object with the id provided.
     */
    public BlubbMessage getMessage(String mId) {
        Log.v(N, "getMessage(mId = " + mId + ")");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                new String[]{
                        M_ID,
                        M_TITLE,
                        M_CONTENT,
                        M_ROLE,
                        M_CREATOR,
                        M_DATE,
                        M_TYPE,
                        M_THREAD_ID,
                        M_LINK,
                        M_IS_NEW},
                M_ID + "=?",
                new String[]{String.valueOf(mId)}, null, null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {

            BlubbMessage message = new BlubbMessage(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getInt(9));

            db.close();
            return message;
        }
        Log.v(N, "Could not find message " + mId + " in db.");

        db.close();
        return null;
    }

    /**
     * Get a BlubbThread from the Sqlite database.
     *
     * @param tId Unique id of the thread.
     * @return BlubbThread object with the id provided.
     */
    public BlubbThread getThread(String tId) {
        Log.v(N, "getThread(tId = " + tId + ")");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_THREADS,
                new String[]{
                        T_ID,
                        T_TITLE,
                        T_DESC,
                        T_CREATOR,
                        T_C_ROLE,
                        T_STATUS,
                        T_DATE,
                        T_MSG_COUNT,
                        T_TYPE,
                        T_IS_NEW,
                        T_HAS_NEW_M},
                T_ID + "=?",
                new String[]{String.valueOf(tId)}, null, null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {

            BlubbThread thread = new BlubbThread(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getInt(10));

            db.close();
            return thread;
        }
        Log.v(N, "Could not find thread " + tId + " in db.");
        db.close();
        return null;
    }

    /**
     * Get all BlubbMessages stored in the Sqlite database.
     *
     * @return A list with all BlubbMessage objects stored in the database.
     */
    @SuppressWarnings("UnusedDeclaration")
    public List<BlubbMessage> getAllMessages() {
        Log.v(N, "getAllMessages()");
        List<BlubbMessage> msgList = new ArrayList<BlubbMessage>();
        String selectAllQuery = "SELECT * FROM " + TABLE_MESSAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectAllQuery, null);
        Log.v(N, "Found " + cursor.getCount() + " messages.");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                BlubbMessage message = new BlubbMessage(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getInt(9));
                msgList.add(message);
            } while (cursor.moveToNext());
        }
        db.close();
        return msgList;
    }

    /**
     * Get all BlubbMessages to a thread.
     *
     * @param tId Id of the thread.
     * @return List of BlubbMessages which were created within the thread with the given id.
     */
    public List<BlubbMessage> getMessagesForThread(String tId) {
        Log.v(N, "getMessagesForThread(tId = " + tId + ")");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                new String[]{
                        M_ID,
                        M_TITLE,
                        M_CONTENT,
                        M_ROLE,
                        M_CREATOR,
                        M_DATE,
                        M_TYPE,
                        M_THREAD_ID,
                        M_LINK,
                        M_IS_NEW},      // Select * From table_messages
                M_THREAD_ID + "=?",   // Where mThreadId = ?
                new String[]{String.valueOf(tId)},   // ? = tId
                null, null, null, null
        );
        List<BlubbMessage> msgList = new ArrayList<BlubbMessage>();
        Log.v(N, "Found " + cursor.getCount() + " messages for thread.");
        if (cursor.moveToFirst()) {
            do {
                BlubbMessage message = new BlubbMessage(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getInt(9));
                Log.v("SQLite", "getting msg " + message.getmId() + " from db.");
                msgList.add(message);
            } while (cursor.moveToNext());
        }
        db.close();
        return msgList;
    }

    /**
     * Get all BlubbThreads stored in the Sqlite database.
     *
     * @return List with all BlubbThreads stored in the database.
     */
    public List<BlubbThread> getAllThreads() {
        Log.v(N, "getAllThreads()");
        List<BlubbThread> tList = new ArrayList<BlubbThread>();
        String selectAllQuery = "SELECT * FROM " + TABLE_THREADS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectAllQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BlubbThread thread = new BlubbThread(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10));
                tList.add(thread);
            } while (cursor.moveToNext());
        }
        db.close();
        return tList;
    }

    /**
     * Set the read flag of a BlubbMessage to true.
     *
     * @param mId Id of the message which had been read.
     */
    public void setMessageRead(String mId) {
        Log.v(N, "setMessageRead(mId = " + mId + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_IS_NEW, 0);

        db.update(TABLE_MESSAGES, values, M_ID + "=?", new String[]{mId});
        db.close();
    }

    /**
     * Set the hasNewMsgs flag of a thread.
     *
     * @param tId        Id of the thread.
     * @param hasNewMsgs value which should be set.
     */
    public void setThreadNewMsgs(String tId, boolean hasNewMsgs) {
        Log.v(N, "setThreadNewMsgs(thread = " + tId + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        int value = 0;
        if (hasNewMsgs) value = 1;
        ContentValues values = new ContentValues();
        values.put(T_HAS_NEW_M, value);

        db.update(TABLE_THREADS, values, T_ID + "=?", new String[]{tId});
        db.close();
    }

    /**
     * Update the values of a stored BlubbMessage, e.g. when the content has been changed.
     *
     * @param message BlubbMessage object with some changed values, which will be stored to the db.
     */
    public void updateMessage(BlubbMessage message) {
        Log.v(N, "updateMessage(message = " + message.getmId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_ID, message.getmId());
        values.put(M_TITLE, message.getmTitle());
        values.put(M_CONTENT, message.getmContent().getStringRepresentation());
        values.put(M_ROLE, message.getmCreatorRole());
        values.put(M_CREATOR, message.getmCreator());
        values.put(M_DATE, message.getmDate());
        values.put(M_TYPE, message.getmType());
        values.put(M_THREAD_ID, message.getmThread());
        values.put(M_LINK, message.getmLink());
        int flag = (message.isNew()) ? 1 : 0;
        values.put(M_IS_NEW, flag);

        db.update(TABLE_MESSAGES, values, M_ID + "=?", new String[]{message.getmId()});
        db.close();
        Log.v(N, "Updated message " + message.getmId());
    }

    /**
     * Update the values of a stored BlubbMessage, e.g. when the content has been changed.
     * This is without the isNew flag.
     *
     * @param message BlubbMessage object with some changed values, which will be stored to the db.
     */
    public void updateMessageFromBeap(BlubbMessage message) {
        Log.v(N, "updateMessageFromBeap(message = " + message.getmId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_ID, message.getmId());
        values.put(M_TITLE, message.getmTitle());
        values.put(M_CONTENT, message.getmContent().getStringRepresentation());
        values.put(M_ROLE, message.getmCreatorRole());
        values.put(M_CREATOR, message.getmCreator());
        values.put(M_DATE, message.getmDate());
        values.put(M_TYPE, message.getmType());
        values.put(M_THREAD_ID, message.getmThread());
        values.put(M_LINK, message.getmLink());

        db.update(TABLE_MESSAGES, values, M_ID + "=?", new String[]{message.getmId()});
        db.close();
        Log.v(N, "Updated message " + message.getmId());
    }

    /**
     * Update the values of a stored BlubbThread, e.g. when the description has been changed.
     *
     * @param thread BlubbThread object with some changed values.
     */
    public void updateThread(BlubbThread thread) {
        Log.v(N, "updateThread(thread = " + thread.gettId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(T_ID, thread.gettId());
        values.put(T_TITLE, thread.getThreadTitle());
        values.put(T_DESC, thread.gettDesc());
        values.put(T_CREATOR, thread.gettCreator());
        values.put(T_C_ROLE, thread.gettCreatorRole());
        values.put(T_STATUS, thread.gettStatusString());
        values.put(T_DATE, thread.gettDate());
        values.put(T_MSG_COUNT, thread.gettMsgCount());
        values.put(T_TYPE, thread.gettType().name());
        int flagNew = (thread.isNew()) ? 1 : 0;
        int flagMsgs = (thread.hasNewMsgs()) ? 1 : 0;
        values.put(T_IS_NEW, flagNew);
        values.put(T_HAS_NEW_M, flagMsgs);

        db.update(TABLE_THREADS, values, T_ID + "=?", new String[]{thread.gettId()});
        db.close();
        Log.v(N, "Updated thread " + thread.gettId());
    }

    /**
     * Update the values of a stored BlubbThread, e.g. when the description has been changed.
     * Without the isNew or hasNewMsgs flags.
     *
     * @param thread BlubbThread object with some changed values.
     */
    public void updateThreadFromBeap(BlubbThread thread) {
        Log.v(N, "updateThreadFromBeap(thread = " + thread.gettId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(T_ID, thread.gettId());
        values.put(T_TITLE, thread.getThreadTitle());
        values.put(T_DESC, thread.gettDesc());
        values.put(T_CREATOR, thread.gettCreator());
        values.put(T_C_ROLE, thread.gettCreatorRole());
        values.put(T_STATUS, thread.gettStatusString());
        values.put(T_DATE, thread.gettDate());
        values.put(T_MSG_COUNT, thread.gettMsgCount());
        values.put(T_TYPE, thread.gettType().name());

        db.update(TABLE_THREADS, values, T_ID + "=?", new String[]{thread.gettId()});
        db.close();
        Log.v(N, "Updated thread " + thread.gettId());
    }

    /**
     * @return Number of messages stored in the Sqlite database.
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getMessageCount() {
        String countQuery = "SELECT * FROM " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        db.close();
        return cursor.getCount();
    }

    /**
     * @return Number of threads stored in the Sqlite database.
     */
    public int getThreadCount() {
        String countQuery = "SELECT * FROM " + TABLE_THREADS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

}
