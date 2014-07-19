package com.blubb.alubb.basics;

import android.content.Context;
import android.util.Log;

import com.blubb.alubb.beapcom.BPC;
import com.blubb.alubb.beapcom.BlubbRequestManager;
import com.blubb.alubb.beapcom.BlubbResponse;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A Singleton class managing the access to BlubbMessages.
 * It's the bridge between user interface, beapDB and the local sqlite database.
 * If new messages are available at beapDB the local sqlite database will be updated automatically.
 * Created by Benni on 31.05.2014.
 */
public class MessageManager {
    /**
     * Name for Logging purposes
     */
    private static final String NAME = "MessageManager";

    /**
     * Instance of the MessageManager to realize the singleton pattern.
     */
    private static MessageManager manager;

    /**
     * Private constructor to realize the singleton pattern.
     */
    private MessageManager() {
    }

    /**
     * Get the one and only MessageManager instance,
     * easy accessible through the MessageManager class.
     *
     * @return The MessageManager object.
     */
    public static MessageManager getInstance() {
        if (manager == null) manager = new MessageManager();
        return manager;
    }

    /**
     * Get all messages from all threads where isNew is true.
     * The message count of all local and all threads on beapDB are compared,
     * when there is a difference on a thread getNewMsgsForThread(..) will be called
     * and all new messages for a thread added to the returning list.
     *
     * @param context The application context from which the method is called.
     * @return A list of BlubbMessages with isNew true.
     * @throws BlubbDBException           if the response status is neither 'OK' nor 'NO_CONTENT'.
     * @throws JSONException              if the value of the json array for threads within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw on
     *                                    getSessionID().
     */
    public List<BlubbMessage> getNewMessagesFromAllThreads(Context context)
            throws BlubbDBException, JSONException,
            SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "getNewMessagesFromAllThreads(context)");
        List<BlubbThread> localThreads = ThreadManager.getInstance()
                .getAllThreadsFromSqlite(context);
        HashMap<String, BlubbThread> hashThreads = new HashMap<String, BlubbThread>();
        for (BlubbThread t : localThreads) {
            hashThreads.put(t.gettId(), t);
        }
        List<BlubbThread> threads = ThreadManager.getInstance()
                .getAllThreadsFromBeap(context);

        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        for (BlubbThread t : threads) {
            BlubbThread localThread = hashThreads.get(t.gettId());
            if (t.gettMsgCount() > localThread.gettMsgCount()) {
                messages.addAll(this.getNewMessagesForThread(context, t));
            }
        }
        return messages;
    }

    /**
     * Get new messages of a thread.
     *
     * @param context The application context from which the method is called.
     * @param thread  The thread of which the new messages shall be returned.
     * @return List of BlubbMessages with m.tId = thread.tId and m.isNew = true.
     * @throws BlubbDBException           if the response status is neither 'OK' nor 'NO_CONTENT'.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw on
     *                                    getSessionID().
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws JSONException              if the value of the json array for messages within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     */
    private List<BlubbMessage> getNewMessagesForThread(Context context, BlubbThread thread)
            throws BlubbDBException, PasswordInitException, BlubbDBConnectionException,
            JSONException, SessionException {
        Log.v(NAME, "getNewMessagesForThread(context, thread = " + thread.gettId());
        List<BlubbMessage> messages = getAllMessagesForThread(context, thread.gettId());
        List<BlubbMessage> newMsgs = new ArrayList<BlubbMessage>();
        for (BlubbMessage m : messages) {
            if (m.isNew()) newMsgs.add(m);
        }
        return newMsgs;
    }

    /**
     * Creates a new message on the beapDB, on a positive response the message will be
     * added to the sqlite database.
     *
     * @param context          The application context from which the method is called.
     * @param messageParameter String parameter for the new message: {mTitle, mContent, mLink, tId1, tId2,...}
     * @return The new created BlubbMessage object.
     * @throws BlubbDBException           if the response status is not 'OK'.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw on
     *                                    getSessionID().
     */
    public BlubbMessage createMsg(Context context, String... messageParameter)
            throws BlubbDBException,
            SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "createMsg(context, tId = " + messageParameter[0] + ", mTitle = " +
                messageParameter[1] + ", mContent = " + messageParameter[2] + ")");

        String mTitle = BPC.parseStringParameterToDB(messageParameter[0]);
        String mContent = BPC.parseStringParameterToDB(messageParameter[1]);
        String mLink = BPC.parseStringParameterToDB(messageParameter[2]);
        String tId = "[";
        for (int i = 3; i < messageParameter.length - 1; i++) {
            tId = tId + BPC.parseStringParameterToDB(messageParameter[i]) + ",";
        }
        tId = tId + BPC.parseStringParameterToDB(messageParameter[messageParameter.length - 1]) + "]";
        String query;
        if (mLink.equals("")) {
            query = "tree.functions.createMsg(self," +
                    tId + "," + mTitle + "," + mContent + ")";
        } else {
            query = "tree.functions.createMsg(self," +
                    tId + "," + mTitle + "," + mContent + "," + mLink + ")";
        }
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        Log.v(NAME, "Executed query: " + query + " with response status: " + response.getStatus());
        switch (response.getStatus()) {
            case OK:
                JSONObject result = (JSONObject) response.getResultObj();
                BlubbMessage message = new BlubbMessage(result);
                DatabaseHandler db = new DatabaseHandler(context);
                db.addMessage(message);
                return message;
            default:
                throw new BlubbDBException("Could not perform createMsg" +
                        " Beap status: " + response.getStatus());
        }
    }

    /**
     * Get all messages for a thread.
     *
     * @param context The application context from which the method is called.
     * @param tId     The id of the thread.
     * @return A list of BlubbMessages belonging to the thread.
     * @throws BlubbDBException           if the response status is neither 'OK' nor 'NO_CONTENT'.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw on
     *                                    getSessionID().
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws JSONException              if the value of the json array for messages within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     */
    public List<BlubbMessage> getAllMessagesForThread(Context context, String tId)
            throws BlubbDBException, PasswordInitException, BlubbDBConnectionException,
            SessionException, JSONException {
        Log.v(NAME, "getAllMessagesForThread(context, tId = " + tId);
        getAllMessagesForThreadFromBeap(context, tId);
        return getAllMessagesForThreadFromSqlite(context, tId);
    }

    /**
     * Get all messages for a thread from beapDB.
     *
     * @param context The application context from which the method is called.
     * @param tId     The id of the thread.
     * @return A list of BlubbMessages from the beapDB and not locally stored.
     * @throws BlubbDBException           if the response status is neither 'OK' nor 'NO_CONTENT'.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws JSONException              if the value of the json array for messages within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw on
     *                                    getSessionID().
     */
    private List<BlubbMessage> getAllMessagesForThreadFromBeap(Context context, String tId)
            throws BlubbDBException, SessionException,
            JSONException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "getAllMessagesForThreadFromBeap(context, tId = " + tId);
        tId = BPC.parseStringParameterToDB(tId);
        String query = "tree.functions.getMsgsForThread(self," + tId + ")";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        Log.v(NAME, "Executed query: " + query + " with response status: " + response.getStatus());
        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        switch (response.getStatus()) {
            case OK:
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                for (int i = 0; i < jsonArray.length(); i++) {
                    BlubbMessage message = new BlubbMessage(jsonArray.getJSONObject(i));
                    messages.add(message);
                    putMessageToSqliteFromBeap(context, message);
                }
                return messages;
            case NO_CONTENT:
                return messages;
            default:
                throw new BlubbDBException("Could not get Messages from beap " +
                        "Response status: " + response.getStatus());
        }
    }

    /**
     * Put a message to the sqlite database. If the db contains a message with the same id it will
     * be updated otherwise it will be added.
     *
     * @param context The application context from which the method is called.
     * @param message The message which will be stored at the sqlite database.
     */
    private void putMessageToSqliteFromBeap(Context context, BlubbMessage message) {
        Log.v(NAME, "putMessageToSqliteFromBeap(context, message = " + message.getmId() + ")");
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbMessage m = db.getMessage(message.getmId());
        if (m != null) {
            if (!message.equals(m)) {
                db.updateMessageFromBeap(message);
            }
        } else {
            message.setNew(true);
            db.setThreadNewMsgs(message.getmThread(), true);
            db.addMessage(message);
        }
    }

    /**
     * Get all messages for a thread, stored in the local sqlite database.
     *
     * @param context The application context from which the method is called.
     * @param tId     The id of the thread.
     * @return A List of BlubbMessages from the sqlite database.
     */
    private List<BlubbMessage> getAllMessagesForThreadFromSqlite(Context context, String tId) {
        DatabaseHandler db = new DatabaseHandler(context);
        return db.getMessagesForThread(tId);

    }

    /**
     * Changes a message at beapDB and the local sqlite database.
     *
     * @param context The application context from which the method is called.
     * @param message The message with the new title and content.
     * @return The status description of the blubbResponse.
     * @throws BlubbDBException           if the response status is not 'OK'.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw on
     *                                    getSessionID().
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     */
    public String setMsg(Context context, BlubbMessage message)
            throws BlubbDBException, PasswordInitException,
            SessionException, BlubbDBConnectionException {

        String mId = BPC.parseStringParameterToDB(message.getmId());
        String mTitle = BPC.parseStringParameterToDB(message.getmTitle());

        String mContent = BPC.parseStringParameterToDB(message.getmContent()
                .getStringRepresentation());
        String mLink;
        String query;
        if (!message.getmLink().equals(BPC.UNDEFINED)) {
            mLink = BPC.parseStringParameterToDB(message.getmLink());
            query = "tree.functions.setMsg(self," + mId + "," + mTitle + "," + mContent
                    + "," + mLink + ")";
        } else {
            query = "tree.functions.setMsg(self," + mId + "," + mTitle + "," + mContent + ")";
        }
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        Log.v(NAME, "Executed query: " + query + " with response status: " + response.getStatus());
        switch (response.getStatus()) {
            case OK:
                DatabaseHandler db = new DatabaseHandler(context);
                db.updateMessage(message);
                return response.getStatusDesc();
            default:
                throw new BlubbDBException("Could not perform setMsg" +
                        " Beap status: " + response.getStatus());
        }
    }
}
