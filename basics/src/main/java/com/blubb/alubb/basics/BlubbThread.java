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

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Class representing a blubb thread where user
 * can post different kinds of messages.
 * 'Thread' means in the whole doc the BlubbThread and not java.lang.Thread.
 */
@SuppressWarnings("ALL")
public class BlubbThread {
    /**
     * Name for Logging purposes
     */
    private static final String NAME = "BlubbThread";
    /**
     * Static final fields to compare the thread status.
     */
    private static final String TS_SOLVED = "solved", TS_CLOSED = "closed", TS_OPEN = "open";
    /**
     * Static final fields for the different thread types, like they are written in the beap db.
     */
    private static final String CHAT_THREAD = "Thread";
    /**
     * Static integer representing the different beap colors red, medium yellow, blue and green.
     */
    private static int red, yellow, blue, green;
    /**
     * tTitle of the thread.
     */
    private String tTitle;
    /**
     * Description for this thread.
     */
    private String tDesc;
    /**
     * The id of the creator of the thread.
     */
    private String tCreator;
    /**
     * The role of the creator of the thread.
     */
    private String tCreatorRole;
    /**
     * Status of the thread, e.g. open, closed or solved.
     */
    private ThreadStatus tStatus;
    /**
     * Date when the thread has been created at the beap database.
     */
    private Date tDate;
    /**
     * Unique id of the thread.
     */
    private String tId;
    /**
     * Number of messages belonging to this thread.
     */
    private int tMsgCount;
    /**
     * Flag whether the current user has seen the thread yet.
     */
    private boolean isNew;
    /**
     * Flag whether there are new messages for the thread the current user has not seen yet.
     */
    private boolean hasNewMsgs;
    /**
     * Flag for whether the details of the thread are displayed now.
     */
    private boolean isBig = false;
    /**
     * Type of this thread, e.g. chat thread, poll thread, task thread.
     */
    private ThreadType tType;
    /**
     * Different views showing the thread in the user interface, bigView provides more details
     * like the thread description.
     */
    private View bigView, smallView;

    /**
     * Constructor for the BlubbThread with a json object providing all needed information.
     * The flags 'isNew' and 'hasNewMsgs'  will be set to true since the threads are
     * just created with a json object when they are new.
     *
     * @param jsonObject Json object with all data needed to create a BlubbThread object.
     *                   {
     *                   "tId" : "t2014-05-17_150000_S-Gross",
     *                   "tType" : "Thread",
     *                   "tCreator" : "S-Gross",
     *                   "tCreatorRole" : "admin",
     *                   "tDate" : "2014-05-17:T15:00:00.000Z",
     *                   "tTitle" : "Thread No. 1",
     *                   "tDescr" : "Weil,\nich bin der Admin\nund ich darf das",
     *                   "tMsgCount" : 2
     *                   }
     */
    public BlubbThread(JSONObject jsonObject) {
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
        if (jsonObject.has("tMsgCount")) try {
            this.tMsgCount = jsonObject.getInt("tMsgCount");
        } catch (JSONException e) {
            Log.e(NAME, e.getMessage());
        }
        this.tId = BPC.findStringInJsonObj(jsonObject, "tId");
        this.tType = parseThreadType(BPC.findStringInJsonObj(jsonObject, "tType"));
        this.tStatus = parseThreadStatus(BPC.findStringInJsonObj(jsonObject, "tStatus"));
        this.isNew = true;
        this.hasNewMsgs = true;
    }

    /**
     * Constructor for a BlubbThread which needs all fields to be provided.
     *
     * @param tId       Unique id of the thread.
     * @param tTitle    tTitle of the thread.
     * @param tDesc     Description for this thread.
     * @param tCreator  The id of the creator of the thread.
     * @param tCRole    The role of the creator of the thread.
     * @param tStatus   Status of the thread, e.g. open, closed or solved.
     * @param tDate     Date when the thread has been created at the beap database.
     * @param tMsgCount Number of messages belonging to this thread.
     * @param tType     Type of this thread, e.g. chat thread, poll thread, task thread.
     * @param isNew     Flag whether the current user has seen the thread yet.
     * @param hasNewMsg Flag whether there are new messages for the thread the
     *                  current user has not seen yet.
     */
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
        this.tType = parseThreadType(tType);
        this.isNew = (isNew == 1);
        this.hasNewMsgs = (hasNewMsg == 1);
    }

    /**
     * Makes a ThreadType out of a string for the thread type.
     *
     * @param tType String with the thread type, e.g. 'Thread' for CHAT_THREAD.
     * @return The type of thread represented by tType or UNDEFINED if unknown.
     */
    private ThreadType parseThreadType(String tType) {
        if (tType.equals(BlubbThread.CHAT_THREAD)) return ThreadType.CHAT_THREAD;
        return ThreadType.UNDEFINED;
    }

    /**
     * Makes a ThreadStatus out of a string representing the thread status.
     *
     * @param threadStatus String with the thread status.
     * @return ThreadStatus represented by threadStatus or OPEN if unknown.
     */
    private ThreadStatus parseThreadStatus(String threadStatus) {
        Log.v(NAME, "parseThreadStatus(tStatus=" + threadStatus + ")");
        if (threadStatus.equals(TS_CLOSED)) return ThreadStatus.CLOSED;
        if (threadStatus.equals(TS_SOLVED)) return ThreadStatus.SOLVED;
        else return ThreadStatus.OPEN;
    }

    /**
     * Get a string representation of the thread.
     *
     * @return Very simple string representing the thread.
     */
    public String toString() {
        return "Thread name:\t\t" + this.tTitle;
    }

    /**
     * Get the thread description.
     *
     * @return String with the thread description.
     */
    public String gettDesc() {
        return tDesc;
    }

    /**
     * Set the description for the thread.
     *
     * @param tDesc New description for the thread.
     */
    public void settDesc(String tDesc) {
        this.tDesc = tDesc;
    }

    /**
     * Get the id of the threads creator.
     *
     * @return String with the creators id.
     */
    public String gettCreator() {
        return tCreator;
    }

    /**
     * Get the role of the creator of the thread.
     *
     * @return String with the role of the creator, 'admin', 'PL' or 'user'.
     */
    public String gettCreatorRole() {
        return tCreatorRole;
    }

    /**
     * Get the date when the thread has been created.
     *
     * @return String representation of the creation date of the thread.
     */
    public String gettDate() {
        return BPC.parseDate(tDate);
    }

    /**
     * Get the number of messages the thread owns.
     *
     * @return Number of messages of this thread.
     */
    public int gettMsgCount() {
        return tMsgCount;
    }

    /**
     * Get the unique id of the thread.
     *
     * @return String with the threads id.
     */
    public String gettId() {
        return tId;
    }

    /**
     * Get the type of this thread.
     *
     * @return ThreadType of the thread.
     */
    public ThreadType gettType() {
        return tType;
    }

    /**
     * Get the title of the thread.
     *
     * @return String with the threads title.
     */
    public String getThreadTitle() {
        return this.tTitle;
    }

    /**
     * Get a string representing the threads status.
     *
     * @return String representation of the thread status.
     */
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

    /**
     * Set the title for the thread.
     *
     * @param tTitle New title for the thread.
     */
    public void settTitle(String tTitle) {
        this.tTitle = tTitle;
    }

    /**
     * Set the threads status.
     *
     * @param tStatus New status for the thread.
     */
    public void settStatus(String tStatus) {
        this.tStatus = parseThreadStatus(tStatus);
    }

    /**
     * Set the value of the hasNewMsgs flag.
     *
     * @param hasNewMsgs Boolean for hasNewMsgs.
     */
    public void setHasNewMsgs(boolean hasNewMsgs) {
        this.hasNewMsgs = hasNewMsgs;
    }

    /**
     * Get the value of the isNew flag.
     *
     * @return True if the current user has seen the thread.
     */
    public boolean isNew() {
        return this.isNew;
    }

    /**
     * Set the isNew flag, e.g. when the thread is displayed at the user interface.
     *
     * @param isNew New value for isNew.
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Get the value of hasNewMsgs, showing whether this thread has messages the current
     * user has not seen yet.
     *
     * @return True if the user has not seen all the messages of the thread.
     */
    public boolean hasNewMsgs() {
        return this.hasNewMsgs;
    }

    /**
     * Compare the thread to another thread.
     *
     * @param other BlubbThread this thread will be compared with.
     * @return True if all fields of the thread are equal.
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(BlubbThread other) {
        if (!this.gettId().equals(other.gettId())) return false;
        if (this.gettMsgCount() != other.gettMsgCount()) return false;
        if (!this.gettCreator().equals(other.gettCreator())) return false;
        if (!this.hasNewMsgs == other.hasNewMsgs) return false;
        if (!this.isNew == other.isNew) return false;
        if (!this.gettCreatorRole().equals(other.gettCreatorRole())) return false;
        if (!this.gettDate().equals(other.gettDate())) return false;
        if (!this.gettDesc().equals(other.gettDesc())) return false;
        if (!this.getThreadTitle().equals(other.getThreadTitle())) return false;
        if (!this.gettType().equals(other.gettType())) return false;
        return true;
    }

    /**
     * Toggle the view given by getView(..)
     * Can be a big view with details or just the mandatory information.
     */
    public void toggleViewSize() {
        isBig = !isBig;
    }

    /**
     * Get the View representing a BlubbThread on the user interface.
     * Due to the flag isBig, toggled by toggleViewSize(), it is a View showing
     * Title, MsgCount and Status or the description and a button to edit the threads
     * title and description.
     *
     * @param context The application context for the view.
     * @param parent  The parent ViewGroup for the view.
     * @return View for the BlubbThread.
     */
    public View getView(Context context, ViewGroup parent) {
        if (isBig) return this.getBigView(context, parent);
        else return this.getSmallView(context, parent);
    }

    /**
     * Get a string representing the creation date of the thread.
     *
     * @return String with the creation date of the thread.
     */
    public String getFormattedDate() {
        return BPC.parseDate(tDate);
    }

    /**
     * Get the View with less details.
     *
     * @param context The application context for the view.
     * @param parent  The parent ViewGroup for the view.
     * @return A simple View without description and details of the thread.
     */
    public View getSmallView(Context context, ViewGroup parent) {
        if (this.smallView == null) this.smallView = createSmallView(context, parent);
        return this.smallView;
    }

    /**
     * Creates the small, less detailed view for the thread.
     *
     * @param context The application context for the view.
     * @param parent  The parent ViewGroup for the view.
     * @return A simple View without description and details of the thread.
     */
    private View createSmallView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.thread_list_entry_small, parent, false);
        return this.setContentForHeader(layout);
    }

    /**
     * Get the View with all details available for the thread.
     *
     * @param context The application context for the view.
     * @param parent  The parent ViewGroup for the view.
     * @return A View with description and buttons for the actions available.
     */
    public View getBigView(Context context, ViewGroup parent) {
        if (this.bigView == null) this.bigView = createBigView(context, parent);
        return bigView;
    }

    /**
     * Creates the big, detailed view for the thread.
     * The buttons need to be provided with OnClickListener by calling
     *
     * @param context The application context for the view.
     * @param parent  The parent ViewGroup for the view.
     * @return A View with description and buttons for the actions available.
     */
    private View createBigView(Context context, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(
                R.layout.thread_list_entry, parent, false);

        layout = this.setContentForHeader(layout);
        TextView tdescr = (TextView) layout.findViewById(R.id.thread_list_item_description),
                tInfo = (TextView) layout.findViewById(R.id.thread_list_item_info);

        tdescr.setText(this.tDesc);
        String info = this.tCreatorRole + " - " + this.getFormattedDate();
        tInfo.setText(info);
        return layout;
    }

    /**
     * Set the icon for the thread status.
     *
     * @param tIcon TextView showing the icon on the thread view.
     * @return tIcon TextView with the right Typeface, color and text set to get the status icon.
     */
    private TextView setIcon(TextView tIcon) {
        Typeface tf = Typeface.createFromAsset(tIcon.getContext().getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, tIcon);
        setColorInts(tIcon.getContext());
        switch (tStatus) {
            case CLOSED:
                tIcon.setText("Z");
                tIcon.setTextColor(red);
                break;
            case SOLVED:
                tIcon.setText("y");
                tIcon.setTextColor(green);
                break;
            default:
                tIcon.setText("U");
                if (tCreatorRole.equals("admin")) {
                    tIcon.setTextColor(red);
                } else if (tCreatorRole.equals("PL")) {
                    tIcon.setTextColor(yellow);
                } else {
                    tIcon.setTextColor(blue);
                }
                break;
        }
        return tIcon;
    }

    /**
     * Set the content for the header of the thread view. The header is the part
     * of the thread view which is on the small and big view the same.
     *
     * @param layout R.layout.thread_list_entry layout.
     * @return Layout with all values properly set.
     */
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

    /**
     * Set the color values to easy access it from within the class.
     * Prevents to call context.getResources().getColor(..) each time a color is needed.
     *
     * @param context The application context.
     */
    private void setColorInts(Context context) {
        if (red == 0 || yellow == 0 || blue == 0 || green == 0) {
            red = context.getResources().getColor(R.color.beap_red);
            yellow = context.getResources().getColor(R.color.beap_medium_yellow);
            blue = context.getResources().getColor(R.color.beap_dark_blue);
            green = context.getResources().getColor(R.color.beap_green);
        }
    }

    /**
     * Different statuses for a thread.
     * It can be open, everybody can write messages in it,
     * solved or closed and nobody can write in it.
     */
    public enum ThreadStatus {
        OPEN, SOLVED, CLOSED
    }

}
