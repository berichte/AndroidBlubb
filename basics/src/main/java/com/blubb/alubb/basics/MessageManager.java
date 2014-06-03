package com.blubb.alubb.basics;

import android.content.Context;
import android.util.Log;

import com.blubb.alubb.beapcom.BPC;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.beapcom.BlubbRequestManager;
import com.blubb.alubb.beapcom.BlubbResponse;
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
            JSONException, SessionException {
        List<BlubbThread> threads = ThreadManager.getInstance()
                .getAllThreadsFromSqlite(context);
        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        for (BlubbThread t: threads) {
             messages.addAll(this.getNewMessagesForThread(context, t.gettId()));
        }
        return messages;
    }

    private List<BlubbMessage> getNewMessagesForThread(Context context, String tId)
            throws BlubbDBException, InvalidParameterException,
            JSONException, SessionException {
        List<BlubbMessage> beapMessages = getAllMessagesForThreadFromBeap(context, tId);
        List<BlubbMessage> sqliteMessages = getAllMessagesForThreadFromSqlite(context, tId);
        beapMessages.removeAll(sqliteMessages);
        boolean flag = false;
        for(BlubbMessage m: beapMessages) {
            m.setNew(true);
            flag = true;
        }
        if(flag) {
            BlubbThread t = ThreadManager.getInstance().getThread(context, tId);
            t.setHasNewMsgs(true);
            DatabaseHandler db = new DatabaseHandler(context);
            db.setThreadNewMsgs(tId);
        }
        for (BlubbMessage m: sqliteMessages) {
            if(m.isNew()) beapMessages.add(m);
        }
        return beapMessages;
    }

    public BlubbMessage createMsg(Context context,
                                  String tId, String mTitle, String mContent)
            throws InvalidParameterException, BlubbDBException, SessionException {
        mTitle = BPC.parseStringParameterToDB(mTitle);
        mContent = BPC.parseStringParameterToDB(mContent);
        String query = "tree.functions.createMsg(self," +
                tId + "," + mTitle + "," + mContent + ")";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        switch (response.getStatus()) {
            case OK:
                JSONObject result = (JSONObject) response.getResultObj();
                BlubbMessage message = new BlubbMessage(result);
                DatabaseHandler db = new DatabaseHandler(context);
                db.addMessage(message);
                this.loadedMessages.get(tId).add(message);
                return message;
            default:
                throw new BlubbDBException("Could not perform createMsg" +
                        " Beap status: " + response.getStatus());
        }
    }


    public List<BlubbMessage> getAllMessagesForThread(Context context, String tId) {
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
        }
        return getAllMessagesForThreadFromSqlite(context, tId);

    }

    private List<BlubbMessage> getAllMessagesForThreadFromBeap(Context context, String tId)
            throws InvalidParameterException, BlubbDBException, SessionException, JSONException {
        tId = BPC.parseStringParameterToDB(tId);
        String query = "tree.functions.getMsgsForThread(self," + tId + ")";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        switch (response.getStatus()) {
            case OK:
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                for(int i = 0; i < jsonArray.length(); i++) {
                    messages.add(new BlubbMessage(jsonArray.getJSONObject(i)));
                }
                return messages;
            case NO_CONTENT:
                return messages;
            default:
                throw new BlubbDBException("Could not get Messages from beap " +
                        "Response status: " + response.getStatus());
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
/*
    public static BlubbDBReplyStatus sendMessage(Context context, String tId, String mTitle,
     String mContent)
            throws BlubbDBException, BlubbDBConnectionException, InvalidParameterException {
        // Build the request for the BeapDB .createMsg(self, "tID", "mTitle", "mContent")
        String url = BlubbRequestManager.buildQuery(
                "tree.functions.createMsg" + F_SELF +
                        encPara(tId) + F_SEP +
                        encPara(BPC.parseStringParameterToDB(mTitle)) + F_SEP +
                        encPara(BPC.parseStringParameterToDB(mContent)) + F_END
        );

        // execute the request
        BlubbResponse response = executeRequest(context, url);
        // return the reply status
        return response.getStatus();
    }


/*
    public static boolean newMessagesAvailable(Context context) {
        int quickCheckMessages = 0;
        try {
            quickCheckMessages = BlubbComManager.quickCheck(context)[1];
        } catch (BlubbDBException e) {
            Log.e("MessageManager", e.getMessage());
            return false;
        }
        if(msgAtBeap < quickCheckMessages) {
            return true;
        }
        return false;
    }

    public static List<BlubbMessage> getNewMessages(Context context) {
        int quickCheckMessages = 0;
        try {
            quickCheckMessages = BlubbComManager.quickCheck(context)[1];
        } catch (BlubbDBException e) {
            Log.e("MessageManager", e.getMessage());
            return new ArrayList<BlubbMessage>();
        }
        if(msgAtBeap < quickCheckMessages) {
            msgAtBeap = quickCheckMessages;
            List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
            List<BlubbThread> threads = ThreadManager.getAllThreads(context);
            for(BlubbThread bt: threads) {
                if(bt.hasNewMsgs()) {

                    messages.addAll(getMessagesNewForThread(context, bt.gettId()));
                }
            }
            return messages;
        }
        return new ArrayList<BlubbMessage>();
    }

    private static List<BlubbMessage> getMessagesNewForThread(Context context, String tId) {
        List<BlubbMessage> list = getMessagesForThread(context, tId),
                result = new ArrayList<BlubbMessage>();
        for (BlubbMessage m: list) {
            if(m.isNew()) result.add(m);
        }
        return result;
    }

    private static List<BlubbMessage> addMessageToList(
            List<BlubbMessage> list, BlubbMessage message) {
        // if the list is empty return it with the message
        if(list.isEmpty()) {
            Log.i("MessageManager", "List is empty, adding: " + message.getmId());
            list.add(message);
            return list;
        }
        // find an entry with the same mId, replace it and return the list.
        for(BlubbMessage m: list) {
            if(m.getmId().equals(message.getmId())) {
                Log.i("MessageManager", "Found entry replacing: " + message.getmId());
                list.remove(m);
                list.add(message);
                return list;
            }
        }
        // if there was no entry with the id add the message and return the list.
        Log.i("MessageManager", "No entry found in list adding: " + message.getmId());
        list.add(message);
        return list;
    }

    public static List<BlubbMessage> getMessagesForThread(Context context, String tId) {
        Log.i("MessageManager", "getting messages for Thread: " + tId + " from beap and db.");
        BlubbMessage[] beapMessages = new BlubbMessage[0];
        try {
            beapMessages = BlubbComManager.getMessages(context, tId);
        } catch (BlubbDBException e) {
            Log.e("MessageManager", e.getMessage());
        }
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        List<BlubbMessage> dbMessages = databaseHandler.getMessagesForThread(tId);
        BlubbMessage dbM = null;
        for(BlubbMessage bM: beapMessages) {
            dbM = databaseHandler.getMessage(bM.getmId());
            if(dbM == null) {
                bM.setNew(true);
                databaseHandler.addMessage(bM);
                dbMessages = addMessageToList(dbMessages, bM);
            }
            dbM = null;
        }
        return dbMessages;
    }*/
}
