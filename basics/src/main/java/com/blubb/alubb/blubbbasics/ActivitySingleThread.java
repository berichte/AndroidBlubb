package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
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
import com.blubb.alubb.basics.ThreadManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class ActivitySingleThread extends Activity {
    public static final String NAME = "SingleThreadActivity";
    public static final String EXTRA_THREAD_ID = "threadId";
    public static final String EXTRA_THREAD_TITLE = "threadTitle",
            EXTRA_THREAD_CREATOR = "threadCreator",
            EXTRA_THREAD_DESCRIPTION = "threadDescription";
    public List<BlubbMessage> messages;
    private String titleInput = "", contentInput = "";
    private String threadId;
    private BlubbThread thread;
    private boolean showSpinner = false, storeDraft = true;
    private InputState inputState = InputState.TITLE;
    private ListView messageListView;
    private int lastPosition;
    private String atMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(NAME, "onCreate()");
        setContentView(R.layout.activity_single_thread);
        Intent intent = getIntent();
        this.threadId = intent.getStringExtra(EXTRA_THREAD_ID);
        AsyncGetThread asyncGetThread = new AsyncGetThread();
        asyncGetThread.execute();
        messageListView = (ListView) findViewById(R.id.single_thread_listview);
    }

    protected void start() {
        ThreadManager.getInstance().readingThread(this, threadId);
        Log.i("SingleThreadActivity", "Requesting Messages for Thread " + threadId);

        String tDescr = thread.gettDesc();
        String tTitle = thread.getThreadTitle();
        setTitle(tTitle + " - " + thread.gettCreator());

        addHeader(tDescr);
        startInputView();
        spinnerOn();
        //AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);
        //asyncTask.execute();
    }

    private void startInputView() {
        final Button yBtn = (Button) findViewById(R.id.y_button),
                xBtn = (Button) findViewById(R.id.x_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        final TextView titleView = (TextView) findViewById(R.id.message_input_title_tv);
        final TextView contentView = (TextView) findViewById(R.id.message_input_content_tv);
        final View msgLayout = findViewById(R.id.message_input_ll);
        contentView.setMovementMethod(new ScrollingMovementMethod());
        inputET.setMovementMethod(new ScrollingMovementMethod());

        yBtn.requestFocus();
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        BlubbApplication.setLayoutFont(tf, xBtn);

        inputET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    msgLayout.setVisibility(View.VISIBLE);
                } else msgLayout.setVisibility(View.INVISIBLE);
            }
        });
        inputET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputState == InputState.TITLE) {
                    titleView.setText(inputET.getText());
                } else if (inputState == InputState.CONTENT) {

                    contentView.setText(atMessage + inputET.getText());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (inputState) {
                    case TITLE:
                        inputState = InputState.CONTENT;
                        yBtn.setText("y");
                        xBtn.setText(".");
                        inputET.setText(contentView.getText());
                        inputET.setHint(getString(R.string.message_new_content_hint));
                        msgLayout.setVisibility(View.VISIBLE);
                        break;
                    case CONTENT:
                        inputState = InputState.TITLE;
                        yBtn.setText(":");
                        xBtn.setText("x");
                        String titleString = titleView.getText().toString();
                        String conteString = contentView.getText().toString();

                        new AsyncSendMessage().execute(threadId, titleString, conteString);
                        showInput(false, inputET);
                        clearTVs(titleView, contentView, inputET);
                        msgLayout.setVisibility(View.INVISIBLE);
                        inputET.setHint(getString(R.string.message_new_title_hint));
                        break;
                }
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (inputState) {
                    case TITLE:
                        showInput(false, inputET);
                        clearTVs(titleView, contentView, inputET);
                        msgLayout.setVisibility(View.INVISIBLE);
                        xBtn.requestFocus();
                        break;
                    case CONTENT:
                        inputState = InputState.TITLE;
                        yBtn.setText(":");
                        xBtn.setText("x");
                        inputET.setText(titleView.getText());
                        inputET.setHint(getString(R.string.message_new_title_hint));
                        break;
                }
            }
        });
    }

    private void addHeader(String tDescr) {
        ListView lv = (ListView) findViewById(R.id.single_thread_listview);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout header = (LinearLayout)
                inflater.inflate(R.layout.single_thread_header, lv, false);

        TextView headerText = (TextView) header.findViewById(R.id.single_thread_header_tv);
        headerText.setText(tDescr);

        lv.addHeaderView(header);
    }

    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_thread_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_message_action:
//                newMessageDialog();
                EditText input = (EditText) findViewById(R.id.message_input_et);
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) ActivitySingleThread.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, 0);
                break;
            case R.id.menu_action_refresh:
                new AsyncGetAllMessagesToThread(threadId).execute();
                break;
        }
        return true;
    }

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

    public void spinnerOn() {
        showSpinner = true;
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        pb.setVisibility(View.VISIBLE);
    }

    public void spinnerOff() {
        showSpinner = false;
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        pb.setVisibility(View.INVISIBLE);
    }

    protected void onResume() {

        // solved by Dralangus http://stackoverflow.com/a/7414659/294884
        super.onResume();
        AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);
        asyncTask.execute();
    }

    private void handleException(Exception e) {
        if (e != null) {
            Log.e(NAME, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void replyToMessage(final BlubbMessage replyTo) {
        final View msgLayout = findViewById(R.id.message_input_ll);
        final Button yBtn = (Button) findViewById(R.id.y_button),
                xBtn = (Button) findViewById(R.id.x_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        final TextView titleView = (TextView) msgLayout.findViewById(R.id.message_input_title_tv);
        final TextView contentView = (TextView) msgLayout.findViewById(R.id.message_input_content_tv);
        inputET.requestFocus();
        msgLayout.setVisibility(View.VISIBLE);
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        BlubbApplication.setLayoutFont(tf, xBtn);

        titleView.setText(replyTo.getmTitle());
        inputState = InputState.CONTENT;
        showInput(true, inputET);

        yBtn.setText("y");
        xBtn.setText("x");

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleString = replyTo.getmTitle();
                String conteString = contentView.getText().toString();
                new AsyncSendMessage().execute(threadId, titleString, conteString, replyTo.getmId());
                showInput(false, inputET);
                clearTVs(titleView, contentView, inputET);
                msgLayout.setVisibility(View.INVISIBLE);
                inputET.setHint(getString(R.string.message_new_title_hint));
                inputState = InputState.TITLE;
                startInputView();
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInput(false, inputET);
                clearTVs(titleView, contentView, inputET);
                msgLayout.setVisibility(View.INVISIBLE);
                xBtn.requestFocus();
                inputState = InputState.TITLE;
                startInputView();
            }
        });

        atMessage = "@" + replyTo.getmCreator() + "\n";
        TextView atSign = (TextView) msgLayout.findViewById(R.id.message_input_profile_pic);
        atSign.setText("@");
        atSign.setVisibility(View.VISIBLE);
    }

    private void changeMessage(final BlubbMessage toChange) {
        final Button yBtn = (Button) findViewById(R.id.y_button),
                xBtn = (Button) findViewById(R.id.x_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        final TextView titleView = (TextView) findViewById(R.id.message_input_title_tv);
        final TextView contentView = (TextView) findViewById(R.id.message_input_content_tv);
        final TextView creatorView = (TextView) findViewById(R.id.message_input_creator_tv);
        final TextView picView = (TextView) findViewById(R.id.message_input_profile_pic);
        final TextView dateView = (TextView) findViewById(R.id.message_input_date_tv);

        creatorView.setVisibility(View.VISIBLE);
        picView.setVisibility(View.VISIBLE);
        dateView.setVisibility(View.VISIBLE);

        inputET.setText(toChange.getmTitle());
        titleView.setText(toChange.getmTitle());
        contentView.setText(toChange.getmContent());
        picView.setText(toChange.getmPicString());
        creatorView.setText(toChange.getmCreator());
        dateView.setText(toChange.getFormatedDate());


        final View msgLayout = findViewById(R.id.message_input_ll);
        contentView.setMovementMethod(new ScrollingMovementMethod());
        inputET.setMovementMethod(new ScrollingMovementMethod());

        yBtn.requestFocus();
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        BlubbApplication.setLayoutFont(tf, xBtn);
        BlubbApplication.setLayoutFont(tf, picView);

        inputET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputState == InputState.TITLE) {
                    titleView.setText(inputET.getText());
                } else if (inputState == InputState.CONTENT) {

                    contentView.setText(atMessage + inputET.getText());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (inputState) {
                    case TITLE:
                        inputState = InputState.CONTENT;
                        yBtn.setText("y");
                        xBtn.setText(".");
                        inputET.setText(contentView.getText());
                        inputET.setHint(getString(R.string.message_new_content_hint));
                        msgLayout.setVisibility(View.VISIBLE);
                        break;
                    case CONTENT:
                        inputState = InputState.TITLE;
                        yBtn.setText(":");
                        xBtn.setText("x");
                        String titleString = titleView.getText().toString();
                        String conteString = contentView.getText().toString();


                        showInput(false, inputET);
                        clearTVs(titleView, contentView, inputET, creatorView, picView, dateView);
                        creatorView.setVisibility(View.INVISIBLE);
                        picView.setVisibility(View.INVISIBLE);
                        dateView.setVisibility(View.INVISIBLE);

                        toChange.setmTitle(titleString);
                        toChange.setmContent(conteString);
                        new AsyncSetMessage().execute(toChange);

                        msgLayout.setVisibility(View.INVISIBLE);
                        inputET.setHint(getString(R.string.message_new_title_hint));
                        startInputView();
                        break;
                }
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (inputState) {
                    case TITLE:
                        showInput(false, inputET);
                        clearTVs(titleView, contentView, inputET, creatorView, picView, dateView);
                        creatorView.setVisibility(View.INVISIBLE);
                        picView.setVisibility(View.INVISIBLE);
                        dateView.setVisibility(View.INVISIBLE);
                        msgLayout.setVisibility(View.INVISIBLE);
                        xBtn.requestFocus();
                        startInputView();
                        break;
                    case CONTENT:
                        inputState = InputState.TITLE;
                        yBtn.setText(":");
                        xBtn.setText("x");
                        inputET.setText(titleView.getText());
                        inputET.setHint(getString(R.string.message_new_title_hint));
                        break;
                }
            }
        });
        showInput(true, inputET);
        /*EditText input = (EditText) findViewById(R.id.message_input_et);
        input.requestFocus();

        final View layout = findViewById(R.id.message_input_ll);
        final ViewGroup parent = (ViewGroup) layout.getParent();
        final int index = parent.indexOfChild(layout);
        parent.removeView(layout);
        final View msgLayout = toChange.createView(this, parent, thread.gettCreator(), null, null);
        parent.addView(msgLayout, index);
        final Button yBtn = (Button) findViewById(R.id.y_button),
                xBtn = (Button) findViewById(R.id.x_button);
        final EditText inputET = (EditText) findViewById(R.id.message_input_et);
        final TextView titleView = (TextView) msgLayout.findViewById(R.id.message_title_tv);
        final TextView contentView = (TextView) msgLayout.findViewById(R.id.message_content_tv);

        contentView.setMovementMethod(new ScrollingMovementMethod());
        inputET.setMovementMethod(new ScrollingMovementMethod());

        yBtn.requestFocus();
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        BlubbApplication.setLayoutFont(tf, xBtn);

        inputET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputState == InputState.TITLE) {
                    titleView.setText(inputET.getText());
                } else if (inputState == InputState.CONTENT) {

                    contentView.setText(atMessage + inputET.getText());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (inputState) {
                    case TITLE:
                        inputState = InputState.CONTENT;
                        yBtn.setText("y");
                        xBtn.setText(".");
                        inputET.setText(contentView.getText());
                        inputET.setHint(getString(R.string.message_new_content_hint));
                        break;
                    case CONTENT:
                        inputState = InputState.TITLE;
                        yBtn.setText(":");
                        xBtn.setText("x");
                        String titleString = titleView.getText().toString();
                        String conteString = contentView.getText().toString();

                        toChange.setmTitle(titleString);
                        toChange.setmContent(conteString);

                        new AsyncSetMessage().execute(toChange);

                        showInput(false, inputET);
                        clearTVs(titleView, contentView, inputET);
                        inputET.setHint(getString(R.string.message_new_title_hint));

                        parent.removeView(msgLayout);
                        parent.addView(layout, index);

                        startInputView();
                        break;
                }
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (inputState) {
                    case TITLE:
                        showInput(false, inputET);
                        clearTVs(titleView, contentView, inputET);
                        parent.removeView(msgLayout);
                        parent.addView(layout, index);
                        startInputView();
                        break;
                    case CONTENT:
                        inputState = InputState.TITLE;
                        yBtn.setText(":");
                        xBtn.setText("x");
                        inputET.setText(titleView.getText());
                        inputET.setHint(getString(R.string.message_new_title_hint));
                        break;
                }
            }
        });*/
    }

    private void clearTVs(TextView... views) {
        for (TextView tv : views) {
            tv.setText("");
        }
    }

    private void showInput(boolean show, View view) {
        InputMethodManager imm = (InputMethodManager) ActivitySingleThread.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.showSoftInput(view, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private enum InputState {TITLE, CONTENT}

    private class AsyncGetAllMessagesToThread extends AsyncTask<Void, Void, List<BlubbMessage>> {

        private String threadId;

        public AsyncGetAllMessagesToThread(String threadId) {
            ActivitySingleThread.this.showSpinner = true;
            spinnerOn();
            this.threadId = threadId;
        }

        @Override
        protected List<BlubbMessage> doInBackground(Void... voids) {
            Log.v(NAME, "AsyncGetAllMessagesToThread.execute(thread = " + threadId + ")");
            return getApp().getMessageManager().getAllMessagesForThread(
                    ActivitySingleThread.this.getApplicationContext(), this.threadId);
        }

        @Override
        protected void onPostExecute(final List<BlubbMessage> response) {
            messages = response;
            ListView lv = (ListView) findViewById(R.id.single_thread_listview);
            Collections.reverse(response);
            final MessageArrayAdapter adapter = new MessageArrayAdapter(
                    ActivitySingleThread.this, R.layout.message_layout, response);
            lv.setAdapter(adapter);

            ActivitySingleThread.this.showSpinner = false;
            spinnerOff();
            messageListView.smoothScrollToPosition(lastPosition + 1);
        }
    }

    public class MessageArrayAdapter extends ArrayAdapter<BlubbMessage> {

        HashMap<BlubbMessage, Integer> mIdMap = new HashMap<BlubbMessage, Integer>();

        public MessageArrayAdapter(Context context, int textViewResourceId,
                                   List<BlubbMessage> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        public int getMsgPosition(String msgId) {
            Set<BlubbMessage> msgSet = mIdMap.keySet();
            for (BlubbMessage m : msgSet) {
                if (m.getmId().equals(msgId)) return mIdMap.get(m);
            }
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final BlubbMessage message = getItem(position);
            View msg = message.getView(ActivitySingleThread.this, parent, thread.gettCreator(),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            replyToMessage(message);
                        }
                    }, this
            );
            message.setOnContentLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    changeMessage(message);
                    lastPosition = position;
                    return false;
                }
            });
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

    private class AsyncGetThread extends AsyncTask<Void, String, BlubbThread> {
        private Exception e;

        @Override
        protected BlubbThread doInBackground(Void... params) {
            try {
                return ThreadManager.getInstance().getThread(ActivitySingleThread.this, threadId);
            } catch (SessionException e) {
                this.e = e;
            } catch (BlubbDBException e) {
                this.e = e;
            } catch (JSONException e) {
                this.e = e;
            } catch (BlubbDBConnectionException e) {
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(BlubbThread thread) {
            ActivitySingleThread.this.thread = thread;
            ActivitySingleThread.this.start();
        }
    }

    private class AsyncSendMessage extends AsyncTask<String, String, BlubbMessage> {

        private Exception exception;

        @Override
        protected BlubbMessage doInBackground(String... blubbs) {
            try {
                return getApp().getMessageManager().createMsg(
                        ActivitySingleThread.this.getApplicationContext(),
                        blubbs);
            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllMessages", e.getMessage());
            } catch (SessionException e) {
                this.exception = e;
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
            } catch (PasswordInitException e) {
                Log.e(NAME, e.getMessage()); // can not happen at this point.
            }
            return null;
        }

        @Override
        protected void onPostExecute(BlubbMessage message) {
            handleException(exception);
            if (message != null) {
                String msg = "Created new Message:\n" +
                        "tId: " + message.getmThread() + "\n" +
                        "tTitle: " + message.getmTitle();
                Log.i(NAME, msg);
                new AsyncGetAllMessagesToThread(threadId).execute();
                Toast.makeText(ActivitySingleThread.this, msg, Toast.LENGTH_SHORT).show();

            }
        }
    }

    private class AsyncSetMessage extends AsyncTask<BlubbMessage, Void, String> {
        @Override
        protected String doInBackground(BlubbMessage... params) {
            try {
                return MessageManager.getInstance().setMsg(ActivitySingleThread.this, params[0]);
            } catch (Exception e) {
                Log.e(NAME, e.getMessage());
                return e.getMessage();
            }
        }

        protected void onPostExecute(String statusDescription) {
            Toast.makeText(ActivitySingleThread.this, statusDescription, Toast.LENGTH_LONG).show();
            new AsyncGetAllMessagesToThread(threadId).execute();
        }
    }

}
