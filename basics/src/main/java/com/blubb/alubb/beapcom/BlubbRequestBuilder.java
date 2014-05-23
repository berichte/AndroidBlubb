package com.blubb.alubb.beapcom;

/**
 * Created by Benjamin Richter on 23.05.2014.
 */
public class BlubbRequestBuilder {
    private static final String URL                     = "http://blubb.traeumtgerade.de:9980/?",
                                BEAP_SESSION            = "BeapId=BeapSession",
                                BEAP_ACTION_LOGIN       = "Action=login",
                                BEAP_APP_VERSION        = "appVers=1.5.0rc1",
                                BLUBB_USERNAME          = "uName",
                                BLUBB_PASSWORD          = "uPwd",
                                BLUBB_AND               = "&amp;";
    //http://blubb.traeumtgerade.de:9980/?
    // BeapId=BeapSession&amp;
    // Action=login&amp;
    // appVers=1.5.0rc1&amp;
    // uName=Der-Praktikant&amp;
    // uPwd=test

    public static String buildLogin(String username, String password){
        String url = URL + BEAP_SESSION + BLUBB_AND
                    + BEAP_ACTION_LOGIN + BLUBB_AND
                    + BEAP_APP_VERSION + BLUBB_AND
                    + BLUBB_USERNAME + "=" + username + BLUBB_AND
                    + BLUBB_PASSWORD + "=" + password;
        return url;
    }
}
