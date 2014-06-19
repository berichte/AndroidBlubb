package com.blubb.alubb.blubbbasics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.DatabaseHandler;
import com.blubb.alubb.basics.ThreadManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class SingleThreadActivity extends Activity {
    public static final String NAME = "SingleThreadActivity";
    public static final String EXTRA_THREAD_ID = "threadId";
    public static final String EXTRA_THREAD_TITLE = "threadTitle",
            EXTRA_THREAD_CREATOR = "threadCreator",
            EXTRA_THREAD_DESCRIPTION = "threadDescription";

    public List<BlubbMessage> messages;

    private String threadId, tCreator;
    private boolean showSpinner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Log.v(NAME, "onCreate()");
       start();
    }

    private void start() {
        setContentView(R.layout.activity_single_thread);
        Intent intent = getIntent();
        this.threadId = intent.getStringExtra(EXTRA_THREAD_ID);
        ThreadManager.getInstance().readingThread(this, threadId);
        Log.i("SingleThreadActivity", "Requesting Messages for Thread " + threadId);
        this.tCreator = intent.getStringExtra(EXTRA_THREAD_CREATOR);

        String tDescr = intent.getStringExtra(EXTRA_THREAD_DESCRIPTION);
        addHeader(tDescr);

        spinnerOn();
        AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);

        String tTitle = intent.getStringExtra(EXTRA_THREAD_TITLE);
        setTitle(tTitle + " - " + tCreator);

        asyncTask.execute();
    }

    private void addHeader(String tDescr) {
        ListView lv = (ListView) findViewById(R.id.single_thread_listview);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout header = (LinearLayout)
                inflater.inflate(R.layout.single_thread_header, lv , false);

        TextView headerText = (TextView) header.findViewById(R.id.single_thread_header_tv);
        headerText.setText(tDescr);

        lv.addHeaderView(header );
    }

    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newMessageDialog() {
        final Dialog dialog = new Dialog(SingleThreadActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.create_message_dialog, null);
        dialog.setContentView(dialogLayout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View divider = dialog.findViewById(
                dialog.getContext().getResources()
                        .getIdentifier("android:id/titleDivider", null, null)
        );
        divider.setBackground(new ColorDrawable(Color.TRANSPARENT));
        // builder.setView(dialogLayout);

        //builder.setInverseBackgroundForced(true);
        assert dialogLayout != null;
        final EditText title = (EditText) dialogLayout.findViewById(
                R.id.message_new_title_dialog),
                descr = (EditText) dialogLayout.findViewById(
                        R.id.message_new_content_dialog);
        Button yBtn;
        yBtn = (Button) dialogLayout.findViewById(
                R.id.y_button_dialog);
        Button xBtn = (Button) dialogLayout.findViewById(
                R.id.x_button_dialog);

        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        BlubbApplication.setLayoutFont(tf, xBtn);

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncSendMessage asyncNewMessage = new AsyncSendMessage(
                        threadId,
                        title.getText().toString(),
                        descr.getText().toString()
                );
                spinnerOn();
                asyncNewMessage.execute();
                dialog.cancel();
                new AsyncGetAllMessagesToThread(threadId).execute();
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
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
                newMessageDialog();
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        for(BlubbMessage m: messages) {
            if(m.isNew()) {
                m.setNew(false);
                DatabaseHandler databaseHandler = new DatabaseHandler(this);
                databaseHandler.setMessageRead(m.getmId());
            }
        }
    }

    public void spinnerOn() {
        showSpinner = true;
        setProgressBarIndeterminateVisibility(true);
    }

    public void spinnerOff() {
        showSpinner = false;
        setProgressBarIndeterminateVisibility(false);
    }

    protected void onResume()
    {

        // solved by Dralangus http://stackoverflow.com/a/7414659/294884
        super.onResume();
        start();
        if (showSpinner) {
            spinnerOn();
        } else {
            spinnerOff();
        }
    }

    private void handleException(Exception e) {
        if (e != null) {
            Log.e(NAME, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class AsyncGetAllMessagesToThread extends AsyncTask<Void, Void, List<BlubbMessage>> {

        private String threadId;

        public AsyncGetAllMessagesToThread(String threadId) {
            this.threadId = threadId;
        }

        @Override
        protected List<BlubbMessage> doInBackground(Void... voids) {
            Log.v(NAME, "AsyncGetAllMessagesToThread.execute(thread = " + threadId + ")");
            return getApp().getMessageManager().getAllMessagesForThread(
                    SingleThreadActivity.this.getApplicationContext(), this.threadId);
        }

        @Override
        protected void onPostExecute(final List<BlubbMessage> response) {
            messages = response;
            ListView lv = (ListView) findViewById(R.id.single_thread_listview);
            Collections.reverse(response);
            final MessageArrayAdapter adapter = new MessageArrayAdapter(
                    SingleThreadActivity.this, R.layout.message_layout, response);
            lv.setAdapter(adapter);

            final List<BlubbMessage> list = new ArrayList<BlubbMessage>(response);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
/*
                    final String item = parent.getItemAtPosition(position).toString();
                    view.animate().setDuration(2000).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    //TODO place here code to reply to a message or something.
                                }
                            });
                */
                }

            });
            spinnerOff();
        }
    }

    private class MessageArrayAdapter extends ArrayAdapter<BlubbMessage> {

        HashMap<BlubbMessage, Integer> mIdMap = new HashMap<BlubbMessage, Integer>();

        public MessageArrayAdapter(Context context, int textViewResourceId,
                                   List<BlubbMessage> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BlubbMessage message = getItem(position);
            return message.getView(SingleThreadActivity.this, parent, tCreator);
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

    private class AsyncSendMessage extends AsyncTask<Void, String, BlubbMessage> {

        private Exception exception;
        private String tId, mTitle, mContent;

        public AsyncSendMessage(String tId, String mTitle, String mContent) {
            this.tId = tId;
            this.mTitle = mTitle;
            this.mContent = mContent;
        }

        @Override
        protected BlubbMessage doInBackground(Void... blubbs) {
            try {
                return getApp().getMessageManager().createMsg(
                        SingleThreadActivity.this.getApplicationContext(), tId, mTitle, mContent);

            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllMessages", e.getMessage());
            } catch (InvalidParameterException e) {
                this.exception = e;
            } catch (SessionException e) {
                this.exception = e;
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
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
                Toast.makeText(SingleThreadActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
