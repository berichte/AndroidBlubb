package com.blubb.alubb.beapcom;

import android.content.Context;
import android.util.Log;

import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Benjamin Richter on 23.05.2014.
 */
public class BlubbRequestManager {
    private static final String NAME = BlubbRequestManager.class.getName();
    private static final String URL                     = "http://blubb.traeumtgerade.de:9980/?",
                                BEAP_ID                 = "BeapId=",
                                BEAP_ACTION             = "Action=",
                                BEAP_APP_VERSION        = "appVers=",
                                BEAP_SESSION_ID         = "sessId=",

                                BEAP_ACTION_LOGIN       = "login",
                                BEAP_ACTION_REFRESH     = "refresh",
                                BEAP_ACTION_LOGOUT      = "logout",
                                BEAP_ACTION_CHECK       = "check",
                                BEAP_ACTION_QUERY       = "query",

                                BEAP_QUERY_STR          = "queryStr=",

                                BEAP_ID_SESSION         = "BeapSession",
                                BEAP_ID_DB              = "BeapDB",
                                BEAP_VERSION            = "1.5.0rc1",
                                BLUBB_USERNAME          = "uName=",
                                BLUBB_PASSWORD          = "uPwd=",
                                BLUBB_AND               = "&",
                                ENCODING                = "UTF-8";
    //http://blubb.traeumtgerade.de:9980/?
    // BeapId=BeapSession&amp;
    // Action=login&amp;
    // appVers=1.5.0 rc1&amp;
    // uName=Der-Praktikant&amp;
    // uPwd=test

    public static BlubbResponse login(String username, String password)
            throws BlubbDBException {
        String url = URL
                + BEAP_ID           + BEAP_ID_SESSION           + BLUBB_AND
                + BEAP_ACTION       + BEAP_ACTION_LOGIN         + BLUBB_AND
                + getParameter(BEAP_APP_VERSION, BEAP_VERSION)  + BLUBB_AND
                + getParameter(BLUBB_USERNAME, username)        + BLUBB_AND
                + getParameter(BLUBB_PASSWORD, password);
        Log.i("BuildLogin", "url");
        return executeRequest(url);
    }

    //http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&Action=check&sessId=a634cca33cc52b1252ba9
    public static BlubbResponse checkSession(Context context, String sessionId)
            throws BlubbDBException {
        String url = URL
                + BEAP_ID + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_CHECK + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId);
        Log.i("BuildSessionCheck", url);
        return executeRequest(url);
    }

    //http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&Action=refresh&sessId=a634cca33cc52b1252ba9
    public static BlubbResponse refreshSession(String sessionId) throws BlubbDBException {
        String url = URL
                + BEAP_ID   + BEAP_ID_SESSION + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_REFRESH + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId);
        Log.i("BuildSessionRefresh", url);
        return executeRequest(url);
    }

    public static BlubbResponse logout(String sessionId) throws BlubbDBException {
        String url = URL
                + BEAP_ID   + BEAP_ID_DB    + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_LOGOUT + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId);
        Log.i("BuildLogout", url);
        return executeRequest(url);
    }


    //http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&sessId=a634cca33cc52b1252ba9&Action=query
    // &queryStr=tree.functions.getAllThreads(self)

    public static BlubbResponse query(String query, String sessionId)
            throws BlubbDBException {
        String url = URL
                + BEAP_ID   + BEAP_ID_DB + BLUBB_AND
                + getParameter(BEAP_SESSION_ID, sessionId) + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_QUERY + BLUBB_AND
                + BEAP_QUERY_STR
                + query + BLUBB_AND;
        Log.i("BuildQuery", "\nBuild " + query + " to\n" + url);
        return executeRequest(url);
    }

    public static int[] quickCheck(Context context, String sessionId) throws BlubbDBException,
            InvalidParameterException, SessionException {
        try {
            String query = "tree.functions.quickCheck(self)";
            BlubbResponse blubbResponse = BlubbRequestManager.query(query, sessionId);
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

    public static String getParameter(String para, String value) {
        return para + encode(value);
    }

    private static String encode(String s) {
        try {
            String enc = URLEncoder.encode(s, ENCODING);
            Log.i("urlEncoding", "Encoding " + s + " to\n" + enc);
            return enc;
        } catch (UnsupportedEncodingException e) {
            Log.e(NAME, e.getMessage());
            return e.getMessage();
        }
    }

    private static BlubbResponse executeRequest(String url)
            throws BlubbDBException {
        Log.i(NAME, "Executing http-request:\n" + url);
        // request via http
        String httpResponse = BlubbHttpRequest.request(url);
        //parse the response to an object
        return new BlubbResponse(httpResponse);
    }

    public static String parameterCheck(String para) throws InvalidParameterException {
        BPC.checkString(para);
        return "\"" + para + "\"";
    }
}
