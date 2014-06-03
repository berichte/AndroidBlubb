package com.blubb.alubb.beapcom;

import android.content.Context;

import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;


/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbComManager {
    private static final String N = BlubbComManager.class.getName();
    private static final String F_SELF = "(self,",
                                 F_SEP = ",",
                                F_END = ")";

/*

    public static BlubbMessage[] getMessages(Context context, String blubbThreadId) throws BlubbDBException {
        // Build the request for the BeapDB
        String url = BlubbRequestManager.buildQuery(
                "tree.functions.getMsgsForThread" + F_SELF +
                        encPara(blubbThreadId) + F_END
        );
        BlubbMessage[] messages;
        // execute the request
        BlubbResponse response = executeRequest(context, url);
        // if the response is empty return an empty array of Messages
        if(response.getStatus() == BlubbDBReplyStatus.NO_CONTENT){
            BlubbMessage m = new BlubbMessage("no-thread-for-this",
                    "It's empty o.O", "Quick be the first who posts here.");
            return new BlubbMessage[] {m};

        }
        Object resObj = response.getResultObj();
        if (resObj == null) throw new BlubbDBException("Did't get any Result.");
         // Check whether the response is an array
        if(resObj.getClass().getName().equals(
                JSONArray.class.getName())) {
            // fill the array
            JSONArray jsonArray = (JSONArray) response.getResultObj();
            try {
                // check whether the type is right - expect mType for messages
                if (jsonArray.getJSONObject(0).has("mType")) {
                    messages = new BlubbMessage[jsonArray.length()];
                    for(int i = 0; i < jsonArray.length(); i++) {
                        messages[i] = new BlubbMessage(jsonArray.getJSONObject(i));
                    }
                    return messages;
                }
                // throw dbException if the response type is wrong
                else {
                    Log.e("BlubbComManager.getMsgsForThread", "Received wrong " +
                            "response type, expected 'mType'");
                    throw new BlubbDBException("Received wrong response type. " +
                            "Expected 'mType'");
                }
                // if there is no object in the array throw a db exception.
            } catch (JSONException e) {
                Log.e("BlubbComManager.getMsgsForThread", "No json object in msg_array.");
                throw new BlubbDBException("Database response has some error: " +
                        "No json object in msg_array.");
            }
            // throw a dbException if the result is not a json array.
        } else throw new BlubbDBException("did not get a jsonArray as expected. :(");
    }

    public static BlubbDBReplyStatus sendMessage(Context context, String tId, String mTitle, String mContent)
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

    public static BlubbThread[] getAllThreads(Context context) throws BlubbDBException, BlubbDBConnectionException {
        //get valid query request string
        String url = BlubbRequestManager.buildQuery("tree.functions.getAllThreads(self)");
        BlubbThread[] threads = null;
        BlubbResponse response = executeRequest(context, url);
        if(response.getResultObj().getClass().getName().equals(
                JSONArray.class.getName())) {
                JSONArray jsonArray = (JSONArray) response.getResultObj();
            try {
                if (jsonArray.getJSONObject(0).has("tType")) {
                    threads = new BlubbThread[jsonArray.length()];
                    for(int i = 0; i < jsonArray.length(); i++) {
                        threads[i] = new BlubbThread(jsonArray.getJSONObject(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else throw new BlubbDBException("did not get a jsonArray as expected. :(");
        return threads;
    }

    public static BlubbDBReplyStatus openNewBlubbThread(
            Context context, String tTitle, String tDescr)
            throws BlubbDBException, InvalidParameterException {
        tTitle = encPara(BPC.parseStringParameterToDB(tTitle));
        tDescr = encPara(BPC.parseStringParameterToDB(tDescr));
        String url = BlubbRequestManager.buildQuery(
                "tree.functions.createThread " + F_SELF + tTitle + F_SEP + tDescr + F_END);
        BlubbResponse response = executeRequest(context, url);
        return response.getStatus();
    }

    private static String username, password;

    public static SessionInfo login(Context context, String username, String password)
            throws InvalidParameterException, BlubbDBException {
        //check the parameter
        username = BPC.parseStringParameterToDB(username);
        password = BPC.parseStringParameterToDB(password);
        // get a valid request string
        return doLogin(context, username, password);
    }

    private static SessionInfo doLogin(Context context, String username, String password)
            throws BlubbDBException {
        String requestString = BlubbRequestManager.buildLogin(username, password);

        BlubbResponse responseObj = executeSessionRequest(context, requestString);
        // check whether the response is ok or there is some error
        if (responseObj.getStatus() == BlubbDBReplyStatus.OK) {
            return responseObj.getSessionInfo();
        } else {
            throw new BlubbDBException(responseObj.getStatusDescr());
        }
    }
/*
    private static SessionInfo doLogin(Context context) throws BlubbDBException {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String username = prefs.getString(Blubb_login.USERNAME_PREFAB, "NULL"),
                password = prefs.getString(Blubb_login.PASSWORD_PREFAB, "NULL");
        if(username.equals("NULL") || password.equals("NULL")) return false;
        return doLogin(context, username, password);
    }

    //TODO Rewrite whole method!
    @Deprecated
    public static int[] quickCheck(Context context) throws BlubbDBException {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String username = prefs.getString(Blubb_login.USERNAME_PREFAB, "NULL"),
                password = prefs.getString(Blubb_login.PASSWORD_PREFAB, "NULL");
        if(username.equals("NULL") || password.equals("NULL")) return new int[]{};

        String url = BlubbRequestManager.buildQuery("tree.functions.quickCheck(self)");
        BlubbResponse response = executeRequest(context, url);
        if(response.getStatus().equals(BlubbDBReplyStatus.OK)) {
            if(response.getResultObj().getClass().getName().equals(
                    JSONArray.class.getName())) {
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                try {
                    int mc = (Integer) jsonArray.get(0);
                    int tc = (Integer) jsonArray.get(1);
                    return new int[]{mc, tc};
                } catch (JSONException e) {
                    Log.e("quickCheck", "Didn't get expected result array.");
                    throw new BlubbDBException(e.getMessage());
                }
            }else throw new BlubbDBException("could not execute quick check, no array returned" );
        } else if (response.getStatus().equals(BlubbDBReplyStatus.LOGIN_REQUIRED)
                || response.getStatus().equals(BlubbDBReplyStatus.REQUEST_FAILURE)) {
            if(doLogin(context)){
                return quickCheck(context);
            }
            throw new BlubbDBException("could not execute quick check.  Status: " +
                    response.getStatus().toString() );
        } else throw new BlubbDBException("could not execute quick check. :( Status: " +
                response.getStatus().toString() );
    }

    @Deprecated
    public static int tryRefresh(Context context) {
        String url = BlubbRequestManager.buildSessionRefresh();
        try {
            BlubbResponse response = executeSessionRequest(context, url);
            if(response.getStatus().equals(BlubbDBReplyStatus.LOGIN_REQUIRED)) {
                return 0;
            } else if (!response.getStatus().equals(BlubbDBReplyStatus.OK)){
                throw new BlubbDBException("unexpected status while session refresh.");
            }
            Object result = response.getResultObj();
            if(result != null) {
                if(result.getClass().equals(Integer.class)) {
                    return (Integer) result;
                }
            }
        } catch (BlubbDBException e) {
            Log.e("SessionCheck", e.getMessage());
            return 0;
        }
        return 0;
    }

    @Deprecated
    public static int sessionCheck(Context context, String sessionId) {
        String validUrl = BlubbRequestManager.buildCheckSession();
        try {
            BlubbResponse response = executeSessionRequest(context, validUrl);
            Object result = response.getResultObj();
            if(result != null) {

                if(result.getClass().equals(Integer.class)) {

                    return (Integer) result;
                }
            }
        } catch (BlubbDBException e) {
            Log.e("SessionCheck", e.getMessage());
            return 0;
        }
        String httpResponse = BlubbHttpRequest.request(validUrl);
        return 0;
    }

    @Deprecated
    private static void sessionRefresh(Context context) {
        // Don't try to refresh a not existing session.
        if(!SessionManager.getInstance().hasSession()) return;

        long now = System.currentTimeMillis();
        // sessiontime - one minute
        long sT = SessionManager.getInstance().timeWhenSessionExpires();

        Date dateNow = new Date();
        Date dateSt = new Date(sT);
        Log.i(N, "Need to refresh the session. " +
                "\nnow:\t" + dateNow.getMinutes() + ":" + dateNow.getSeconds() + "" +
                "\nSession:\t" + dateSt.getMinutes() + ":" + dateSt.getSeconds());

        if(sT<now) {

            int refresh = tryRefresh(context);
            if(refresh>0) SessionManager.getInstance().setTimeTillSessionExpires(refresh);
        }
    }

    private static BlubbResponse executeSessionRequest(Context context, String url)
            throws BlubbDBException {
        Log.i("BlubbComManager", "Executing http-session-request:\n" + url);
        // request via http
        String httpResponse = BlubbHttpRequest.request(url);
        //parse the response to an object
        BlubbResponse blubbResponse = new BlubbResponse(httpResponse);
        if(blubbResponse.getStatus() == BlubbDBReplyStatus.LOGIN_REQUIRED) {
            doLogin(context);
        }
        return new BlubbResponse(httpResponse);
    }

    private static BlubbResponse executeRequest(Context context, String url)
            throws BlubbDBException {
        sessionRefresh(context);
        Log.i("BlubbComManager", "Executing http-request:\n" + url);
        // request via http
        String httpResponse = BlubbHttpRequest.request(url);
        //parse the response to an object
        BlubbResponse blubbResponse = new BlubbResponse(httpResponse);
        if(blubbResponse.getStatus() == BlubbDBReplyStatus.LOGIN_REQUIRED) {
            doLogin(context);
        }
        return new BlubbResponse(httpResponse);
    }

    private static String encPara(String para) {
        String p = " \"" + para + "\"";
        return BlubbRequestManager.encode(p);
    }

    public static int checkSession(Context context, String sessionId) {
        //TODO check Session
        return 0;
    }

    public static int refreshSession(Context context, String sessionId) {
        //TODO refresh Session
        return 0;
    }
    */
}