package com.blubb.alubb.basics;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Class representing a blubb thread where user
 * can post massages.
 */
public class BlubbThread {
    /** tTitle of the Thread.*/
    private String tTitle;
    /** Description for this Thread. */
    private String tDesc;
    /** The tCreator of the Thread. */
    private String tCreator;
    private String tCreatorRole;
    private String tDate;
    private String tId;
    private int tMsgCount;
    private boolean isNew;
    private boolean hasNewMsgs;

    /** Type of this thread, e.g. Chatthread, pollThread, taskThread.*/
    private ThreadType tType;
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
        this.tCreator = BPC.findStringInJsonObj(jsonObject, "tCreator");
        this.tCreatorRole = BPC.findStringInJsonObj(jsonObject, "tCreatorRole");
        this.tDate = BPC.findStringInJsonObj(jsonObject, "tDate");
        this.tTitle = BPC.findStringInJsonObj(jsonObject, "tTitle");
        this.tDesc = BPC.findStringInJsonObj(jsonObject, "tDescr");
        if(jsonObject.has("tMsgCount")) this.tMsgCount = jsonObject.getInt("tMsgCount");
        this.tId = BPC.findStringInJsonObj(jsonObject, "tId");
        this.tType = findThreadType(BPC.findStringInJsonObj(jsonObject, "tType"));
    }

    public BlubbThread (String tId, String tTitle, String tDesc,
                        String tCreator, String tCRole,
                    String tDate, int tMsgCount, String tType, int isNew, int hasNewMsg) {
        this.tId = tId;
        this.tTitle = tTitle;
        this.tDesc = tDesc;
        this.tCreator = tCreator;
        this.tCreatorRole = tCRole;
        this.tDate = tDate;
        this.tMsgCount = tMsgCount;
        this.tType = findThreadType(tType);
        this.isNew = (isNew == 1)? true : false;
        this.hasNewMsgs = (hasNewMsg == 1)? true : false;
    }

    private ThreadType findThreadType(String tType) {
        if(tType.equals("Thread")) return ThreadType.CHAT_THREAD;

        return ThreadType.UNDEFINED;
    }

    public String toString() {
        return "Thread name:\t\t" + this.tTitle;
    }

    public String gettDesc() {
        return tDesc;
    }

    public String gettCreator() {
        return tCreator;
    }

    public String gettCreatorRole() {
        return tCreatorRole;
    }

    public String gettDate() {
        return tDate;
    }

    public int gettMsgCount() {
        return tMsgCount;
    }

    public String gettId() {
        return tId;
    }

    public ThreadType gettType() {
        return tType;
    }

    public String getThreadTitle() {
        return this.tTitle;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void setHasNewMsgs(boolean hasNewMsgs) {
        this.hasNewMsgs = hasNewMsgs;
    }

    public boolean isNew() {
        return this.isNew;
    }

    public boolean hasNewMsgs() {
        return this.hasNewMsgs;
    }
}
