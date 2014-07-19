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
import java.util.List;

/**
 * A Singleton class managing the access to BlubbThreads.
 * It's the bridge for threads between user interface, beapDB and the local sqlite database.
 * If new threads are available at beapDB the local sqlite database will be updated automatically.
 * (Threads in this context always mean BlubbThreads not the java.lang.Thread class)
 * <p/>
 * Created by Benjamin Richter on 24.05.2014.
 */
public class ThreadManager {
    /**
     * Name for logging purposes.
     */
    private static final String NAME = "ThreadManager";

    /**
     * Instance of the ThreadManager to realize the singleton pattern.
     */
    private static ThreadManager tManager;

    /**
     * Private constructor to realize the singleton pattern.
     */
    private ThreadManager() {
        Log.v(NAME, "Creating ThreadManager.");
    }

    /**
     * Get the one and only ThreadManager instance,
     * easy accessible through the ThreadManager class.
     *
     * @return The ThreadManager object.
     */
    public static ThreadManager getInstance() {
        Log.v(NAME, "get Instance called.");
        if (tManager == null) tManager = new ThreadManager();
        return tManager;
    }

    /**
     * Gets first the threads from beapDB and updates the sqlite database then gets all threads
     * from sqlite.
     *
     * @param context The applications context.
     * @return List of all available BlubbThreads.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBException           if the response status is not 'OK'.
     * @throws JSONException              if the value of the json array for threads within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     */
    public List<BlubbThread> getAllThreads(Context context)
            throws BlubbDBException, BlubbDBConnectionException, SessionException, JSONException {
        this.getAllThreadsFromBeap(context);
        return this.getAllThreadsFromSqlite(context);
    }

    /**
     * Get all threads with the 'isNew' tag set to true.
     *
     * @param context The applications context.
     * @return List of BlubbThreads the user has not seen yet.
     */
    public List<BlubbThread> getNewThreads(Context context) {
        Log.v(NAME, "getNewThreads(context)");
        List<BlubbThread> threads;
        try {
            threads = getAllThreads(context);
        } catch (BlubbDBException e) {
            threads = getAllThreadsFromSqlite(context);
            Log.e(NAME, e.getClass().getName() + ": " + e.getMessage());
        } catch (BlubbDBConnectionException e) {
            threads = getAllThreadsFromSqlite(context);
            Log.e(NAME, e.getClass().getName() + ": " + e.getMessage());
        } catch (SessionException e) {
            threads = getAllThreadsFromSqlite(context);
            Log.e(NAME, e.getClass().getName() + ": " + e.getMessage());
        } catch (JSONException e) {
            threads = getAllThreadsFromSqlite(context);
            Log.e(NAME, e.getClass().getName() + ": " + e.getMessage());
        }
        List<BlubbThread> newThreads = new ArrayList<BlubbThread>();
        for (BlubbThread t : threads) {
            if (t.isNew()) newThreads.add(t);
        }
        return newThreads;
    }

    /**
     * Get all threads from the beapDB.
     *
     * @param context The applications context.
     * @return List of BlubbThreads instantly from the beapDB server.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBException           if the response status is not 'OK'.
     * @throws JSONException              if the value of the json array for threads within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     */
    public List<BlubbThread> getAllThreadsFromBeap(Context context)
            throws SessionException, BlubbDBException,
            JSONException, BlubbDBConnectionException {
        Log.v(NAME, "getAllThreadsFromBeap(context)");
        List<BlubbThread> beapThreads = new ArrayList<BlubbThread>();
        String query = "tree.functions.getAllThreads(self)";
        String sessionId = null;
        try {
            sessionId = SessionManager.getInstance().getSessionID(context);
        } catch (PasswordInitException e) {
            Log.e(NAME, e.getMessage() + " can not happen at this point!");
        }
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        switch (response.getStatus()) {
            case OK:
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                for (int i = 0; i < jsonArray.length(); i++) {
                    BlubbThread t = new BlubbThread(jsonArray.getJSONObject(i));
                    putThreadToSqliteFromBeap(context, t);
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

    /**
     * Put a thread to the sqlite database, either this creates a new or updates an existing entry.
     * This updates not the isNew or hasNewEntries fields.
     *
     * @param context The applications context.
     * @param thread  The BlubbThread object which will be stored at the sqlite database.
     */
    private void putThreadToSqliteFromBeap(Context context, BlubbThread thread) {
        Log.v(NAME, "putThreadToSqlite(context, thread=" + thread.gettId() + ")");
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbThread t = db.getThread(thread.gettId());
        if (t != null) {
            if (!thread.equals(t)) {
                if (thread.gettMsgCount() > t.gettMsgCount()) thread.setHasNewMsgs(true);
                db.updateThreadFromBeap(thread);
            }
        } else {
            thread.setNew(true);
            db.addThread(thread);
        }
    }

    /**
     * Put a thread to the sqlite database, either this creates a new or updates an existing entry.
     *
     * @param context The applications context.
     * @param thread  The BlubbThread object which will be stored at the sqlite database.
     */
    private void putThreadToSqlite(Context context, BlubbThread thread) {
        Log.v(NAME, "putThreadToSqlite(context, thread=" + thread.gettId() + ")");
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbThread t = db.getThread(thread.gettId());
        if (t != null) {
            if (!thread.equals(t)) {
                db.updateThread(thread);
            }
        } else {
            db.addThread(thread);
        }
    }

    /**
     * Get all the threads stored on the local sqlite database.
     *
     * @param context The applications context.
     * @return List of BlubbThreads from the sqlite database.
     */
    public List<BlubbThread> getAllThreadsFromSqlite(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        Log.v(NAME, "getAllThreadsFromSqlite(context)");
        List<BlubbThread> list = db.getAllThreads();
        db.close();
        return list;
    }

    /**
     * Get a single thread either from sqlite or if not available from the beapDB.
     *
     * @param context The applications context.
     * @param tId     String containing the thread id of the thread needed.
     * @return BlubbThread object according to the tId.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBException           if the response status is not 'OK'.
     * @throws JSONException              if the value of the json array for threads within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     */
    public BlubbThread getThread(Context context, String tId)
            throws SessionException, BlubbDBException,
            JSONException, BlubbDBConnectionException {
        Log.v(NAME, "getThreadFromList(context, tId");
        List<BlubbThread> threads = getAllThreadsFromSqlite(context);
        BlubbThread thread = getThreadFromList(threads, tId);
        if (thread != null) return thread;
        threads = getAllThreadsFromBeap(context);
        return getThreadFromList(threads, tId);
    }

    /**
     * Get a thread according to it's thread id from a list of threads.
     *
     * @param threads List of BlubbThreads.
     * @param tId     String containing the thread id of the thread needed.
     * @return BlubbThread object according to the tId.
     */
    private BlubbThread getThreadFromList(List<BlubbThread> threads, String tId) {
        Log.v(NAME, "getThreadFromList(list, tId");
        for (BlubbThread t : threads) {
            if (t.gettId().equals(tId)) {
                Log.v(NAME, "Found Thread: " + tId);
                return t;
            }
        }
        Log.w(NAME, "Could not find Thread: " + tId + " in list.");
        return null;
    }

    /**
     * Creates a new Thread at beap. If everything goes well the thread will
     * be added to the sqlite db and the whole new BlubbThread object will be returned.
     *
     * @param context      The applications context.
     * @param tTitle       String containing the title for the thread.
     * @param tDescription String containing the description for the thread.
     * @return The created BlubbThread.
     * @throws BlubbDBException if the response status is not 'OK'.
     * @throws SessionException if it was not possible to log in, probably the username or password
     *                          is wrong.
     */
    public BlubbThread createThread(
            Context context, String tTitle, String tDescription)
            throws BlubbDBException,
            SessionException, BlubbDBConnectionException {
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
        String sessionId = null;
        try {
            sessionId = SessionManager.getInstance().getSessionID(context);
        } catch (PasswordInitException e) {
            Log.e(NAME, e.getMessage() + " can not happen at this point!");
        }
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
                return thread;
            default:
                throw new BlubbDBException("Could not perform createThread" +
                        " Beap status: " + response.getStatus());
        }
    }

    /**
     * Call this if a thread is shown on the user interface and the thread is not longer a new one.
     *
     * @param context  The applications context.
     * @param threadId String with the threads id.
     */
    public void readingThread(Context context, String threadId) {
        DatabaseHandler db = new DatabaseHandler(context);
        BlubbThread thread = db.getThread(threadId);
        thread.setNew(false);
        thread.setHasNewMsgs(false);
        db.updateThread(thread);
    }

    /**
     * Updates the thread at beapDB and the sqlite database.
     *
     * @param context The applications context.
     * @param thread  The BlubbThread object which will be updated.
     * @return
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBException           if the response status is not 'OK'.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     */
    public Boolean setThread(Context context, BlubbThread thread)
            throws BlubbDBException, PasswordInitException,
            SessionException, BlubbDBConnectionException {

        String tId = BPC.parseStringParameterToDB(thread.gettId());
        String tTitle = BPC.parseStringParameterToDB(thread.getThreadTitle());
        String tDesc = BPC.parseStringParameterToDB(thread.gettDesc());
        String tStatus = BPC.parseStringParameterToDB(thread.gettStatusString());
        String query = "tree.functions.setThread(self," + tId + "," + tTitle + "," + tDesc
                + "," + tStatus + ")";

        String sessionId = SessionManager.getInstance().getSessionID(context);
        BlubbResponse response = BlubbRequestManager.query(query, sessionId);
        Log.v(NAME, "Executed query: " + query + " with response status: " + response.getStatus());
        switch (response.getStatus()) {
            case OK:
                JSONObject result = (JSONObject) response.getResultObj();
                BlubbThread changedThread = new BlubbThread(result);
                DatabaseHandler db = new DatabaseHandler(context);
                db.updateThread(changedThread);
                return true;
            default:
                throw new BlubbDBException("Could not perform setMsg" +
                        " Beap status: " + response.getStatus());
        }

    }
}
