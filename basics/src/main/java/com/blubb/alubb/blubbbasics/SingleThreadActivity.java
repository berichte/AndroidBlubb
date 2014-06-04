package com.blubb.alubb.blubbbasics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.DatabaseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class SingleThreadActivity extends Activity {
    public static final String NAME = "SingleThreadActivity";
    public static final String EXTRA_THREAD_ID = "threadId";
    public static final String EXTRA_THREAD_TITLE = "threadTitle",
            EXTRA_THREAD_CREATOR = "threadCreator";

    public List<BlubbMessage> messages;

    private String threadId, tCreator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(NAME, "onCreate()");
        setContentView(R.layout.activity_single_thread);

        Intent intent = getIntent();
        this.threadId = intent.getStringExtra(EXTRA_THREAD_ID);
        Log.i("SingleThreadActivity", "Requesting Messages for Thread " + threadId);
        this.tCreator = intent.getStringExtra(EXTRA_THREAD_CREATOR);

        AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);

        String tTitle = intent.getStringExtra(EXTRA_THREAD_TITLE);
        setTitle(tTitle + " - " + tCreator);

        this.addNewMessageButtonListener();
        asyncTask.execute();
    }

    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    private void addNewMessageButtonListener() {
        ImageButton nMessageButton = (ImageButton)
                findViewById(R.id.single_thread_new_message_button);
        nMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.i(NAME, "addNewMessageButton.onClick()");
                Intent intent = new Intent(SingleThreadActivity.this, WriteMessageActivity.class);
                intent.putExtra(EXTRA_THREAD_ID, SingleThreadActivity.this.threadId);
                SingleThreadActivity.this.startActivity(intent);
            }
        });
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

    private class AsyncGetAllMessagesToThread extends AsyncTask<Void, Void, List<BlubbMessage>> {

        private Exception exception;
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

            lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    final String item = (String) parent.getItemAtPosition(position).toString();
                    view.animate().setDuration(2000).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    list.remove(item);
                                    adapter.notifyDataSetChanged();
                                    view.setAlpha(1);
                                }
                            });
                }

            });
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


}
