package com.blubb.alubb.basics;

import java.util.ArrayList;

/**
 * Created by richt_000 on 24.05.2014.
 */
public class ThreadManager {
    private static ThreadManager tm;
    private ArrayList<BlubbThread> blubbThreads;

    private ThreadManager() {
        this.blubbThreads = new ArrayList<BlubbThread>();

    }

    public boolean hasThreads() {
        if(this.blubbThreads != null) {
            if(this.blubbThreads.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public ThreadManager getInstance() {
        if(tm == null) {
            tm = new ThreadManager();
        }
        return tm;
    }

    public BlubbThread getThreadByID(String tId) {
        for(BlubbThread t: this.blubbThreads) {
            if(t.gettId().equals(tId)) {
                return t;
            }
        }
        return null;
    }
}
