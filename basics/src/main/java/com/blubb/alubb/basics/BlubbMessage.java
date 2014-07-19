package com.blubb.alubb.basics;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BPC;
import com.blubb.alubb.blubbbasics.ActivitySingleThread;
import com.blubb.alubb.blubbbasics.BlubbApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * A BlubbMessage represents a message within a BlubbThread.
 * There are two ways of constructing one. Either with a JSON-object from the beap-db
 * or by providing all fields.
 * It also creates the View for the user interface.
 * <p/>
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbMessage {
    /**
     * Name for Logging purposes.
     */
    private static final String NAME = "BlubbMessage";

    /**
     * Unique id within the db
     */
    private String mId;

    /**
     * Optional Title - may be "undefined" or "NULL".
     */
    private String mTitle;

    /**
     * Content of the message, now just TextContent.
     */
    private MContent mContent;

    /**
     * The creator of the message can be a admin, project leader or regular user.
     */
    private String mCreatorRole;

    /**
     * Id of the creator
     */
    private String mCreator;

    /**
     * Date when the message was created in the db.
     */
    private Date mDate;

    /**
     * Type of the message, e.g. 'message'.
     */
    private String mType;

    /**
     * Id of the parent thread
     */
    private String mThread;

    /**
     * If this message is a reply to another message this is the id of that.
     */
    private String mLink;

    /**
     * Flag for whether the user has already seen this message.
     */
    private boolean isNew;

    /**
     * View displaying the message in the user interface.
     * Will be build when getView(...) has been called.
     */
    private View msgView;

    /**
     * If this message is a reply to another message the linkPos will point to the position of
     * the message replied to within the message array adapter.
     */
    private int linkPos;

    /**
     * Constructs a BlubbMessage with a json object.
     * Will call fillFieldsViaJson(object) to build the message.
     * isNew will be true.
     *
     * @param object the json object containing all data for the BlubbMessage.
     */
    public BlubbMessage(JSONObject object) {
        this.fillFieldsViaJson(object);
    }

    /**
     * Constructs a BlubbMessage with every single parameter.
     *
     * @param mId          Unique id within the db
     * @param mTitle       Optional Title - may be "undefined" or "NULL"
     * @param mContent     Content of the message, now just text.
     * @param mCreatorRole The creator of the message can be a admin,
     *                     project leader or regular user.
     * @param mCreator     Id of the creator.
     * @param mDate        Date when the message was created in the db.
     * @param mType        Type of the message, e.g. 'message'.
     * @param mThread      Id of the parent thread.
     * @param mLink        If this message is a reply to another message this is the id of that.
     * @param isNew        Flag for whether the user has already seen this message.
     */
    public BlubbMessage(String mId, String mTitle, String mContent, String mCreatorRole,
                        String mCreator, String mDate, String mType, String mThread,
                        String mLink, int isNew) {
        this.mId = mId;
        this.mType = mType;
        this.mCreator = mCreator;
        this.mCreatorRole = mCreatorRole;

        try {
            this.mDate = BPC.parseDate(mDate);
        } catch (ParseException e) {
            Log.e(NAME, e.getMessage());
            this.mDate = new Date();
        }
        this.mThread = mThread;
        this.mLink = mLink;

        this.mTitle = mTitle;
        this.mContent = new TextContent(mContent);
        this.isNew = (isNew == 1);
    }

    /**
     * Fills all fields of a BlubbMessage with the values of the json object, isNew will be true.
     *
     * @param object the json object containing all data for the BlubbMessage.
     */
    public void fillFieldsViaJson(JSONObject object) {
        this.mId = BPC.findStringInJsonObj(object, "mId");
        this.mType = BPC.findStringInJsonObj(object, "mType");
        this.mCreator = BPC.findStringInJsonObj(object, "mCreator");
        this.mCreatorRole = BPC.findStringInJsonObj(object, "mCreatorRole");
        String date = BPC.findStringInJsonObj(object, "mDate");
        try {
            this.mDate = BPC.parseDate(date);
        } catch (ParseException e) {
            Log.e(NAME, e.getMessage());
            this.mDate = new Date();
        }
        this.mThread = getThreadID(object);
        this.mTitle = BPC.findStringInJsonObj(object, "mTitle");
        if (mTitle.equals(BPC.UNDEFINED)) mTitle = "";
        this.mContent = new TextContent(BPC.findStringInJsonObj(object, "mContent"));
        this.mLink = BPC.findStringInJsonObj(object, "mLink");
        this.isNew = true;
    }

    /**
     * Since mThread within the json object from beapDB is an array with one or more
     * thread ids this method will find the right threadId for the message.
     * If there are more than one thread within the array this is a private message
     * which belongs to two private threads.
     *
     * @param object the json object containing all data for the BlubbMessage.
     * @return thread id of the message from the json object or the private thread id
     * of the actual user if this is a private message.
     */
    private String getThreadID(JSONObject object) {
        try {
            JSONArray threads = object.getJSONArray("mThread");
            if (threads.length() == 1) return (String) threads.get(0);
            else {
                String username = SessionManager.getInstance().getActiveUsername();
                String privateThreadName = "@" + username;
                for (int i = 0; i < threads.length(); i++) {
                    if (threads.get(i).equals(privateThreadName)) return (String) threads.get(i);
                }
                return (String) threads.get(0);
            }
        } catch (JSONException e) {
            Log.e("BlubbMessage", "got the wrong json object, there was no array for threadId");
            return null;
        }
    }

    /**
     * Get the title of the message.
     *
     * @return The title for of the message. May be 'NULL' or 'UNDEFINED'.
     */
    public String getmTitle() {
        return mTitle;
    }

    /**
     * Set the title of the message.
     *
     * @param mTitle String for the new title.
     */
    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * Get the id of the message.
     *
     * @return Id string of the message.
     */
    public String getmId() {
        return mId;
    }

    /**
     * Get the type of the message.
     *
     * @return Type of the message.
     */
    public String getmType() {
        return mType;
    }

    /**
     * Get the id of the creator of the message.
     *
     * @return Id string of the creator of the message.
     */
    public String getmCreator() {
        return mCreator;
    }

    /**
     * Get the role of the message creator, 'admin', 'PL' or 'user'.
     *
     * @return String of the creators role.
     */
    public String getmCreatorRole() {
        return mCreatorRole;
    }

    /**
     * Get the messages pictures string. This should be used with the BeapIconic font.
     * 'U' shows the shape of a person.
     *
     * @return 'U' if the link is undefined or '@' if this message is a reply.
     */
    public String getmPicString() {
        if (mLink.equals(BPC.UNDEFINED)) return "U";
        else return "@";
    }

    /**
     * Get the date of the creation of the message.
     *
     * @return Date object with the time set to the creation of the message.
     */
    @SuppressWarnings("UnusedDeclaration")
    public Date getMessageDate() {
        return mDate;
    }

    /**
     * Get the thread id of the parent thread of this message.
     *
     * @return String with the parent threads id.
     */
    public String getmThread() {
        return mThread;
    }

    /**
     * Get the content of this message.
     *
     * @return MContent object showing the content of the message.
     */
    public MContent getmContent() {
        return mContent;
    }

    /**
     * Set the content for the message.
     *
     * @param mContent New MContent object replacing the old content.
     */
    public void setmContent(MContent mContent) {
        this.mContent = mContent;
    }

    /**
     * Get a string representation of the creation date of the message.
     *
     * @return String with the formatted date.
     */
    public String getmDate() {
        return BPC.parseDate(mDate);
    }

    /**
     * Create a View to display the message in the user interface
     *
     * @param context                 The android context for the View.
     * @param parent                  Parent ViewGroup for the messages View.
     * @param tCreator                Creator of the current thread. If the message creator and the
     *                                thread creator are the same the message will be highlighted.
     * @param replyClickListener      OnClickListener for the replyButton.
     * @param privateMsgClickListener OnClickListener to write a private message to
     *                                the creator of this message.
     * @param adapter                 The array adapter for the message. If this message is a reply
     *                                to another message with a click on the '@' the array adapter
     *                                will scroll to the position of the replied message.
     * @return The created View displaying this message in the user interface.
     */
    public View createView(Context context, final ViewGroup parent,
                           String tCreator, View.OnClickListener replyClickListener,
                           View.OnClickListener privateMsgClickListener,
                           final ActivitySingleThread.MessageArrayAdapter adapter) {
        Log.i(NAME, "parentType: " + parent.getClass().getName());
        View messageView;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (ownMessage(context)) {
            messageView = inflater.inflate(R.layout.message_own_layout, parent, false);
        } else {
            messageView = inflater.inflate(R.layout.message_default_layout, parent, false);
        }

        LinearLayout backLayout = (LinearLayout) messageView.findViewById(R.id.message_back_ll);

        int backLevel = 0;
        if (tCreator.equals(this.mCreator)) {
            backLevel++;
        }
        if (isNew()) {
            backLevel += 2;
        }
        backLayout.getBackground().setLevel(backLevel);

        TextView mTitle = (TextView) messageView.findViewById(R.id.message_title_tv),
                mCreator = (TextView) messageView.findViewById(R.id.message_creator_tv),
                mDate = (TextView) messageView.findViewById(R.id.message_date_tv),
                mRole = (TextView) messageView.findViewById(R.id.message_role_tv),
                mPic = (TextView) messageView.findViewById(R.id.message_profile_pic);
        Button replyBtn = (Button) messageView.findViewById(R.id.message_reply_btn);
        View mContent = messageView.findViewById(R.id.message_content_v);

        LinearLayout rightLL = (LinearLayout) messageView.findViewById(R.id.message_right_ll);
        rightLL.removeView(mContent);
        rightLL.addView(getmContent().getContentView(context), 3);

        mTitle.setText(this.getmTitle());

        mCreator.setText(this.getmCreator());
        mCreator.setOnClickListener(privateMsgClickListener);
        mDate.setText(this.getmDate());
        mRole.setText(this.getmCreatorRole());

        Typeface tf = Typeface.createFromAsset(context.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, mPic);
        BlubbApplication.setLayoutFont(tf, replyBtn);
        replyBtn.setOnClickListener(replyClickListener);

        if (!mLink.equals(BPC.UNDEFINED)) {
            mPic.setText("@");
            mPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkPos = adapter.getMsgPosition(mLink);
                    ((ListView) parent).smoothScrollToPosition(linkPos + 1);
                }
            });
        }
        if (mCreatorRole.equals("admin")) {
            mPic.setTextColor(context.getResources().getColor(R.color.beap_red));
        } else if (mCreatorRole.equals("PL")) {
            mPic.setTextColor(context.getResources().getColor(R.color.beap_medium_yellow));
        } else {
            mPic.setTextColor(context.getResources().getColor(R.color.beap_dark_blue));
        }
        return messageView;
    }

    /**
     * Get the View representation of this message.
     *
     * @param context                 The android context for the View.
     * @param parent                  Parent ViewGroup for the messages View.
     * @param tCreator                Creator of the current thread. If the message creator and the
     *                                thread creator are the same the message will be highlighted.
     * @param replyClickListener      OnClickListener for the replyButton.
     * @param privateMsgClickListener OnClickListener to write a private message to
     *                                the creator of this message.
     * @param adapter                 The array adapter for the message. If this message is a reply
     *                                to another message with a click on the '@' the array adapter
     *                                will scroll to the position of the replied message.
     * @return The created View displaying this message in the user interface.
     */
    public View getView(Context context, final ViewGroup parent,
                        String tCreator, View.OnClickListener replyClickListener,
                        View.OnClickListener privateMsgClickListener,
                        final ActivitySingleThread.MessageArrayAdapter adapter) {
        if (msgView == null) {
            linkPos = adapter.getMsgPosition(mLink);
            msgView = createView(context, parent, tCreator, replyClickListener,
                    privateMsgClickListener, adapter);
        }
        return msgView;
    }

    /**
     * Finds out whether the current user is the creator of this message.
     *
     * @param context The android context for the application.
     * @return True if the current user is the creator of this message.
     */
    private boolean ownMessage(Context context) {
        String userId = SessionManager.getInstance().getUserId(context);
        return (userId.equals(this.mCreator));
    }

    /**
     * Get a very simple string representation of the message only showing
     * thread id, message title and message content.
     *
     * @return a simple string representation of this message.
     */
    public String toString() {
        return "mThread: " + mThread +
                "mTitle: " + mTitle +
                "mContent: " + mContent.getStringRepresentation();
    }

    /**
     * @return True if the user has not seen the message yet.
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * Set the isNew flag of the message.
     *
     * @param isNew New value for the isNew flag.
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Compares two messages to find out whether they are the same.
     *
     * @param other BlubbMessage object this one will be compared to.
     * @return True if all fields are equal except the isNew flag.
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(BlubbMessage other) {
        if (!this.mId.equals(other.mId)) return false;
        if (!this.mType.equals(other.mType)) return false;
        if (!this.mCreator.equals(other.mCreator)) return false;
        if (!this.mCreatorRole.equals(other.mCreatorRole)) return false;
        if (!this.mDate.equals(other.mDate)) return false;
        if (!this.mThread.equals(other.mThread)) return false;
        if (!this.mTitle.equals(other.mTitle)) return false;
        if (!this.mContent.equals(other.mContent)) return false;
        return true;
    }

    /**
     * Get the message link of this message.
     * May return 'UNDEFINED' or 'NULL'.
     *
     * @return The id of the replied message.
     */
    public String getmLink() {
        return mLink;
    }

    /* TODO Change to an OnClickListener on a button. */

    /**
     * Set an OnLongClickListener to the Content of the messages view.
     *
     * @param context  The android context for the application.
     * @param listener OnLongClickListener for the message content.
     */
    public void
    setOnContentLongClickListener(Context context, View.OnLongClickListener listener) {
        if (msgView == null) return;
        //View content = msgView.findViewById(R.id.message_content_v);
        this.getmContent().getContentView(context).setOnLongClickListener(listener);
    }
}
