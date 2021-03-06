package com.blubb.alubb.basics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BeapReplyStatus;
import com.blubb.alubb.beapcom.BlubbRequestManager;
import com.blubb.alubb.beapcom.BlubbResponse;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * The SessionManager manages all matters with the beap session.
 * <p/>
 * Created by Benjamin Richter on 23.05.2014.
 */
public class SessionManager {
    /**
     * Name of this class for logging.
     */
    private static final String NAME = "SessionManager";
    /**
     * Preferences name for the message and thread counter.
     */
    private static final String M_COUNT_PREF = "mCount",
            T_COUNT_PREF = "tCount";
    /**
     * Instance of SessionManager to realize the singleton pattern.
     */
    private static SessionManager instance;
    /**
     * Actual SessionInfo object
     */
    private SessionInfo session;
    /**
     * System time when the session will be expired.
     */
    private long timeWhenSessionIsExpired;
    /**
     * Last username and password used for login.
     */
    private String tempUsername, tempPassword;
    /**
     * Counter from Beap for the threads.
     */
    private int tCount;
    /**
     * Counter from Beap for the messages.
     */
    private int mCount;

    /**
     * Private constructor in order to realize the singleton pattern.
     */
    private SessionManager() {
    }

    /**
     * Get the one and only SessionManager object.
     *
     * @return SessionManager instance.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Get the username of the user logged in.
     *
     * @return String containing the actual users username.
     */
    public String getActiveUsername() {
        return tempUsername;
    }

    /**
     * Get the session id currently in use. This will perform a login or session refresh if the
     * session is not valid or will expire soon.
     *
     * @param context The applications context.
     * @return String containing the current session id.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    public synchronized String getSessionID(Context context) throws
            SessionException, BlubbDBConnectionException, PasswordInitException {
        return getSession(context).getSessionId();
    }

    /**
     * Checks for a valid session. If Login preferences are available will log in otherwise
     * will throw a SessionException - let user do sessionLogin which will provide the
     * SessionManager with a SessionInfo-Object.
     *
     * @param context The applications context.
     * @return A up-to-date SessionInfo object.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    private SessionInfo getSession(Context context) throws SessionException,
            BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "getSession(context)");
        // check whether there's a sessionInfo
        if (this.session == null) {
            // if not try a sessionLogin with preferences
            this.session = loginWithPrefs(context);
            int exMinutes = checkSession();
            this.setTimeTillSessionExpires(exMinutes);
        } else if (this.willSessionExpireSoon()) {
            int exMinutes = refreshSession(context);
            this.setTimeTillSessionExpires(exMinutes);
        }
        return this.session;
    }

    /**
     * Preforms a refresh for the current session.
     *
     * @param context The applications context.
     * @return Integer value of the minutes left till this session expires.
     * @throws SessionException           if there are no username or password in the Preferences and
     *                                    user needs to log in manually.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    private int refreshSession(Context context) throws
            SessionException, BlubbDBConnectionException,
            PasswordInitException {
        Log.v(NAME, "refreshSession(context)");
        //execute a refresh with beap
        BlubbResponse blubbResponse = new BlubbRequestManager()
                .refreshSession(this.session.getSessionId());
        //
        BeapReplyStatus status = blubbResponse.getStatus();
        switch (status) {
            case OK: //if status is ok return the result obj - will be a int.
                return (Integer) blubbResponse.getResultObj();
            case LOGIN_REQUIRED:                // if sessionLogin is required:
                this.loginWithPrefs(context);   // try sessionLogin with prefs
                return this.checkSession();
            case CONNECTION_ERROR:
                throw new BlubbDBConnectionException("No connection available.");
            default:
                throw new SessionException("could not refresh session. received status: " +
                        status);
        }
    }

    /**
     * Check whether the session will expire within the next minute.
     *
     * @return True if the session will expire within the next minute.
     */
    private boolean willSessionExpireSoon() {
        Log.v(NAME, "willSessionExpireSoon()");
        long now = System.currentTimeMillis();
        return (this.timeWhenSessionIsExpired < now);
    }

    /**
     * Perform a 'checkSession' at the beap server.
     *
     * @return An integer with the minutes till the session will expire.
     * @throws SessionException           if there are no username or password in the Preferences and
     *                                    user needs to log in manually.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    private int checkSession() throws
            SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "checkSession(context)");
        BlubbResponse blubbResponse =
                new BlubbRequestManager().checkSession(this.session.getSessionId());
        switch (blubbResponse.getStatus()) {
            case OK:
                return (Integer) blubbResponse.getResultObj();
            case LOGIN_REQUIRED:                // if sessionLogin is required:
                if (this.login(tempUsername, tempPassword)) {
                    // since it will just be called when a login has happened there will be un and pw.
                    return this.checkSession();
                } else throw new SessionException("logged in but still need login!????");
            case CONNECTION_ERROR:
                throw new BlubbDBConnectionException("No connection available.");
            default:
                throw new SessionException("could not check session. received status: " +
                        blubbResponse.getStatus());
        }
    }

    /**
     * Make a login and check whether it was successful.
     *
     * @param username String containing the username to log in.
     * @param password String containing the password of the user.
     * @return True if the login was successful.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    public synchronized boolean login(String username, String password)
            throws SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "login(username = " + username +
                ", passwordLength = " + password.length() + ")");
        // make the sessionLogin
        this.session = sessionLogin(username, password);
        // if there has been returned a session return true
        return (this.session != null);
    }

    /**
     * Tries to log in to beap with username and password from Preferences.
     *
     * @param appContext the blubb application context.
     * @return The sessionInfo from the sessionLogin
     * @throws SessionException           if there are no username or password in the Preferences and
     *                                    user needs to log in manually.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    private SessionInfo loginWithPrefs(Context appContext) throws
            SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "loginWithPrefs(appContext)");
        // try a sessionLogin with preferences
        // first get Prefs.
        String unPref = appContext.getString(R.string.pref_username),
                pwPref = appContext.getString(R.string.pref_password);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String username = prefs.getString(unPref, "NULL"),
                password = prefs.getString(pwPref, "NULL");
        Log.v(NAME, "unPref = " + username + "  -  pwPref = " + password);
        // if there are no prefs throw a exception - it's not possible to get
        // a session without the username or password -> user needs to log in manually.
        if (username.equals("NULL") || password.equals("NULL")) {
            throw new SessionException("no valid login parameter available. " +
                    "Please log in first.");
        } else {
            // there are a username and password so try to log in - there will be
            // exceptions if something is wrong.
            return this.sessionLogin(username, password);
        }
    }

    /**
     * Makes a login at the beap server.
     *
     * @param username String containing the username to log in.
     * @param password String containing the password of the user.
     * @return SessionInfo object containing all information to the session.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    private SessionInfo sessionLogin(String username, String password) throws
            SessionException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "sessionLogin(username = " + username +
                ", password.length = " + password.length());
        BlubbResponse blubbResponse = new BlubbRequestManager().login(username, password);
        switch (blubbResponse.getStatus()) {
            case OK:
                // if it's ok the username and password will be stored temporarily for
                // future sessionLogin.
                this.tempUsername = username;
                Log.v(NAME, "Set tempUsername to: " + tempUsername);
                this.tempPassword = password;
                Log.v(NAME, "Length of tempPassword: " + tempPassword.length());
                return blubbResponse.getSessionInfo();
            case CONNECTION_ERROR:
                throw new BlubbDBConnectionException("No connection available.");
            case PASSWORD_INIT:
                throw new PasswordInitException();
            default:
                throw new SessionException("Could not perform sessionLogin with prefs. " +
                        "Beap status: " + blubbResponse.getStatus());
        }
    }

    /**
     * Set the time when the session will expire due to a sessionCheck at beap minus one minute
     * as buffer to be able to refresh the session.
     *
     * @param time Minutes till the session will expire due to a sessionCheck at the beap server.
     */
    private void setTimeTillSessionExpires(int time) {
        Log.v(NAME, "setTimeTillSessionExpires(minutes = " + time + ")");
        long now = System.currentTimeMillis();
        now += (time - 1) * 60 * 1000;
        this.timeWhenSessionIsExpired = now;
    }

    /**
     * Make a quickCheck at the beap server and see whether there are new threads or messages
     * available. This will automatically load the new messages or threads.
     *
     * @param context The applications context.
     * @return QuickCheck object containing two lists with threads and messages which are new.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBException           if the BeapReplyStatus was not 'OK' or the received json was not
     *                                    the kind expected or
     *                                    if the value of the json array for messages within the blubbResponse
     *                                    from the beap server doesn't exist or is not a {@code JSONObject}.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    public synchronized QuickCheck quickCheck(Context context) throws SessionException,
            BlubbDBException, BlubbDBConnectionException,
            PasswordInitException {
        loadCounter(context);
        Log.v(NAME, "quickCheck(context)");
        List<BlubbThread> threads = new ArrayList<BlubbThread>();
        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        // get the quickCheck from beap.
        int[] check = check(context);
        if (check[1] > mCount) {
            Log.v(NAME, "QuickCheck " + (check[1] - mCount) + " new messages.");
            messages = MessageManager.getInstance().getNewMessagesFromAllThreads(context);
            mCount = check[1];
            storeCounter(context, M_COUNT_PREF, mCount);
        }
        if (check[0] > tCount) {
            Log.v(NAME, "QuickCheck " + (check[0] - tCount) + " new threads.");
            threads = ThreadManager.getInstance().getNewThreads(context);
            tCount = check[0];
            storeCounter(context, T_COUNT_PREF, tCount);
        }

        return new QuickCheck(threads, messages);
    }

    /**
     * Load the counter for messages and threads from the SharedPreferences if they are not
     * available in the preferences they will be set to 0.
     *
     * @param context The applications context.
     */
    private void loadCounter(Context context) {
        if ((mCount == 0) || (tCount == 0)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            mCount = prefs.getInt(M_COUNT_PREF, 0);
            tCount = prefs.getInt(T_COUNT_PREF, 0);
            Log.i(NAME, "mcount-prefs: " + mCount + " tcount-prefs: " + tCount);
        }
    }

    /**
     * Store a counter value to the shared prefs.
     *
     * @param context  The applications context.
     * @param prefName Name of the preference or the key.
     * @param counter  Value to store in the preferences.
     */
    private void storeCounter(Context context, String prefName, int counter) {
        Log.v(NAME, "storeCounter(context, prefName=" + prefName +
                ", counter=" + counter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(prefName, counter);
        editor.commit();
    }

    /**
     * Makes the quickCheck at the beapDB.
     *
     * @param context The applications context.
     * @return The raw response array. First value will be the counter of threads, second the
     * counter of messages.
     * @throws SessionException           if it was not possible to log in, probably the username or password
     *                                    is wrong.
     * @throws BlubbDBException           if the BeapReplyStatus was not 'OK' or the received json was not
     *                                    the kind expected.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws PasswordInitException      if the password is 'init' and the user must set his own pw.
     */
    private int[] check(Context context) throws SessionException,
            BlubbDBException, BlubbDBConnectionException, PasswordInitException {
        Log.v(NAME, "check(context)");
        try {
            String query = "tree.functions.quickCheck(self)";
            BlubbResponse blubbResponse =
                    new BlubbRequestManager().query(query, getSessionID(context));
            switch (blubbResponse.getStatus()) {
                case OK:
                    // with status ok result object will be a json array.
                    JSONArray array = (JSONArray) blubbResponse.getResultObj();
                    return new int[]{
                            (Integer) array.get(0),
                            (Integer) array.get(1)};
                case CONNECTION_ERROR:
                    throw new BlubbDBConnectionException("No connection available.");
                default:
                    throw new BlubbDBException("Could not perform quickCheck" +
                            " Beap status: " + blubbResponse.getStatus());
            }
        } catch (JSONException e) {
            throw new BlubbDBException("Received wrong Json object for quickCheck query.");
        }
    }

    /**
     * Reset the password for the beap server.
     *
     * @param username           String containing the username to log in.
     * @param oldPassword        String containing the old password that will be replaced.
     * @param newPassword        String containing the new password.
     * @param confirmNewPassword String containing the new password again to ensure that it
     *                           has no typo.
     * @return True if the password has been reset successfully.
     * @throws BlubbDBConnectionException if it's not possible to get a connection to the server.
     *                                    Probably there's no wifi or network connection or the
     *                                    server is offline.
     * @throws InvalidParameterException  if there was a parameter error.
     */
    public synchronized boolean resetPassword(String username, String oldPassword, String newPassword,
                                              String confirmNewPassword)
            throws BlubbDBConnectionException, InvalidParameterException {
        BlubbResponse response = new BlubbRequestManager().resetPassword(
                username, oldPassword, newPassword, confirmNewPassword);
        Log.v(NAME, "resetPassword(un, oldPW, newPW, confirmPW)");
        switch (response.getStatus()) {
            case OK:
                return true;
            case CONNECTION_ERROR:
                throw new BlubbDBConnectionException("No connection available.");
            case PARAMETER_ERROR:
                throw new InvalidParameterException();
            default:
                return false;
        }
    }

    /**
     * Get the id of the currently logged in user, e.g. to identify the users own messages.
     *
     * @param context The applications context.
     * @return String with the users unique id from the current SessionInfo or the stored username.
     */
    public synchronized String getUserId(Context context) {
        if (this.session != null) return this.session.getBlubbUser();
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String unId = context.getString(R.string.pref_username);
            return prefs.getString(unId, "noUser");
        }
    }

    /**
     * Log out from the beap server and delete the password from the preferences though no service
     * can log in.
     *
     * @param context The applications context.
     */
    public synchronized void logout(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.edit().remove(context.getString(R.string.pref_password)).commit();
        if (this.session != null) {
            new AsyncLogout().execute(session.getSessionId());
        }

    }

    /**
     * AsyncTask to log out from the beap server.
     */
    private class AsyncLogout extends AsyncTask<String, Void, BlubbResponse> {

        @Override
        protected BlubbResponse doInBackground(String... params) {
            return new BlubbRequestManager().logout(params[0]);
        }
    }
}
