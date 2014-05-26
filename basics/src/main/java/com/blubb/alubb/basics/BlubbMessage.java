package com.blubb.alubb.basics;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbMessage {
    private static final String UNDEFINED = "undefined";

   private String mId,
    mType,
    mCreator,
    mCreatorRole,
    mDate,
    mThread,
    mTitle,
    mContent;

    public BlubbMessage(JSONObject object) {
       this.fillFieldsViaJson(object);
    }

    public BlubbMessage(String Title, String Content) {

    }

    public void fillFieldsViaJson(JSONObject object) {
        this.mId            = findString(object, "mId");
        this.mType          = findString(object, "mType");
        this.mCreator       = findString(object, "mCreator");
        this.mCreatorRole   = findString(object, "mCreatorRole");
        this.mDate          = findString(object, "mDate");
        this.mThread        = findString(object, "mThread");
        this.mTitle         = findString(object, "mTitle");
        this.mContent       = findString(object, "mContent");
    }

    private String findString(JSONObject obj, String toFind) {
        if(obj.has(toFind)) try {
            return obj.getString(toFind);
        } catch (JSONException e) {
            return UNDEFINED;
        }
        return UNDEFINED;
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
    /*public BlubbUser getAuthor();

    void getType();
    void getMessage();
    void getCreationTime();
    BlubbThread getThread();
    BlubbMessage getQuotedMessage();*/
}
/* /* ReturnOkObj
        "BeapStatus" : 200,
        "StatusDescr" : "OK",
        "Result" : [
        {
        "mId" : "m2014-05-17_161900_Der-Praktikant",
        "mType" : "Message",
        "mCreator" : "Der-Praktikant",
        "mCreatorRole" : "user",
        "mDate" : "2014-05-17:T16:19:00.000Z",
        "mThread" : "t2014-05-17_150000_S-Gross",
        "mTitle" : "Sind wir schon online?",
        "mContent" : "Is' ja cool!\nFunzt schon alles?"
        },
        {
        "mId" : "m2014-05-17_162000_S-Gross",
        "mType" : "Message",
        "mCreator" : "S-Gross",
        "mCreatorRole" : "admin",
        "mDate" : "2014-05-17:T16:20:00.000Z",
        "mThread" : "t2014-05-17_150000_S-Gross",
        "mTitle" : "Na ja ... fast ;-)",
        "mContent" : "Ist erst mal der Rohling\nAn den Details müssen wir schon noch ein bißchen rumfummeln ..."
        }
        ],
        "sessInfo" : {
        "sessId" : "ad46180026c6d1c440c0082c16af69a5",
        "sessUser" : "Der-Praktikant",
        "sessRole" : "user",
        "sessActive" : true,
        "expires" : "Sun, 25 May 2014 14:18:39 GMT"
        }
        }*/