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
 * Created by Benni on 31.05.2014.
 */
public class MessageManager {
    private static final String NAME = "MessageManager";

    private static MessageManager manager;

    private MessageManager() {

    }

    public static MessageManager getInstance() {
        if (manager == null) manager = new MessageManager();
        return manager;
    }

    public List<BlubbMessage> getNewMessagesFromAllThreads(Context context)
            throws BlubbDBException,
            JSONException, SessionException, BlubbDBConnectionException, PasswordInitException {
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
            BlubbThread lT = hashThreads.get(t.gettId());
            if (t.gettMsgCount() > lT.gettMsgCount()) {
                messages.addAll(this.getNewMessagesForThread(context, t));
            }
        }
        return messages;
    }

    private List<BlubbMessage> getNewMessagesForThread(Context context, BlubbThread thread)
            throws BlubbDBException,
            JSONException, SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "getNewMessagesForThread(context, thread = " + thread.gettId());
        List<BlubbMessage> messages = getAllMessagesForThread(context, thread.gettId());
        List<BlubbMessage> newMsgs = new ArrayList<BlubbMessage>();
        for (BlubbMessage m : messages) {
            if (m.isNew()) newMsgs.add(m);
        }
        return newMsgs;
    }

    public BlubbMessage createMsg(Context context, String... blubbs)
            throws BlubbDBException,
            SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "createMsg(context, tId = " + blubbs[0] + ", mTitle = " +
                blubbs[1] + ", mContent = " + blubbs[2] + ")");

        String mTitle = BPC.parseStringParameterToDB(blubbs[0]);
        String mContent = BPC.parseStringParameterToDB(blubbs[1]);
        String mLink = BPC.parseStringParameterToDB(blubbs[2]);
        String tId = "[";
        for (int i = 3; i < blubbs.length - 1; i++) {
            tId = tId + BPC.parseStringParameterToDB(blubbs[i]) + ",";
        }
        tId = tId + BPC.parseStringParameterToDB(blubbs[blubbs.length - 1]) + "]";
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

    public List<BlubbMessage> getAllMessagesForThread(Context context, String tId) {
        Log.v(NAME, "getAllMessagesForThread(context, tId = " + tId);
        try {
            // this makes an update for the messages
            getAllMessagesForThreadFromBeap(context, tId);
        } catch (BlubbDBException e) {
            Log.e(NAME, e.getMessage());
        } catch (SessionException e) {
            Log.e(NAME, e.getMessage());
        } catch (JSONException e) {
            Log.e(NAME, e.getMessage());
        } catch (BlubbDBConnectionException e) {
            Log.e(NAME, e.getMessage());
        } catch (PasswordInitException e) {
            Log.e(NAME, e.getMessage());
        }

        return getAllMessagesForThreadFromSqlite(context, tId);
    }

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

    private List<BlubbMessage> getAllMessagesForThreadFromSqlite(Context context, String tId) {
        DatabaseHandler db = new DatabaseHandler(context);
        List<BlubbMessage> messages = db.getMessagesForThread(tId);
        return messages;

    }

    public String setMsg(Context context, BlubbMessage message)
            throws BlubbDBException, PasswordInitException,
            SessionException, BlubbDBConnectionException {

        String mId = BPC.parseStringParameterToDB(message.getmId());
        String mTitle = BPC.parseStringParameterToDB(message.getmTitle());
        String mContent = BPC.parseStringParameterToDB(message.getmContent());
        String mLink = "";
        String query = "";
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
                JSONObject result = (JSONObject) response.getResultObj();
                BlubbMessage changedMessage = new BlubbMessage(result);
                DatabaseHandler db = new DatabaseHandler(context);
                db.updateMessage(message);
                return response.getStatusDescr();
            default:
                throw new BlubbDBException("Could not perform setMsg" +
                        " Beap status: " + response.getStatus());
        }

    }


}
