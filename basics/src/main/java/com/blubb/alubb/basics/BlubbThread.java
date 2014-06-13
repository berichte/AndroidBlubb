package com.blubb.alubb.basics;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BPC;
import com.blubb.alubb.blubbbasics.BlubbApplication;

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
    private boolean isBig = false;

    /** Type of this thread, e.g. Chatthread, pollThread, taskThread.*/
    private ThreadType tType;

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

    public boolean equals(BlubbThread other) {
        if(!this.gettId().equals(other.gettId())) return false;
        if(this.gettMsgCount() != other.gettMsgCount()) return false;
        if(!this.gettCreator().equals(other.gettCreator())) return false;
        if(!this.hasNewMsgs != other.hasNewMsgs) return false;
        if(!this.isNew != other.isNew) return false;
        if(!this.gettCreatorRole().equals(other.gettCreatorRole())) return false;
        if(!this.gettDate().equals(other.gettDate())) return false;
        if(!this.gettDesc().equals(other.gettDesc())) return false;
        if(!this.getThreadTitle().equals(other.getThreadTitle())) return false;
        if(!this.gettType().equals(other.gettType())) return false;
        return true;
    }

    public void toggleViewSize() {
        if(isBig)   isBig = false;
        else        isBig = true;
    }

    public View getView(Context context, ViewGroup parent) {
        if(isBig) return this.getBigView(context, parent);
        else return this.getSmallView(context, parent);
    }

    public String getFormatedDate() {
        return tDate.substring(0, 16).replace('T', ' ');
    }


    public View getBigView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.thread_list_entry, parent, false);
        TextView tTitle     = (TextView) layout.findViewById(R.id.thread_list_item_title),
                tMsg        = (TextView) layout.findViewById(R.id.thread_list_item_msgcount),
                tdescr      = (TextView) layout.findViewById(R.id.thread_list_item_description),
                tInfo       = (TextView) layout.findViewById(R.id.thread_list_item_info),
                tCreator    = (TextView) layout.findViewById(R.id.thread_list_item_author);
        tTitle.setText(this.tTitle);
        tCreator.setText(this.tCreator);
        tMsg.setText(this.tMsgCount + "");
        if(this.hasNewMsgs) {
            int red = context.getResources().getColor(R.color.beap_red);
            tMsg.setTextColor(red);
        }
        tdescr.setText(this.tDesc);
        String info = this.tCreatorRole + " - " +  this.getFormatedDate();
        tInfo.setText(info);
        return layout;
    }

    public View getSmallView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.thread_list_item_small, parent, false);
        TextView tTitle     = (TextView) layout.findViewById(R.id.thread_list_item_title),
                tMsg        = (TextView) layout.findViewById(R.id.thread_list_item_msgcount);

        tTitle.setText(this.tTitle);
        tMsg.setText(this.tMsgCount + "");
        if(this.hasNewMsgs) {
            int red = context.getResources().getColor(R.color.beap_red);
            tMsg.setTextColor(red);
        }
        return layout;
    }


}
