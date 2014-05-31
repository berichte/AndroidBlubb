package com.blubb.alubb.basics;

import android.content.Context;
import android.util.Log;

import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.blubexceptions.BlubbDBException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benni on 31.05.2014.
 */
public class MessageManager {

    private static int msgAtBeap = 0;

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
    }
}
