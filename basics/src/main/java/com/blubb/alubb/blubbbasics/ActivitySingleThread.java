package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.DatabaseHandler;
import com.blubb.alubb.basics.MessageManager;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.basics.TextContent;
import com.blubb.alubb.basics.ThreadManager;
import com.blubb.alubb.beapcom.BPC;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Activity that shows BlubbMessages to a thread.
 * The user can read messages, write, modify and reply to existing messages.
 */
public class ActivitySingleThread extends Activity {

    /**
     * Name for Logging purposes.
     */
    public static final String NAME = "SingleThreadActivity";

    /**
     * Key for the thread id extra of the calling intend.
     */
    public static final String EXTRA_THREAD_ID = "threadId";

    /**
     * The list of messages shown in the activity.
     */
    public List<BlubbMessage> messages;

    /**
     * The thread id of the messages shown in the activity.
     */
    private String threadId;

    /**
     * The thread of the messages shown in the activity.
     */
    private BlubbThread thread;

    /**
     * Field to access the ListView of the messages.
     */
    private ListView messageListView;

    /**
     * The last position of the messageListView.
     */
    private int lastPosition;

    /**
     * Creates the activity for a single thread.
     * Sets the content view, reads the thread id extra from the intend
     * initializes the thread and messageListView fields and calls start().
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(NAME, "onCreate()");
        setContentView(R.layout.activity_single_thread);
        Intent intent = getIntent();
        this.threadId = intent.getStringExtra(EXTRA_THREAD_ID);
        this.thread = ThreadManager.getInstance().getThreadFromSqlite(this, threadId);
        messageListView = (ListView) findViewById(R.id.single_thread_listview);
        start();
    }

    /**
     * Fills the messageListView first with the messages form sqlite and calls the
     * AsyncGetAllMessagesToThread.
     */
    @Override
    protected void onResume() {
        // solved by Dralangus http://stackoverflow.com/a/7414659/294884
        super.onResume();
        this.fillListWithMessages(MessageManager.getInstance()
                .getAllMessagesForThreadFromSqlite(this, threadId));
        AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);
        asyncTask.execute();
    }

    /**
     *
     */
    protected void start() {
        ThreadManager.getInstance().readingThread(this, threadId);
        String tDescription = thread.gettDesc();
        String tTitle = thread.getThreadTitle();
        setTitle(tTitle + " - " + thread.gettCreator());
        addHeader(tDescription);
        startInputView();
        //AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);
        //asyncTask.execute();
    }

    /**
     * Adds the Header (the thread description) to the messageListView.
     *
     * @param tDescription The description of a thread for the header.
     */
    private void addHeader(String tDescription) {
        ListView lv = (ListView) findViewById(R.id.single_thread_listview);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout header = (LinearLayout)
                inflater.inflate(R.layout.single_thread_header, lv, false);

        TextView headerText = (TextView) header.findViewById(R.id.single_thread_header_tv);
        headerText.setText(tDescription);

        lv.addHeaderView(header);
    }

    /**
     * Get the custom BlubbApplication, e.g. to handle exceptions.
     *
     * @return The custom Application instance.
     */
    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    /**
     * Set the menu of single_thread_actions.
     *
     * @param menu The menu for this activity.
     * @return True if the menu could be set.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_thread_actions, menu);
        return true;
    }

    /**
     * Starts the actions intended for the menu items.
     * - new_message_action     -> starts the new message dialog.
     * - menu_action_refresh    -> reloads the messages from the beapDB.
     *
     * @param item Menu item selected.
     * @return True if action could be performed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_message_action:
                EditText inputET = (EditText) findViewById(R.id.message_input_et);
                inputET.requestFocus();
                startInputView();
                InputMethodManager imm = (InputMethodManager) ActivitySingleThread.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(inputET, 0);
                break;
            case R.id.menu_action_refresh:
                new AsyncGetAllMessagesToThread(threadId).execute();
                break;
        }
        return true;
    }

    /**
     * Calls super.onStop() and sets all messages isNew() flag to false
     * since the user has seen them.
     */
    @Override
    public void onStop() {
        super.onStop();
        for (BlubbMessage m : messages) {
            if (m.isNew()) {
                m.setNew(false);
                DatabaseHandler databaseHandler = new DatabaseHandler(this);
                databaseHandler.setMessageRead(m.getmId());
                databaseHandler.setThreadNewMsgs(threadId, false);
            }
        }
    }

    /**
     * Activates the spinner indicating that messages are loading from  the beapDB.
     */
    public void spinnerOn() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        pb.setVisibility(View.VISIBLE);
    }

    /**
     * Deactivates the spinner indicating that messages are loading from  the beapDB.
     */
    public void spinnerOff() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        pb.setVisibility(View.INVISIBLE);
    }

    /**
     * Setup the input view for new messages.
     */
    private void startInputView() {
        final Button yBtn = (Button) findViewById(R.id.y_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        inputET.setMovementMethod(new ScrollingMovementMethod());
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        inputET.setText("");
        inputET.setHint(getString(R.string.message_new_content_hint));
        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncSendMessage().execute(
                        BPC.UNDEFINED, inputET.getText().toString(), "", threadId);
                showInput(false, inputET);
                startInputView();
                inputET.requestFocus();
            }
        });
    }

    /**
     * Setup the input view to reply to a message.
     *
     * @param replyTo The BlubbMessage to which will be replied to.
     */
    private void replyToMessage(final BlubbMessage replyTo) {
        final Button yBtn = (Button) findViewById(R.id.y_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        inputET.requestFocus();
        inputET.setHint(getString(R.string.message_reply_hint) + replyTo.getmCreator());
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        showInput(true, inputET);
        yBtn.setText("y");
        final String atMessage = "@" + replyTo.getmCreator() + "\n";

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "Hello Everyone";
                SpannableString ss1 = new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(2f), 0, 5, 0); // set size
                String titleString = replyTo.getmTitle();
                String contentString = atMessage + inputET.getText().toString();
                new AsyncSendMessage().execute(titleString, contentString,
                        replyTo.getmId(), threadId);
                showInput(false, inputET);
                clearTVs(inputET);
                startInputView();
            }
        });
    }

    /**
     * Setup the input view to change a messages content.
     *
     * @param toChange The BlubbMessage which will be modified.
     */
    private void changeMessage(final BlubbMessage toChange) {
        final Button yBtn = (Button) findViewById(R.id.y_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        inputET.requestFocus();
        //TODO Must be changed if other message contents are available.
        inputET.setText(toChange.getmContent().getStringRepresentation());
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        showInput(true, inputET);
        yBtn.setText("y");

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleString = toChange.getmTitle();
                String contentString = inputET.getText().toString();
                toChange.setmTitle(titleString);
                toChange.setmContent(new TextContent(contentString));
                new AsyncSetMessage().execute(toChange);
                showInput(false, inputET);
                clearTVs(inputET);
                startInputView();
            }
        });
    }

    /**
     * Clears the text of some TextViews.
     *
     * @param views The TextViews that will be cleared.
     */
    private void clearTVs(TextView... views) {
        for (TextView tv : views) {
            tv.setText("");
        }
    }

    /**
     * Show the soft keyboard.
     *
     * @param show True if it should be shown, false if it should be hidden.
     * @param view The currently focused view, which would like to receive
     *             soft keyboard input.
     */
    private void showInput(boolean show, View view) {
        InputMethodManager imm = (InputMethodManager) ActivitySingleThread.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.showSoftInput(view, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Fills the messageListView with a list of BlubbMessages.
     *
     * @param messages The list of BlubbMessages to be displayed in the messageListView.
     */
    private void fillListWithMessages(List<BlubbMessage> messages) {
        if (messages != null) {
            this.messages = messages;
            ListView lv = (ListView) findViewById(R.id.single_thread_listview);
            Collections.reverse(messages);
            final MessageArrayAdapter adapter = new MessageArrayAdapter(
                    ActivitySingleThread.this, R.layout.message_layout, messages);
            lv.setAdapter(adapter);
            messageListView.smoothScrollToPosition(lastPosition + 1);
        }
    }

    /**
     * Array adapter for the messageListView.
     */
    public class MessageArrayAdapter extends ArrayAdapter<BlubbMessage> {

        /**
         * HashMap to easily find the adapter id of a message.
         */
        HashMap<BlubbMessage, Integer> mIdMap = new HashMap<BlubbMessage, Integer>();

        /**
         * Constructor for the MessageArrayAdapter.
         *
         * @param context            The current context.
         * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
         *                           instantiating views.
         * @param objects            The objects to represent in the ListView.
         */
        public MessageArrayAdapter(Context context, int textViewResourceId,
                                   List<BlubbMessage> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        /**
         * Get the position of a message within the array adapter.
         *
         * @param msgId The id of the message.
         * @return Integer representing the position of the message with message id = msgId.
         */
        public int getMsgPosition(String msgId) {
            Set<BlubbMessage> msgSet = mIdMap.keySet();
            for (BlubbMessage m : msgSet) {
                if (m.getmId().equals(msgId)) return mIdMap.get(m);
            }
            return 0;
        }

        /**
         * Get a View of a BlubbMessage and set the OnClickListener for the reply button and the
         * edit button.
         *
         * @param position    Position of the message.
         * @param convertView --
         * @param parent      The parent view group for the view.
         * @return View representing a BlubbMessage at the user interface.
         */
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final BlubbMessage message = getItem(position);
            View msg = message.getView(ActivitySingleThread.this, parent, thread.gettCreator(),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            replyToMessage(message);
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //createPrivateMessage(message);
                        }
                    }, this
            );
            String username = SessionManager.getInstance().getActiveUsername();
            //Add a long click listener if it's a message of the user.
            if (message.getmCreator().equals(username)) {
                message.setOnContentLongClickListener(this.getContext(),
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                changeMessage(message);
                                lastPosition = position;
                                return false;
                            }
                        }
                );
            }
            return msg;
        }

        @Override
        public long getItemId(int position) {
            BlubbMessage item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    /**
     * AsyncTask to get all messages for a thread form the beapDB server.
     */
    private class AsyncGetAllMessagesToThread extends AsyncTask<Void, Void, List<BlubbMessage>> {
        /**
         * The thread id for the messages.
         */
        private String threadId;

        /**
         * Exception caught while executing doInBackground.
         */
        private Exception e;

        /**
         * Constructor for the AsyncTask. Starts the spinner and sets the threadId for the AT.
         *
         * @param threadId The thread id for the messages.
         */
        public AsyncGetAllMessagesToThread(String threadId) {
            spinnerOn();
            this.threadId = threadId;
        }

        /**
         * Executes the request for all messages of a certain thread at the MessageManager.
         *
         * @param voids ...
         * @return List of BlubbMessages or null if a exception has been caught.
         */
        @Override
        protected List<BlubbMessage> doInBackground(Void... voids) {
            Log.v(NAME, "AsyncGetAllMessagesToThread.execute(thread = " + threadId + ")");
            try {
                return MessageManager.getInstance().getAllMessagesForThread(
                        ActivitySingleThread.this.getApplicationContext(), this.threadId);
            } catch (Exception e) {
                this.e = e;
                return null;
            }
        }

        /**
         * Stops the spinner and fills the messageListView with the current messages.
         *
         * @param response List of BlubbThreads.
         */
        @Override
        protected void onPostExecute(final List<BlubbMessage> response) {
            getApp().handleException(e);
            fillListWithMessages(response);
            spinnerOff();
        }
    }

    /**
     * AsyncTask to send a new message to the beapDB.
     */
    private class AsyncSendMessage extends AsyncTask<String, String, Boolean> {

        /**
         * Exception caught while executing doInBackground.
         */
        private Exception exception;

        /**
         * Executes the create message at the MessageManager.String parameter for the new message:
         *
         * @param parameter {mTitle, mContent, mLink, tId1, tId2,...}
         * @return True if the message has been created.
         */
        @Override
        protected Boolean doInBackground(String... parameter) {
            try {
                return MessageManager.getInstance().createMsg(
                        ActivitySingleThread.this.getApplicationContext(),
                        parameter);
            } catch (Exception e) {
                this.exception = e;
                return false;
            }
        }

        /**
         * If a message has been created, this makes a toast and reloads the messages.
         *
         * @param isCreated Indicates whether the message has been created.
         */
        @Override
        protected void onPostExecute(Boolean isCreated) {
            getApp().handleException(exception);
            if (isCreated) {
                String msg = getResources().getString(R.string.create_message_confirmation_toast);
                Log.i(NAME, msg);
                fillListWithMessages(MessageManager.getInstance()
                        .getAllMessagesForThreadFromSqlite(ActivitySingleThread.this, threadId));
                Toast.makeText(ActivitySingleThread.this, msg, Toast.LENGTH_SHORT).show();

            }
        }
    }

    /**
     * Change a messages title or content.
     */
    private class AsyncSetMessage extends AsyncTask<BlubbMessage, Void, Boolean> {
        /**
         * Exception caught while executing doInBackground.
         */
        private Exception exception;

        /**
         * Executes the setMsg(..) at the MessageManager.
         *
         * @param parameter {mTitle, mContent, mLink, tId1, tId2,...}
         * @return True if the message could be modified.
         */
        @Override
        protected Boolean doInBackground(BlubbMessage... parameter) {
            try {
                return MessageManager.getInstance().setMsg(ActivitySingleThread.this, parameter[0]);
            } catch (Exception e) {
                this.exception = e;
                return false;
            }
        }

        /**
         * If a message has been modified, this makes a toast and reloads the messages.
         *
         * @param wasModified Indicates whether the message could be modified.
         */
        protected void onPostExecute(Boolean wasModified) {
            getApp().handleException(exception);
            if (wasModified) {
                String msg = getResources().getString(R.string.modify_message_confirmation_toast);
                Log.i(NAME, msg);
                fillListWithMessages(MessageManager.getInstance()
                        .getAllMessagesForThreadFromSqlite(ActivitySingleThread.this, threadId));
                Toast.makeText(ActivitySingleThread.this, msg, Toast.LENGTH_SHORT).show();

            }
        }
    }

}
