package com.blubb.alubb.basics;

import com.blubb.alubb.beapcom.BPC;

import org.json.JSONObject;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbMessage {

   private String mId,
    mType,
    mCreator,
    mCreatorRole,
    mDate,
    mThread,
    mTitle,
    mContent;
    private boolean isNew;

    public BlubbMessage(JSONObject object) {
       this.fillFieldsViaJson(object);
    }

    public BlubbMessage(String mId, String mTitle, String mContent, String mCreatorRole,
                        String mCreator, String mDate, String mType, String mThread, int isNew) {
        this.mId = mId;
        this.mType = mType;
        this.mCreator = mCreator;
        this.mCreatorRole = mCreatorRole;
        this.mDate = mDate;
        this.mThread = mThread;
        this.mTitle = mTitle;
        this.mContent = mContent;
        this.isNew = (isNew == 1)? true : false;
    }

    public BlubbMessage(String threadId, String title, String content) {
        this.mThread = threadId;
        this.mTitle = title;
        this.mContent = content;
    }

    public void fillFieldsViaJson(JSONObject object) {
        // TODO: parse the strings from DB
        this.mId            = BPC.findStringInJsonObj(object, "mId");
        this.mType          = BPC.findStringInJsonObj(object, "mType");
        this.mCreator       = BPC.findStringInJsonObj(object, "mCreator");
        this.mCreatorRole   = BPC.findStringInJsonObj(object, "mCreatorRole");
        this.mDate          = BPC.findStringInJsonObj(object, "mDate");
        this.mThread        = BPC.findStringInJsonObj(object, "mThread");
        this.mTitle         = BPC.findStringInJsonObj(object, "mTitle");
        this.mContent       = BPC.findStringInJsonObj(object, "mContent");
    }



    public String getmTitle() {
        return mTitle;
    }

    public String getmId() {
        return mId;
    }

    public String getmType() {
        return mType;
    }

    public String getmCreator() {
        return mCreator;
    }

    public String getmCreatorRole() {
        return mCreatorRole;
    }

    public String getmDate() {
        return mDate;
    }

    public String getmThread() {
        return mThread;
    }

    public String getmContent() {
        return mContent;
    }

    public String toString() {
        return "mThread: " + mThread +
                "mTitle: " + mTitle +
                "mContent: " + mContent;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }
}
