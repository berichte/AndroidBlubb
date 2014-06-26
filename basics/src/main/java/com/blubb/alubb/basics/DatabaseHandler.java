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
 * Created by Benni on 30.05.2014.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String N = "SQLite";
    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;
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

    //Column names for messages
    private static final String
            M_ID = "mId",
            M_TITLE = "mTitle",
            M_CONTENT = "mContent",
            M_ROLE = "mRole",
            M_CREATOR = "mCreator",
            M_DATE = "mDate",
            M_TYPE = "mType",
            M_THREAD_ID = "mThreadId",
            M_LINK = "mLink",
            M_IS_NEW = "mIsNew";

    //Column names for threads
    private static final String
            T_ID = "tId",
            T_TITLE = "tTitle",
            T_DESC = "tDescription",
            T_CREATOR = "tCreator",
            T_C_ROLE = "tCreatorRole",
            T_DATE = "tDate",
            T_MSG_COUNT = "tMsgCount",
            T_TYPE = "tType",
            T_IS_NEW = "tIsNew",
            T_HAS_NEW_M = "tHasNewMsg";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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

    public void addMessage(BlubbMessage message) {
        Log.v(N, "addMessage(message = " + message.getmId() + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_ID, message.getmId());
        values.put(M_TITLE, message.getmTitle());
        values.put(M_CONTENT, message.getmContent());
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

    public void addThread(BlubbThread thread) {
        Log.v(N, "addThread(thread = " + thread.gettId() + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(T_ID, thread.gettId());
        values.put(T_TITLE, thread.getThreadTitle());
        values.put(T_DESC, thread.gettDesc());
        values.put(T_CREATOR, thread.gettCreator());
        values.put(T_C_ROLE, thread.gettCreatorRole());
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
                    cursor.getInt(6),
                    cursor.getString(7),
                    cursor.getInt(8),
                    cursor.getInt(9));

            db.close();
            return thread;
        }
        Log.v(N, "Could not find thread " + tId + " in db.");
        db.close();
        return null;
    }

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
                        cursor.getInt(6),
                        cursor.getString(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                tList.add(thread);
            } while (cursor.moveToNext());
        }
        db.close();
        return tList;
    }

    public void setMessageRead(String mId) {
        Log.v(N, "setMessageRead(mId = " + mId + ")");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_IS_NEW, 0);

        int id = db.update(TABLE_MESSAGES, values, M_ID + "=?", new String[]{mId});
        db.close();
    }

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

    public void updateMessage(BlubbMessage message) {
        Log.v(N, "updateMessage(message = " + message.getmId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_ID, message.getmId());
        values.put(M_TITLE, message.getmTitle());
        values.put(M_CONTENT, message.getmContent());
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

    public void updateMessageFromBeap(BlubbMessage message) {
        Log.v(N, "updateMessageFromBeap(message = " + message.getmId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(M_ID, message.getmId());
        values.put(M_TITLE, message.getmTitle());
        values.put(M_CONTENT, message.getmContent());
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

    public void updateThread(BlubbThread thread) {
        Log.v(N, "updateThread(thread = " + thread.gettId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(T_ID, thread.gettId());
        values.put(T_TITLE, thread.getThreadTitle());
        values.put(T_DESC, thread.gettDesc());
        values.put(T_CREATOR, thread.gettCreator());
        values.put(T_C_ROLE, thread.gettCreatorRole());
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

    public void updateThreadFromBeap(BlubbThread thread) {
        Log.v(N, "updateThreadFromBeap(thread = " + thread.gettId());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(T_ID, thread.gettId());
        values.put(T_TITLE, thread.getThreadTitle());
        values.put(T_DESC, thread.gettDesc());
        values.put(T_CREATOR, thread.gettCreator());
        values.put(T_C_ROLE, thread.gettCreatorRole());
        values.put(T_DATE, thread.gettDate());
        values.put(T_MSG_COUNT, thread.gettMsgCount());
        values.put(T_TYPE, thread.gettType().name());

        db.update(TABLE_THREADS, values, T_ID + "=?", new String[]{thread.gettId()});
        db.close();
        Log.v(N, "Updated thread " + thread.gettId());
    }

    public int getMessageCount() {
        String countQuery = "SELECT * FROM " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        db.close();
        return cursor.getCount();
    }

    public int getThreadCount() {
        String countQuery = "SELECT * FROM " + TABLE_THREADS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

}
