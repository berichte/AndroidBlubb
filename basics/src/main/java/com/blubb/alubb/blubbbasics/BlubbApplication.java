package com.blubb.alubb.blubbbasics;

import android.app.Application;
import android.graphics.Typeface;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.MessageManager;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.basics.ThreadManager;

/**
 * Created by Benjamin Richter on 03.06.2014.
 */
public class BlubbApplication extends Application {
    private static final String NAME = "BlubbApplication";

    SessionManager sessionManager;
    ThreadManager threadManager;
    MessageManager messageManager;

    public static void setLayoutFont(Typeface tf, TextView... params) {
        for (TextView tv : params) {
            tv.setTypeface(tf);
        }
    }

    public void onCreate() {
        super.onCreate();
        String fontName = getString(R.string.application_font);
        FontsOverride.setDefaultFont(this, "MONOSPACE", fontName);
        initSingletons();
    }

    protected void initSingletons() {
        this.sessionManager = SessionManager.getInstance();
        this.threadManager = ThreadManager.getInstance();
        this.messageManager = MessageManager.getInstance();
    }

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    public ThreadManager getThreadManager() {
        return this.threadManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }
}
