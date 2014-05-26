package com.blubb.alubb.basics;

/**
 * Created by richt_000 on 24.05.2014.
 */
public class ThreadManager {
    private static ThreadManager tm;

    private ThreadManager() {

    }

    public ThreadManager getInstance() {
        if(tm == null) {
            tm = new ThreadManager();
        }
        return tm;
    }
}
