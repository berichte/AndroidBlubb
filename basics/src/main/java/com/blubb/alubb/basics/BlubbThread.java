package com.blubb.alubb.basics;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Class representing a blubb thread where user
 * can post massages.
 */
public class BlubbThread {
    private static final String NAME = "BlubbThread";
    private static final String TS_SOLVED = "solved", TS_CLOSED = "closed", TS_OPEN = "open";

    private static DateFormat df = new SimpleDateFormat("EEEE, dd.MM.yyyy");
    /**
     * tTitle of the Thread.
     */
    private String tTitle;
    /**
     * Description for this Thread.
     */
    private String tDesc;
    /**
     * The tCreator of the Thread.
     */
    private String tCreator;
    private String tCreatorRole;
    private ThreadStatus tStatus;
    ;
    private Date tDate;
    private String tId;
    private int tMsgCount;
    private boolean isNew;
    private boolean hasNewMsgs;
    private boolean isBig = false;
    /**
     * Type of this thread, e.g. Chatthread, pollThread, taskThread.
     */
    private ThreadType tType;
    private View bigView, smallView;

    public BlubbThread(JSONObject jsonObject) throws JSONException {

        this.tCreator = BPC.findStringInJsonObj(jsonObject, "tCreator");
        this.tCreatorRole = BPC.findStringInJsonObj(jsonObject, "tCreatorRole");
        String date = BPC.findStringInJsonObj(jsonObject, "tDate");
        try {
            this.tDate = BPC.parseDate(date);
        } catch (ParseException e) {
            Log.e(NAME, e.getMessage());
            this.tDate = new Date();
        }
        this.tTitle = BPC.findStringInJsonObj(jsonObject, "tTitle");
        this.tDesc = BPC.findStringInJsonObj(jsonObject, "tDescr");
        if (jsonObject.has("tMsgCount")) this.tMsgCount = jsonObject.getInt("tMsgCount");
        this.tId = BPC.findStringInJsonObj(jsonObject, "tId");
        this.tType = findThreadType(BPC.findStringInJsonObj(jsonObject, "tType"));
        this.tStatus = parseThreadStatus(BPC.findStringInJsonObj(jsonObject, "tStatus"));
    }

    public BlubbThread(String tId, String tTitle, String tDesc,
                       String tCreator, String tCRole, String tStatus,
                       String tDate, int tMsgCount, String tType, int isNew, int hasNewMsg) {
        this.tId = tId;
        this.tTitle = tTitle;
        this.tDesc = tDesc;
        this.tCreator = tCreator;
        this.tCreatorRole = tCRole;
        settStatus(tStatus);
        try {
            this.tDate = BPC.parseDate(tDate);
        } catch (ParseException e) {
            Log.e(NAME, e.getMessage());
            this.tDate = new Date();
        }
        this.tMsgCount = tMsgCount;
        this.tType = findThreadType(tType);
        this.isNew = (isNew == 1) ? true : false;
        this.hasNewMsgs = (hasNewMsg == 1) ? true : false;
    }

    private ThreadType findThreadType(String tType) {
        if (tType.equals("Thread")) return ThreadType.CHAT_THREAD;

        return ThreadType.UNDEFINED;
    }

    private ThreadStatus parseThreadStatus(String threadStatus) {
        Log.v(NAME, "parseThreadStatus(tStatus=" + threadStatus + ")");
        if (threadStatus.equals(TS_CLOSED)) return ThreadStatus.CLOSED;
        if (threadStatus.equals(TS_SOLVED)) return ThreadStatus.SOLVED;
        else return ThreadStatus.OPEN;
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
        return BPC.parseDate(tDate);
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

    public String gettStatusString() {
        switch (this.tStatus) {
            case CLOSED:
                return TS_CLOSED;
            case SOLVED:
                return TS_SOLVED;
            default:
                return TS_OPEN;
        }
    }

    public void settStatus(String tStatus) {
        this.tStatus = parseThreadStatus(tStatus);
    }

    public ThreadStatus getThreadStatus() {
        return this.tStatus;
    }

    public void settStatus(ThreadStatus tStatus) {
        this.tStatus = tStatus;
    }

    public void setHasNewMsgs(boolean hasNewMsgs) {
        this.hasNewMsgs = hasNewMsgs;
    }

    public boolean isNew() {
        return this.isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean hasNewMsgs() {
        return this.hasNewMsgs;
    }

    public boolean equals(BlubbThread other) {
        if (!this.gettId().equals(other.gettId())) return false;
        if (this.gettMsgCount() != other.gettMsgCount()) return false;
        if (!this.gettCreator().equals(other.gettCreator())) return false;
        if (!this.hasNewMsgs != other.hasNewMsgs) return false;
        if (!this.isNew != other.isNew) return false;
        if (!this.gettCreatorRole().equals(other.gettCreatorRole())) return false;
        if (!this.gettDate().equals(other.gettDate())) return false;
        if (!this.gettDesc().equals(other.gettDesc())) return false;
        if (!this.getThreadTitle().equals(other.getThreadTitle())) return false;
        if (!this.gettType().equals(other.gettType())) return false;
        return true;
    }

    public void toggleViewSize() {
        if (isBig) isBig = false;
        else isBig = true;
    }

    public View getView(Context context, ViewGroup parent) {
        if (isBig) return this.getBigView(context, parent);
        else return this.getSmallView(context, parent);
    }

    public String getFormatedDate() {
        return df.format(tDate);
    }

    public View getBigView(Context context, ViewGroup parent) {
        if (this.bigView == null) this.bigView = createBigView(context, parent);
        return bigView;
    }

    private View createBigView(Context context, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = (LinearLayout) inflater.inflate(
                R.layout.thread_list_entry, parent, false);

        layout = this.setContentForHeader(layout);
        TextView tdescr = (TextView) layout.findViewById(R.id.thread_list_item_description),
                tInfo = (TextView) layout.findViewById(R.id.thread_list_item_info),
                tIcon = (TextView) layout.findViewById(R.id.thread_list_item_status);

        tdescr.setText(this.tDesc);
        String info = this.tCreatorRole + " - " + this.getFormatedDate();
        tInfo.setText(info);
        return layout;
    }

    private TextView setIcon(TextView tIcon) {
        Typeface tf = Typeface.createFromAsset(tIcon.getContext().getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, tIcon);

        if (tCreatorRole.equals("admin")) {
            tIcon.setTextColor(tIcon.getContext().getResources().getColor(R.color.beap_red));
        } else if (tCreatorRole.equals("PL")) {
            tIcon.setTextColor(tIcon.getContext().getResources().getColor(R.color.beap_medium_yellow));
        } else {
            tIcon.setTextColor(tIcon.getContext().getResources().getColor(R.color.beap_dark_blue));
        }
        switch (tStatus) {
            case CLOSED:
                tIcon.setText("Z");
                break;
            case SOLVED:
                tIcon.setText("y");
                break;
            default:
                tIcon.setText("U");
                break;
        }
        return tIcon;
    }

    private View setContentForHeader(View layout) {

        TextView tTitle = (TextView) layout.findViewById(R.id.thread_list_item_title),
                tMsg = (TextView) layout.findViewById(R.id.thread_list_item_msgcount),
                tCreator = (TextView) layout.findViewById(R.id.thread_list_item_author),
                tIcon = (TextView) layout.findViewById(R.id.thread_list_item_status);
        setIcon(tIcon);
        tTitle.setText(this.tTitle);
        tCreator.setText(this.tCreator);
        tMsg.setText(this.tMsgCount + "");
        if (this.hasNewMsgs) {
            int red = layout.getContext().getResources().getColor(R.color.beap_red);
            tMsg.setTextColor(red);
        }
        return layout;
    }

    public View getSmallView(Context context, ViewGroup parent) {
        if (this.smallView == null) this.smallView = createSmallView(context, parent);
        return this.smallView;
    }

    private View createSmallView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.thread_list_entry_small, parent, false);
        return this.setContentForHeader(layout);
    }

    //private String tDate;
    public enum ThreadStatus {
        OPEN, SOLVED, CLOSED
    }

}
