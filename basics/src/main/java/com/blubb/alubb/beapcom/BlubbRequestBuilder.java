package com.blubb.alubb.beapcom;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.blubb.alubb.beapcom.SessionManager.*;

/**
 * Created by Benjamin Richter on 23.05.2014.
 */
public class BlubbRequestBuilder {
    private static final String NAME = BlubbRequestBuilder.class.getName();
    private static final String URL                     = "http://blubb.traeumtgerade.de:9980/?",
                                BEAP_ID                 = "BeapId=",
                                BEAP_ACTION             = "Action=",
                                BEAP_APP_VERSION        = "appVers=",
                                BEAP_SESSION_ID         = "sessId=",
                                BEAP_ACTION_LOGIN       = "login",
                                BEAP_ACTION_REFRESH     = "refresh",
                                BEAP_ACTION_LOGOUT      = "logout",
                                BEAP_ACTION_QUERY       = "query",
                                BEAP_QUERY_STR          = "queryStr=",
                                BEAP_QUERY_PREFIX       = "tree.functions.",
                                BEAP_QUERY_SELF_SUFIX = "(self)",
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

    public static String buildLogin(String username, String password){
        return URL
               + BEAP_ID           + BEAP_ID_SESSION + BLUBB_AND
               + BEAP_ACTION       + BEAP_ACTION_LOGIN     + BLUBB_AND
               + getParameter(BEAP_APP_VERSION, BEAP_VERSION)
               + getParameter(BLUBB_USERNAME, username)
               + getParameter(BLUBB_PASSWORD, password);
    }

    //http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&Action=refresh&sessId=a634cca33cc52b1252ba9
    public static String buildSessionRefresh() {
        return URL
                + BEAP_ID   + BEAP_ID_DB + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_REFRESH + BLUBB_AND
                + getSessionPara();
    }

    public static String buildLogout() {
        return URL
                + BEAP_ID   + BEAP_ID_DB    + BLUBB_AND
                + BEAP_ACTION + BEAP_ACTION_LOGOUT + BLUBB_AND
                + getSessionPara();
    }


    //http://blubb.traeumtgerade.de:9980/?BeapId=BeapDB&sessId=a634cca33cc52b1252ba9&Action=query&queryStr=tree.functions.getAllThreads(self)

    public static String buildGetAllThreads() {

    }

    private static String buildQuery(String query) {
        return URL
                + BEAP_ID   + BEAP_ID_DB + BLUBB_AND
                + getSessionPara()
                + BEAP_ACTION + BEAP_ACTION_QUERY + BLUBB_AND
                + BEAP_QUERY_STR
                + encode(BEAP_QUERY_PREFIX + query + BEAP_QUERY_SELF_SUFIX) + BLUBB_AND;
    }

    private static String getSessionPara() {
        return getParameter(BEAP_SESSION_ID,
                getInstance().getSessionID());
    }

    private static String getParameter(String para, String value) {
        return para + encode(value) + BLUBB_AND;
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.e(NAME, e.getMessage());
            return e.getMessage();
        }
    }
}