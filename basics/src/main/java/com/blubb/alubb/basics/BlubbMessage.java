package com.blubb.alubb.basics;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BPC;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    public String getFormatedDate() {
        return mDate.substring(0, 16).replace('T', ' ');
    }

    public View getView(Context context, ViewGroup parent, String tCreator) {
        View messageView, layout = null;
        if(tCreator.equals(this.mCreator)) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout = inflater.inflate(R.layout.message_thread_creator, parent, false);
            messageView = layout.findViewById(R.id.message_thread_creator);
        } else {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            messageView = inflater.inflate(R.layout.message_layout, parent, false);
        }
        TextView mTitle = (TextView) messageView.findViewById(R.id.message_title_tv),
                mContent= (TextView) messageView.findViewById(R.id.message_content_tv),
                mCreator = (TextView) messageView.findViewById(R.id.message_creator_tv),
                mDate = (TextView) messageView.findViewById(R.id.message_date_tv),
                mRole = (TextView) messageView.findViewById(R.id.message_role_tv);

        mTitle.setText(this.getmTitle());
        mContent.setText(this.getmContent());
        mCreator.setText(this.getmCreator());
        mDate.setText(this.getFormatedDate());
        mRole.setText(this.getmCreatorRole());

        if(isNew()) {
            Log.v("BlubbMessage", "Message is new.");
            messageView.setBackground(
                    context.getResources().getDrawable(R.drawable.message_layout_back_new));
        }

        if(tCreator.equals(this.mCreator)) {
            return layout;
        } else {
            return messageView;
        }
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

    public boolean equals(BlubbMessage other) {
        if(!this.mId.equals(other.mId)) return false;
        if(!this.mType.equals(other.mType)) return false;
        if(!this.mCreator.equals(other.mCreator)) return false;
        if(!this.mCreatorRole.equals(other.mCreatorRole)) return false;
        if(!this.mDate.equals(other.mDate)) return false;
        if(!this.mThread.equals(other.mThread)) return false;
        if(!this.mTitle.equals(other.mTitle)) return false;
        if(!this.mContent.equals(other.mContent)) return false;
        return true;
    }
}
