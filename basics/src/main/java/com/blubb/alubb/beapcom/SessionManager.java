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

    public SessionInfo getSession() {
        return session;
    }

    public void setSession(SessionInfo session) {
        this.session = session;
    }

    public String getSessionID() {
        return this.session.getSessionId();
    }
}
