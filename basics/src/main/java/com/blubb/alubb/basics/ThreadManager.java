package com.blubb.alubb.basics;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.blubb.alubb.beapcom.BPC;
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
import java.util.List;

/**
 * Created by Benjamin Richter on 24.05.2014.
 */
public class ThreadManager {
    private static final String NAME = "ThreadManager";

    private static int threadsAtBeap = 0;
    private static ThreadManager tManager;

    private List<BlubbThread> loadedThreads = new ArrayList<BlubbThread>();

    private ThreadManager() {
        Log.v(NAME, "Creating ThreadManager.");
    }

    public static ThreadManager getInstance() {
        Log.v(NAME, "get Instance called.");
        if(tManager == null) tManager = new ThreadManager();
        return tManager;
    }

    public List<BlubbThread> getNewThreads(Context context)
            throws InvalidParameterException, SessionException, BlubbDBException,
            JSONException, BlubbDBConnectionException {
        Log.v(NAME, "getNewThreads(context)");
        List<BlubbThread> beapThreads = this.getAllThreadsFromBeap(context);
        Log.v(NAME, "got Threads from beap, ListSize: " + beapThreads.size());
        this.getAllThreadsFromSqlite(context);
        Log.v(NAME, "got Threads from sqlite db, ListSize: " + this.loadedThreads.size());
        beapThreads.removeAll(this.loadedThreads);
        for (BlubbThread t: beapThreads) {
            t.setNew(true);
        }
        for(BlubbThread t: loadedThreads) {
            if(t.isNew()) beapThreads.add(t);
        }
        Log.i(NAME, "final size of newThreadList: " + beapThreads.size());
        return beapThreads;
    }

    public List<BlubbThread> getAllThreadsFromBeap(Context context)
            throws InvalidParameterException, SessionException, BlubbDBException,
            JSONException, BlubbDBConnectionException {
        Log.v(NAME, "getAllThreadsFromBeap(context)");
        List<BlubbThread> beapThreads = new ArrayList<BlubbThread>();
        String query = "tree.functions.getAllThreads(self)";
        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        switch (response.getStatus()) {
            case OK:
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                for(int i = 0; i < jsonArray.length(); i++) {
                    BlubbThread t = new BlubbThread(jsonArray.getJSONObject(i));
                    putThreadToSqlite(context, t);
                    beapThreads.add(t);
                }
                Log.i(NAME, "received " + beapThreads.size() + " threads from beap.");
                return beapThreads;
            default:
                Log.e(NAME, "received no valid response from beap, status: " + response.getStatus());
                throw new BlubbDBException("Could not get Threads from beap " +
                        "Response status: " + response.getStatus());
        }
    }

    private void putThreadToSqlite(Context context, BlubbThread thread) {
        Log.v(NAME, "putThreadToSqlite(context, message = " + thread.gettId() + ")");
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbThread t = db .getThread(thread.gettId());
        if(t != null) {
            if(!thread.equals(t)){
                db.updateThread(thread);
            }
        } else {
            db.addThread(thread);
        }
    }

    public List<BlubbThread> getAllThreadsFromSqlite(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        Log.v(NAME, "getAllThreadsFromSqlite(context)");
        if(loadedThreads.size() < db.getThreadCount()) {
            Log.v(NAME, "Have no threads saved temporary, get it from sqlite.");
            this.loadedThreads = db.getAllThreads();
            Log.v(NAME, "Received " + this.loadedThreads.size() + " threads from sqlite.");
        }
        Log.v(NAME, "Have " + loadedThreads.size() + " to return from sqlite.");
        db.close();
        return this.loadedThreads;
    }

    public BlubbThread getThread(Context context, String tId)
            throws SessionException, InvalidParameterException, BlubbDBException,
            JSONException, BlubbDBConnectionException {
        Log.v(NAME, "getThread(context, tId");
        List<BlubbThread> threads = getAllThreadsFromSqlite(context);
        BlubbThread thread = getThread(threads, tId);
        if(thread != null) return thread;
        threads = getAllThreadsFromBeap(context);
        return getThread(threads, tId);
    }

    private BlubbThread getThread(List<BlubbThread> threads, String tId) {
        Log.v(NAME, "getThread(list, tId");
        for (BlubbThread t: threads) {
            if(t.gettId().equals(tId)) {
                Log.v(NAME, "Found Thread: " + tId);
                return t;
            }
        }
        Log.w(NAME, "Could not find Thread: " + tId + " in list.");
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
            SessionException, JSONException, BlubbDBConnectionException {
        Log.v(NAME, "createThread(context, tTitle = " +
                tTitle + ", tDescription = " + tDescription + ")");
        // first parse the incoming strings to tTitle -> "tTitle"
        // tDesc -> "bla bla //n bla bla"
        tTitle = BPC.parseStringParameterToDB(tTitle);
        tDescription = BPC.parseStringParameterToDB(tDescription);
        // make query and get session id
        String query = "tree.functions.createThread(self," +
                tTitle + "," + tDescription + ")";
        Log.i(NAME, "Executing query: " + query);
        String sessionId = SessionManager.getInstance().getSessionID(context);
        Log.v(NAME, "Received sessionId: " + sessionId);
        // execute query
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        Log.i(NAME, "Received response - Status: " + response.getStatus());
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
}
