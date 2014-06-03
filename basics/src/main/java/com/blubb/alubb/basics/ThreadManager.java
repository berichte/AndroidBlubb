package com.blubb.alubb.basics;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.blubb.alubb.beapcom.BPC;
import com.blubb.alubb.beapcom.BlubbRequestManager;
import com.blubb.alubb.beapcom.BlubbResponse;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richt_000 on 24.05.2014.
 */
public class ThreadManager {
    private static final String NAME = "ThreadManager";

    private static int threadsAtBeap = 0;
    private static ThreadManager tManager;

    private List<BlubbThread> loadedThreads = new ArrayList<BlubbThread>();

    private ThreadManager() {

    }

    public static ThreadManager getInstance() {
        if(tManager == null) tManager = new ThreadManager();
        return tManager;
    }

    public List<BlubbThread> getNewThreads(Context context)
            throws InvalidParameterException, SessionException, BlubbDBException, JSONException {
        List<BlubbThread> beapThreads = this.getAllThreadsFromBeap(context);
        this.getAllThreadsFromSqlite(context);
        beapThreads.removeAll(this.loadedThreads);

        for (BlubbThread t: beapThreads) {
            t.setNew(true);
        }
        for(BlubbThread t: loadedThreads) {
            if(t.isNew()) beapThreads.add(t);
        }
        return beapThreads;
    }

    public List<BlubbThread> getAllThreadsFromBeap(Context context)
            throws InvalidParameterException, SessionException, BlubbDBException, JSONException {
        List<BlubbThread> beapThreads = new ArrayList<BlubbThread>();
        String query = "tree.functions.getAllThreads(self)";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        switch (response.getStatus()) {
            case OK:
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                for(int i = 0; i < jsonArray.length(); i++) {
                    BlubbThread t = new BlubbThread(jsonArray.getJSONObject(i));
                    beapThreads.add(t);
                }
                return beapThreads;
            default:
                throw new BlubbDBException("Could not get Threads from beap " +
                        "Response status: " + response.getStatus());
        }
    }

    public List<BlubbThread> getAllThreadsFromSqlite(Context context) {
        if(loadedThreads == null) {
            DatabaseHandler databaseHandler = new DatabaseHandler(context);
            this.loadedThreads = databaseHandler.getAllThreads();
        }
        return this.loadedThreads;
    }

    public BlubbThread getThread(Context context, String tId)
            throws SessionException, InvalidParameterException, BlubbDBException, JSONException {
        List<BlubbThread> threads = getAllThreadsFromSqlite(context);
        BlubbThread thread = getThread(threads, tId);
        if(thread != null) return thread;
        threads = getAllThreadsFromBeap(context);
        return getThread(threads, tId);
    }

    private BlubbThread getThread(List<BlubbThread> threads, String tId) {
        for (BlubbThread t: threads) {
            if(t.gettId().equals(tId)) return t;
        }
        return null;
    }


    /**
     * creates a new Thread at beap. if all goes well the thread will
     * be added to the sqlite db and the hole BlubbThread will be returned.
     *
     * @param context actual context.
     * @param tTitle title for the thread
     * @param tDescription description for the thread
     * @return the BlubbThread returned from Beap
     * @throws InvalidParameterException if title or desc are not valid.
     * @throws BlubbDBException
     * @throws SessionException if it's not possible to log in
     * @throws JSONException if there should be returned a wrong kind of result.
     */
    public BlubbThread createThread(
            Context context, String tTitle, String tDescription)
            throws InvalidParameterException, BlubbDBException,
            SessionException, JSONException {
        // first parse the incoming strings to tTitle -> "tTitle"
        // tDesc -> "bla bla //n bla bla"
        tTitle = BPC.parseStringParameterToDB(tTitle);
        tDescription = BPC.parseStringParameterToDB(tDescription);
        // make query and get session id
        String query = "tree.functions.createThread(self, " +
                tTitle + ", " + tDescription + ")";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        // execute query
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        switch (response.getStatus()) {
            // when OK there will be a thread obj as json obj in response
            // make a BlubbThread and add it to db and loaded Threads.
            case OK:
                JSONObject result = (JSONObject) response.getResultObj();
                BlubbThread thread = new BlubbThread(result);
                DatabaseHandler db = new DatabaseHandler(context);
                db.addThread(thread);
                this.loadedThreads.add(thread);
                return thread;
            default:
                throw new BlubbDBException("Could not perform createThread" +
                        " Beap status: " + response.getStatus());
        }
    }


    /*
    public boolean newThreadsAvailable(Context context)
            throws InvalidParameterException, SessionException, BlubbDBException {
        int quickCheckThreads = 0;
        quickCheckThreads = BlubbRequestManager.quickCheck(context)[0];
        return newThreadsAvailable(quickCheckThreads);
    }

    public boolean newThreadsAvailable(int threadCountFromBeap) {
        if(threadsAtBeap < threadCountFromBeap) {
            return true;
        } else {
            Log.i("ThreadManager", "getting Threads only from the sqlite DB.");
            return false;
        }
    }
    public static int getThreadsAtBeap() {
        return threadsAtBeap;
    }

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

    public static List<BlubbThread> getAllThreads(Context context) {
        int quickCheckThreads = 0;
        try {
            int[] qC = BlubbComManager.quickCheck(context);
            if(qC.length==0) {
                quickCheckThreads = 0;
            } else {
                quickCheckThreads = qC[0];
            }
        } catch (BlubbDBException e) {
            Log.e("ThreadManager", e.getMessage());
            DatabaseHandler databaseHandler = new DatabaseHandler(context);
            return databaseHandler.getAllThreads();
        }
        if(threadsAtBeap < quickCheckThreads) {
            threadsAtBeap = quickCheckThreads;
            return getThreads(context);
        } else {
            Log.i("ThreadManager", "getting Threads only from the sqlite DB.");
            DatabaseHandler databaseHandler = new DatabaseHandler(context);
            return databaseHandler.getAllThreads();
        }
    }

    private static List<BlubbThread> getThreads(Context context)  {
        Log.i("ThreadManager", "getting threads from beap and db.");
        BlubbThread[] beapThreads = new BlubbThread[0];

        beapThreads = BlubbRequestManager.//TODO hier weiter machen

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
                }
                dbThreads = addThreadToList(dbThreads, dbT);
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
    }*/
}
