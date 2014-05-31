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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class SingleThreadActivity extends Activity {
    public static final String EXTRA_THREAD_ID = "threadId";
    public static final String EXTRA_THREAD_TITLE = "threadTitle";

    private String threadId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_thread);

        Intent intent = getIntent();
        this.threadId = intent.getStringExtra(EXTRA_THREAD_ID);
        Log.i("SingleThreadActivity", "Requesting Messages for Thread " + threadId);
        AsyncGetAllMessagesToThread asyncTask = new AsyncGetAllMessagesToThread(threadId);

        String tTitle = intent.getStringExtra(EXTRA_THREAD_TITLE);
        setTitle(tTitle);

        this.addNewMessageButtonListener();
        asyncTask.execute();
    }

    private void addNewMessageButtonListener() {
        ImageButton nMessageButton = (ImageButton)
                findViewById(R.id.single_thread_new_message_button);
        nMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleThreadActivity.this, WriteMessageActivity.class);
                intent.putExtra(EXTRA_THREAD_ID, SingleThreadActivity.this.threadId);
                SingleThreadActivity.this.startActivity(intent);
            }
        });
    }

    private class AsyncGetAllMessagesToThread extends AsyncTask<Void, Void, BlubbMessage[]> {

        private Exception exception;
        private String threadId;

        public AsyncGetAllMessagesToThread(String threadId) {
            this.threadId = threadId;
        }

        @Override
        protected BlubbMessage[] doInBackground(Void... voids) {
            try {
                return BlubbComManager.getMessages(SingleThreadActivity.this, this.threadId);
            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllMessages", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(final BlubbMessage[] response) {
            ListView lv = (ListView) findViewById(R.id.single_thread_listview);
            final MessageArrayAdapter adapter = new MessageArrayAdapter(
                    SingleThreadActivity.this, R.layout.message_layout, response);
            lv.setAdapter(adapter);

            final List<BlubbMessage> list = new ArrayList<BlubbMessage>(Arrays.asList(response));

            lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    /*Intent intent = new Intent(SingleThreadActivity.this, SingleThreadActivity.class);
                    assert ((BlubbThread) parent.getItemAtPosition(position)) != null;
                    String threadId = ((BlubbThread) parent.getItemAtPosition(position)).gettId();
                    intent.putExtra("Thread", threadId);
                    SingleThreadActivity.this.startActivity(intent);
*/
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
           /* String allThreadsString = "";
            if(response != null) {
                for(BlubbThread bt: response) {
                    allThreadsString = allThreadsString + bt.toString() + "\n";
                }
            } else allThreadsString = this.exception.getMessage();

            TextView tv = (TextView) findViewById(R.id.thread_textv);
            tv.setText(allThreadsString);*/
        }
    }

    private class MessageArrayAdapter extends ArrayAdapter<BlubbMessage> {

        HashMap<BlubbMessage, Integer> mIdMap = new HashMap<BlubbMessage, Integer>();

        public MessageArrayAdapter(Context context, int textViewResourceId,
                                   BlubbMessage[] objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.length; ++i) {
                mIdMap.put(objects[i], i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) super.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.message_layout, parent, false);

            TextView mTitle = (TextView) rowView.findViewById(R.id.message_title_tv),
                    mContent= (TextView) rowView.findViewById(R.id.message_content_tv),
                    mCreator = (TextView) rowView.findViewById(R.id.message_creator_tv),
                    mDate = (TextView) rowView.findViewById(R.id.message_date_tv),
                    mRole = (TextView) rowView.findViewById(R.id.message_role_tv);
            BlubbMessage blubbMessage = getItem(position);

            mTitle.setText(blubbMessage.getmTitle());
            mContent.setText(blubbMessage.getmContent());
            mCreator.setText(blubbMessage.getmCreator());
            mDate.setText(blubbMessage.getmDate());
            mRole.setText(blubbMessage.getmCreatorRole());

            return rowView;
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
