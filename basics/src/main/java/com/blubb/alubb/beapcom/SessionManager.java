package com.blubb.alubb.beapcom;

import com.blubb.alubb.basics.SessionInfo;

/**
 * Created by Benjamin Richter on 23.05.2014.
 */
public class SessionManager {
    private SessionInfo session;
    private static SessionManager instance;
    private long timeWhenSessionExpires;


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

    public void setTimeTillSessionExpires(int time) {
        long now = System.currentTimeMillis();
        now += (time-1)*60*1000;
        this.timeWhenSessionExpires = now;
    }

    public boolean hasSession() {
        if(this.session != null) return true;
        return false;
    }
    public long timeWhenSessionExpires() {
        return timeWhenSessionExpires;
    }
}
