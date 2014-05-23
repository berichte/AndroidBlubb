package com.blubb.alubb.beapcom;

import com.blubb.alubb.basics.SessionInfo;

/**
 * Created by Benjamin Richter on 23.05.2014.
 */
public class SessionManager {
    private SessionInfo session;
    private static SessionManager instance;

    private SessionManager() {

    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public SessionInfo getSessionId() {
        return session;
    }

    public void setSessionId(SessionInfo session) {
        this.session = session;
    }
}
