package com.blubb.alubb.basics;

import android.content.Context;
import android.util.Log;

import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richt_000 on 24.05.2014.
 */
public class ThreadManager {

    public static BlubbThread getThread(Context context, String tId) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        return  databaseHandler.getThread(tId);
    }

    private static List<BlubbThread> addThreadToList(
            List<BlubbThread> list, BlubbThread blubbThread) {
        if(list.isEmpty()) {
            list.add(blubbThread);
            return list;
        }
        for(BlubbThread bt: list) {
            if(bt.gettId().equals(blubbThread.gettId())) {
                list.remove(bt);
                list.add(blubbThread);
                return list;
            }
        }
        list.add(blubbThread);
        return list;
    }

    public static List<BlubbThread> getAllThreads(Context context)  {
        BlubbThread[] beapThreads = new BlubbThread[0];
        try {
            beapThreads = BlubbComManager.getAllThreads(context);
        } catch (BlubbDBException e) {
            Log.e("ThreadManager", e.getMessage());
        } catch (BlubbDBConnectionException e) {
            Log.e("ThreadManager", e.getMessage());
        }
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        List<BlubbThread> dbThreads = databaseHandler.getAllThreads();
        BlubbThread dbT = null;
        for(BlubbThread bt: beapThreads) {
            dbT = databaseHandler.getThread(bt.gettId());
            if(dbT == null) {
                bt.setNew(true);
                if(bt.gettMsgCount()>0) bt.setHasNewMsgs(true);
                databaseHandler.addThread(bt);
                addThreadToList(dbThreads, bt);
            } else {
                if(bt.gettMsgCount()>dbT.gettMsgCount()) {
                    dbT.setHasNewMsgs(true);
                    addThreadToList(dbThreads, dbT);
                }
            }
            dbT = null;
        }
        return dbThreads;
    }

    public static List<BlubbThread> getThreadsWithNewMsg(Context context) {
        List<BlubbThread> result = getAllThreads(context);
        for (int i = 0; i < result.size(); i++) {
            if(!result.get(i).isNew()) result.remove(i);
        }
        return result;
    }
}
