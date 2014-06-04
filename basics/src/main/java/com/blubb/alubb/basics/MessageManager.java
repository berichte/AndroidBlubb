package com.blubb.alubb.basics;

import android.content.Context;
import android.util.Log;

import com.blubb.alubb.beapcom.BPC;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.beapcom.BlubbRequestManager;
import com.blubb.alubb.beapcom.BlubbResponse;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Benni on 31.05.2014.
 */
public class MessageManager {
    private static final String NAME = "MessageManager";

    private static MessageManager manager;
    private MessageManager() {
        this.loadedMessages = new HashMap<String, List<BlubbMessage>>();
    }

    public static MessageManager getInstance() {
        if(manager == null) manager = new MessageManager();
        return manager;
    }

    private HashMap<String, List<BlubbMessage>> loadedMessages;

    public List<BlubbMessage> getNewMessagesFromAllThreads(Context context)
            throws BlubbDBException, InvalidParameterException,
            JSONException, SessionException, BlubbDBConnectionException {
        Log.v(NAME, "getNewMessagesFromAllThreads(context)");

        List<BlubbThread> threads = ThreadManager.getInstance()
                .getAllThreadsFromBeap(context);

        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        for (BlubbThread t: threads) {
             messages.addAll(this.getNewMessagesForThread(context, t));
        }
        return messages;
    }

    private List<BlubbMessage> getNewMessagesForThread(Context context, BlubbThread thread)
            throws BlubbDBException, InvalidParameterException,
            JSONException, SessionException, BlubbDBConnectionException {
        Log.v(NAME, "getNewMessagesForThread(context, thread = " + thread.gettId());
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbThread sqliteThread = db.getThread(thread.gettId());

        List<BlubbMessage> beapMessages = new ArrayList<BlubbMessage>();
        // if the threads have the same msgCount nothing has changed,
        // if local is bigger msg has been deleted. -> "<"
        if(sqliteThread.gettMsgCount()<thread.gettMsgCount()) {
            beapMessages = getAllMessagesForThreadFromBeap(context, thread.gettId());
            List<BlubbMessage> sqliteMessages =
                    getAllMessagesForThreadFromSqlite(context, thread.gettId());
            // get difference between the two lists
            beapMessages.removeAll(sqliteMessages);
            boolean flag = false;
            for (BlubbMessage m : beapMessages) {
                //set all new messages to new.
                m.setNew(true);
                flag = true;
            }
            if (flag) {
                // if there where new Msgs set flag in thread.

                thread.setHasNewMsgs(true);
                db = new DatabaseHandler(context);
                db.setThreadNewMsgs(thread.gettId());
            }
            for (BlubbMessage m : sqliteMessages) {
                if (m.isNew()) beapMessages.add(m);
            }
        }
        Log.i(NAME, "Thread " + thread.gettId() + " has " +
                beapMessages.size() + " new Messages.");
        return beapMessages;
    }

    public BlubbMessage createMsg(Context context,
                                  String tId, String mTitle, String mContent)
            throws InvalidParameterException, BlubbDBException,
            SessionException, BlubbDBConnectionException {
        Log.v(NAME, "createMsg(context, tId = " + tId + ", mTitle = " +
                mTitle + ", mContent = " + mContent + ")");
        mTitle = BPC.parseStringParameterToDB(mTitle);
        mContent = BPC.parseStringParameterToDB(mContent);
        String ptId = BPC.parseStringParameterToDB(tId);
        String query = "tree.functions.createMsg(self," +
                ptId + "," + mTitle + "," + mContent + ")";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        Log.v(NAME, "Executed query: " + query + " with response status: " + response.getStatus());
        switch (response.getStatus()) {
            case OK:
                JSONObject result = (JSONObject) response.getResultObj();
                BlubbMessage message = new BlubbMessage(result);
                DatabaseHandler db = new DatabaseHandler(context);
                db.addMessage(message);
                List<BlubbMessage> messages = this.loadedMessages.get(tId);
                if(messages == null) {
                    messages = new ArrayList<BlubbMessage>();
                    this.loadedMessages.put(tId, messages);
                }
                messages.add(message);
                return message;
            default:
                throw new BlubbDBException("Could not perform createMsg" +
                        " Beap status: " + response.getStatus());
        }
    }


    public List<BlubbMessage> getAllMessagesForThread(Context context, String tId) {
        Log.v(NAME, "getAllMessagesForThread(context, tId = " + tId);
        try {
            return getAllMessagesForThreadFromBeap(context, tId);
        } catch (InvalidParameterException e) {
            Log.e(NAME, e.getMessage());
        } catch (BlubbDBException e) {
            Log.e(NAME, e.getMessage());
        } catch (SessionException e) {
            Log.e(NAME, e.getMessage());
        } catch (JSONException e) {
            Log.e(NAME, e.getMessage());
        } catch (BlubbDBConnectionException e) {
            Log.e(NAME, e.getMessage());
        }
        Log.v(NAME, "Did not receive msgs form Beap. Returning local msgs.");
        return getAllMessagesForThreadFromSqlite(context, tId);

    }

    private List<BlubbMessage> getAllMessagesForThreadFromBeap(Context context, String tId)
            throws InvalidParameterException, BlubbDBException, SessionException,
            JSONException, BlubbDBConnectionException {
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
                for(int i = 0; i < jsonArray.length(); i++) {
                    BlubbMessage message = new BlubbMessage(jsonArray.getJSONObject(i));
                    messages.add(message);
                    putMessageToSqlite(context, message);
                }
                return messages;
            case NO_CONTENT:
                return messages;
            default:
                throw new BlubbDBException("Could not get Messages from beap " +
                        "Response status: " + response.getStatus());
        }
    }

    private void putMessageToSqlite(Context context, BlubbMessage message) {
        Log.v(NAME, "putMessageToSqlite(context, message = " + message.getmId() + ")");
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbMessage m = db .getMessage(message.getmId());
        if(m != null) {
            if(!message.equals(m)) {
                db.updateMessage(message);
            }
        } else {
            message.setNew(true);
            db.addMessage(message);
        }
    }
    private List<BlubbMessage> getAllMessagesForThreadFromSqlite(Context context, String tId) {
         if(this.loadedMessages.containsKey(tId)) {
            return this.loadedMessages.get(tId);
        } else {
            DatabaseHandler db = new DatabaseHandler(context);
            List<BlubbMessage> messages = db.getMessagesForThread(tId);
            this.loadedMessages.put(tId, messages);
            return messages;
        }

    }

}