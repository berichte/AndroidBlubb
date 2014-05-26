package com.blubb.alubb.basics;

import com.blubb.alubb.blubexceptions.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Class representing a blubb thread where user
 * can post massages.
 */
public class BlubbThread {
    /** title of the Thread.*/
    private String title;
    /** Description for this Thread. */
    private String description;
    /** The creator of the Thread. */
    private String creator;
    private String creatorRole;
    private String date;
    private String id;
    private int msgCount;
    /** Type of this thread, e.g. Chatthread, pollThread, taskThread.*/
    private ThreadType threadType;
 /*
 "tId" : "t2014-05-17_150000_S-Gross",
            "tType" : "Thread",
            "tCreator" : "S-Gross",
            "tCreatorRole" : "admin",
            "tDate" : "2014-05-17:T15:00:00.000Z",
            "tTitle" : "Thread No. 1",
            "tDescr" : "Weil,\nich bin der Admin\nund ich darf das ! ;-)",
            "tMsgCount" : 2
            */

    public BlubbThread(JSONObject jsonObject) throws JSONException {
        this.creator        = jsonObject.getString("tCreator");
        this.creatorRole    = jsonObject.getString("tCreatorRole");
        this.date           = jsonObject.getString("tDate");
        this.title          = jsonObject.getString("tTitle");
        this.description    = jsonObject.getString("tDescr");
        this.msgCount       = jsonObject.getInt("tMsgCount");
        this.id             = jsonObject.getString("tId");
        this.threadType     = findThreadType(jsonObject.getString("tType"));
    }

    private ThreadType findThreadType(String tType) {
        if(tType.equals("Thread")) return ThreadType.CHAT_THREAD;

        return ThreadType.UNDEFINED;
    }

    public String toString() {
        return "Thread name:\t\t" + this.title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatorRole() {
        return creatorRole;
    }

    public String getDate() {
        return date;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public String getId() {
        return id;
    }

    public ThreadType getThreadType() {
        return threadType;
    }

    public String getThreadTitle() {
        return this.title;
    }
}
