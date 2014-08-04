package com.blubb.alubb.beapcom;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Collection of functions to provide request urls to beap, e.g. the url for a login.
 * <p/>
 * Created by Benjamin Richter on 23.05.2014.
 */
public class BlubbRequestManager {
    /**
     * Name for Logging purposes.
     */
    private static final String NAME = BlubbRequestManager.class.getName();
    /**
     * URL to the beap server, prefix for all urls created and all other parts for the
     * different urls except the parameter of course.
     */
    private static final String URL = "http://blubb.traeumtgerade.de:9980/?",
            BEAP_ID = "BeapId=",
            BEAP_ACTION = "Action=",
            BEAP_APP_VERSION = "appVers=",
            BEAP_SESSION_ID = "sessId=",

    BEAP_ACTION_LOGIN = "login",
            BEAP_ACTION_REFRESH = "refresh",
            BEAP_ACTION_LOGOUT = "logout",
            BEAP_ACTION_CHECK = "check",
            BEAP_ACTION_QUERY = "query",
            BEAP_ACTION_RESET_PW = "setOwnPwd",
            BEAP_QUERY_STR = "queryStr=",

    BEAP_ID_SESSION = "BeapSession",
            BEAP_ID_DB = "BeapDB",
            BEAP_VERSION = "1.5.0rc1",
            BLUBB_USERNAME = "uName=",
            BLUBB_PASSWORD = "uPwd=",
            BLUBB_NEW_PWD1 = "newPwd1=",
            BLUBB_NEW_PWD2 = "newPwd2=",
            BLUBB_AND = "&",
            ENCODING = "UTF-8";
    //http://blubb.traeumtgerade.de:9980/?
    // BeapId=BeapSession&amp;
    // Action=login&amp;
    // appVers=1.5.0 rc1&amp;
    // uName=Der-Praktikant&amp;
    // uPwd=test

    /**
     * Builds a login url with the username and password and executes the request.
     *
     * @param username String containing the username.
     * @param password String containing the password corresponding to the username.
     * @return A BlubbResponse from the executed request.
     */
    public BlubbResponse login(String username, String password) {
        String url = URL
                + BEAP_ID + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_LOGIN + BLUBB_AND
                + getParameter(BEAP_APP_VERSION, BEAP_VERSION) + BLUBB_AND
                + getParameter(BLUBB_USERNAME, username) + BLUBB_AND
                + getParameter(BLUBB_PASSWORD, password);
        Log.v("BuildLogin", "url");
        return executeRequest(url);
    }

    /**
     * Builds a checkSession url and executes the request, e.g. like this:
     * http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&Action=check&sessId=a634cca33cc52b1252ba9
     *
     * @param sessionId String with the session id of the session to be refreshed.
     * @return BlubbResponse from the executed request.
     */
    public BlubbResponse checkSession(String sessionId) {
        String url = URL
                + BEAP_ID + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_CHECK + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId);
        Log.v("BuildSessionCheck", url);
        return executeRequest(url);
    }

    /**
     * Makes a session refresh.
     * http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&Action=refresh&sessId=a634cca33cc52b1252ba9
     *
     * @param sessionId String with the session id of the session to be refreshed.
     * @return BlubbResponse from the executed request.
     */
    public BlubbResponse refreshSession(String sessionId) {
        String url = URL
                + BEAP_ID + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_REFRESH + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId);
        Log.v("BuildSessionRefresh", url);
        return executeRequest(url);
    }

    /**
     * Performs a logout for a session.
     *
     * @param sessionId String with the session id of the session to be refreshed.
     * @return BlubbResponse from the executed request.
     */
    public BlubbResponse logout(String sessionId) {
        String url = URL
                + BEAP_ID + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_LOGOUT + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId);
        Log.v("BuildLogout", url);
        return executeRequest(url);
    }

    /**
     * Builds a url to reset the password of a user and executes the request.
     *
     * @param username  String containing the username.
     * @param oldPw     String containing the old password of the user.
     * @param newPw     String containing the new password of the user.
     * @param confirmPw String containing the new password of the user again, the server verifies
     *                  whether the passwords are equal.
     * @return BlubbResponse from the executed request.
     */
    public BlubbResponse resetPassword(
            String username, String oldPw, String newPw, String confirmPw) {
        String url = URL
                + BEAP_ID + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_RESET_PW + BLUBB_AND
                + getParameter(BLUBB_USERNAME, username) + BLUBB_AND
                + getParameter(BLUBB_PASSWORD, oldPw) + BLUBB_AND
                + getParameter(BLUBB_NEW_PWD1, newPw) + BLUBB_AND
                + getParameter(BLUBB_NEW_PWD2, confirmPw);
        Log.v(NAME, "Build resetPassword url:\n" + url);
        return executeRequest(url);
    }

    //http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&sessId=a634cca33cc52b1252ba9&Action=query
    // &queryStr=tree.functions.getAllThreads(self)

    /**
     * Builds the url for a query to the beapDB and executes it.
     * To execute a query on the beapDB a valid sessionId is mandatory.
     *
     * @param query     String containing the query to the beapDB:
     *                  - 'tree.functions.getAllThreads(self)'
     *                  - 'tree.functions.createThread(self,"tTitle","tDescription")'
     *                  - 'tree.functions.setThread(self,"tId","tTitle","tDescription","tStatus")'
     *                  - 'tree.functions.quickCheck(self)'
     *                  - 'tree.functions.getMsgsForThread(self,"tId")'
     *                  - 'tree.functions.createMsg(self,"tId","mTitle","mContent","mLink")'
     *                  - 'tree.functions.setMsg(self,"mId","mTitle","mContent","mLink")'
     * @param sessionId String with the sessionId of the session which executes the query on beapDB.
     * @return BlubbResponse from the executed request containing a result for the executed query.
     */
    public BlubbResponse query(String query, String sessionId) {
        String url = URL
                + BEAP_ID + BEAP_ID_DB + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId) + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_QUERY + BLUBB_AND
                + BEAP_QUERY_STR
                + query + BLUBB_AND;
        Log.v("BuildQuery", "\nBuild " + query + " to\n" + url);
        return executeRequest(url);
    }

    /**
     * The parameter send to beap must be url encoded. This function puts the parameter key and
     * the encoded value in one string.
     *
     * @param para  Name of the parameter at beap (not beapDB).
     * @param value Parameter value which needs to be encoded.
     * @return String containing key and encoded value, e.g.
     * "uName=" + "Der-Blubb" => "uName=Der-Blubb".
     */
    public String getParameter(String para, String value) {
        return para + encode(value);
    }

    /**
     * Makes a url encoding with utf-8 for a string.
     *
     * @param s String which will be encoded.
     * @return Encoded string.
     */
    private String encode(String s) {
        try {
            String enc = URLEncoder.encode(s, ENCODING);
            Log.v("urlEncoding", "Encoding " + s + " to\n" + enc);
            return enc;
        } catch (UnsupportedEncodingException e) {
            Log.e(NAME, e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Executes a BlubbHttpRequest with the provided url.
     *
     * @param url Url which will be send with the request
     * @return BlubbResponse object build from the http response if it contains a valid json object.
     */
    private BlubbResponse executeRequest(String url) {
        Log.v(NAME, "Executing http-request:\n" + url);
        // request via http
        String httpResponse = new BlubbHttpRequest().request(url);
        //parse the response to an object
        return new BlubbResponse(httpResponse);
    }
}
