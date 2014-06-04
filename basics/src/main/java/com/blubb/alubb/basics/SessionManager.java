package com.blubb.alubb.basics;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BlubbDBReplyStatus;
import com.blubb.alubb.beapcom.BlubbRequestManager;
import com.blubb.alubb.beapcom.BlubbResponse;
import com.blubb.alubb.beapcom.QuickCheck;
import com.blubb.alubb.blubbbasics.BlubbApplication;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin Richter on 23.05.2014.
 */
public class SessionManager {
    /** Name of this class for logging */
    private static final String NAME = "SessionManager";
    /** actual SessionInfo object */
    private SessionInfo session;
    /** Instance of SessionManager - for Singleton pattern. */
    private static SessionManager instance;
    /** System time in long when the session will be expired. */
    private long timeWhenSessionIsExpired;
    /** username and password used last for login. */
    private String tempUsername, tempPassword;
    /** Counter from Beap for the threads. */
    private int tCount;
    /** Counter from Beap for the messages. */
    private int mCount;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getSessionID(Context context) throws InvalidParameterException,
            SessionException, BlubbDBException, BlubbDBConnectionException {
        return getSession(context).getSessionId();
    }

    /**
     * Checks for a valid session. If Login preferences are available will log you in otherwise
     * will throw a SessionException - let user do sessionLogin then and provide the SessionManager
     * with a SessionInfo-Object
     * @param context Android-Context for this
     * @return valid SessionId string for Beap.
     * @throws SessionException if no Session is available
     */
    private SessionInfo getSession(Context context) throws SessionException,
            InvalidParameterException, BlubbDBException, BlubbDBConnectionException {
        Log.v(NAME, "getSession(context)");
        // check whether there's a sessionInfo
        if(this.session == null) {
            // if not try a sessionLogin with preferences
            this.session = loginWithPrefs(context);
            int exMinutes = checkSession(context);
            this.setTimeTillSessionExpires(exMinutes);
        } else if (this.willSessionExpireSoon()){
            int exMinutes = refreshSession(context);
            this.setTimeTillSessionExpires(exMinutes);
        }
        return this.session;
    }

    private int refreshSession(Context context) throws BlubbDBException,
            InvalidParameterException, SessionException, BlubbDBConnectionException {
        Log.v(NAME, "refreshSession(context)");
        //execute a refresh with beap
        BlubbResponse blubbResponse = BlubbRequestManager
                .refreshSession(this.session.getSessionId());
        //
        BlubbDBReplyStatus status = blubbResponse.getStatus();
        switch (status) {
            case OK: //if status is ok return the result obj - will be a int.
                return (Integer) blubbResponse.getResultObj();
            case LOGIN_REQUIRED:                // if sessionLogin is required:
                this.loginWithPrefs(context);   // try sessionLogin with prefs
                return this.checkSession(context);
            default:
                throw new SessionException("could not refresh session. received status: " +
                        status);
        }
    }

    private boolean willSessionExpireSoon() {
        Log.v(NAME, "willSessionExpireSoon()");
        long now = System.currentTimeMillis();
        return (this.timeWhenSessionIsExpired < now);
    }

    private int checkSession(Context context) throws InvalidParameterException,
            SessionException, BlubbDBException, BlubbDBConnectionException {
        Log.v(NAME, "checkSession(context)");
        BlubbResponse blubbResponse =
                BlubbRequestManager.checkSession(context, this.session.getSessionId());
        switch (blubbResponse.getStatus()) {
            case OK:
                return (Integer) blubbResponse.getResultObj();
            case LOGIN_REQUIRED:                // if sessionLogin is required:
                if(this.login(tempUsername, tempPassword)) { // since it will just be called when a login has happened there will be un and pw.
                return this.checkSession(context);
            } else throw new SessionException("logged in but still need login!????");
            default:
                throw new SessionException("could not check session. received status: " +
                        blubbResponse.getStatus());
        }
    }

    public boolean login(String username, String password)
            throws InvalidParameterException, BlubbDBException,
            SessionException, BlubbDBConnectionException {
        Log.v(NAME, "login(usename = " + username +
                ", passwordLength = " + password.length() + ")");
        // make the sessionLogin
        this.session = sessionLogin(username, password);
        // if there has been returned a session return true
        return (this.session != null);
    }

    /**
     * Trys to log in to beap with username and password from Preferences.
     * @param appContext
     * @return the sessionInfo from the sessionLogin
     * @throws InvalidParameterException if prefUsername or prefPassword are not valid.
     * @throws BlubbDBException if something went wrong with the DB
     * @throws SessionException if there are no username or password in the Preferences -
     * user needs to log in manually.
     */
    private SessionInfo loginWithPrefs(Context appContext) throws InvalidParameterException,
            BlubbDBException, SessionException,BlubbDBConnectionException {
        Log.v(NAME, "loginWithPrefs(appContext)");
        // try a sessionLogin with preferences
        // first get Prefs.
        String unPref = appContext.getString(R.string.pref_username),
                pwPref = appContext.getString(R.string.pref_password),
                prefName = appContext.getString(R.string.preferences_name);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String username = prefs.getString(unPref, "NULL"),
                password = prefs.getString(pwPref, "NULL");
        Log.v(NAME, "unPref = " + username + "  -  pwPref = " + password);
        // if there are no prefs throw a exception - it's not possible to get
        // a session without the username or password -> user needs to log in manually.
        if(username.equals("NULL") || password.equals("NULL")) {
            throw new SessionException("no valid login parameter available. " +
                    "Please log in first.");
        } else {
            // there are a username and password so try to log in - there will be
            // exceptions if something is wrong.
            return this.sessionLogin(username, password);
        }
    }

    private SessionInfo sessionLogin(String username, String password) throws BlubbDBException,
            SessionException, BlubbDBConnectionException {
        Log.v(NAME, "sessionLogin(username = " + username +
                ", password.length = " + password.length());
        BlubbResponse blubbResponse = BlubbRequestManager.login(username, password);
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
            default:
                throw new SessionException("Could not perform sessionLogin with prefs. " +
                        "Beap status: " + blubbResponse.getStatus());
        }
    }

    private void setTimeTillSessionExpires(int time) {
        Log.v(NAME, "setTimeTillSessionExpires(minutes = " + time + ")");
        long now = System.currentTimeMillis();
        now += (time-1)*60*1000;
        this.timeWhenSessionIsExpired = now;
    }

    public QuickCheck quickCheck(Context context) throws
            InvalidParameterException, SessionException,
            BlubbDBException, BlubbDBConnectionException,
            JSONException {
        Log.v(NAME, "quickCheck(context)");
        List<BlubbThread> threads = new ArrayList<BlubbThread>();
        List<BlubbMessage> messages = new ArrayList<BlubbMessage>();
        // get the quickCheck from beap.
        int [] check = check(context);
        if(check[0] > tCount) {
            threads = ThreadManager.getInstance().getNewThreads(context);
            tCount = check[0];
        }
        if(check[1] > mCount) {
            messages = MessageManager.getInstance().getNewMessagesFromAllThreads(context);
            mCount = check[1];
        }
        return new QuickCheck(threads, messages);
    }

    private int[] check(Context context) throws
            InvalidParameterException, SessionException,
            BlubbDBException, BlubbDBConnectionException {
        Log.v(NAME, "check(context)");
        try {
            String query = "tree.functions.quickCheck(self)";
            BlubbResponse blubbResponse =
                    BlubbRequestManager.query(query, getSessionID(context));
            switch (blubbResponse.getStatus()) {
                case OK:
                    // with status ok result object will be a json array.
                    JSONArray array = (JSONArray) blubbResponse.getResultObj();

                    return new int[] {
                            (Integer) array.get(0),
                            (Integer) array.get(1)};
                default:
                    throw new BlubbDBException("Could not perform quickCheck" +
                            " Beap status: " + blubbResponse.getStatus());
            }
        } catch (JSONException e) {
            throw new BlubbDBException("Received wrong Json object for quickCheck query.");
        }

    }
}
