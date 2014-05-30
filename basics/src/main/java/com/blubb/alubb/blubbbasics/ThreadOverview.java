package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.beapcom.BlubbDBReplyStatus;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ThreadOverview extends Activity {

    private static final int RESULT_SETTINGS = 1;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.activity_thread_overview);

        AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
        asyncGetAllThreads.execute();
        addNewThreadButtonListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.blubb_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
        }
        return true;
    }



    private void addNewThreadButtonListener() {
        Button nMessageButton = (Button) findViewById(R.id.thread_overview_new_thread_button);
        nMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.new_thread_layout);
                dialog.setTitle("Create new thread");

                final EditText title = (EditText) dialog.findViewById(R.id.thread_new_title),
                        descr = (EditText) dialog.findViewById(R.id.thread_new_descr);
                Button sendBtn = (Button) dialog.findViewById(R.id.thread_new_send_button),
                        cancelBtn = (Button) dialog.findViewById(R.id.thread_new_cancel_button);

                sendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AsyncNewThread asyncNewThread = new AsyncNewThread(
                                title.getText().toString(),
                                descr.getText().toString()
                        );
                        asyncNewThread.execute();
                    }
                });


                /*
                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.text);
                text.setText("Android custom dialog example!");
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                image.setImageResource(R.drawable.ic_launcher);

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
*/
                dialog.show();

            }
        });
    }

    private class AsyncNewThread extends AsyncTask<Void, Void, BlubbDBReplyStatus>{

        String title, descr;
        public AsyncNewThread(String title, String descr) {
            this.title = title;
            this.descr = descr;
        }
        @Override
        protected BlubbDBReplyStatus doInBackground(Void... voids) {
            BlubbComManager manager = new BlubbComManager();
            try {
                return manager.openNewBlubbThread (title, descr);
            } catch (BlubbDBException e) {
                Context context = getApplicationContext();
                CharSequence text = "something went terribly wrong: " + e.getMessage();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } catch (InvalidParameterException e) {
                Context context = getApplicationContext();
                CharSequence text = "something went terribly wrong: " + e.getMessage();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            return BlubbDBReplyStatus.REQUEST_FAILURE;
        }

        @Override
        protected void onPostExecute(BlubbDBReplyStatus status) {
            Context context = getApplicationContext();
            CharSequence text = "Sent new Thread to DB - Reply: " + status.toString();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private class AsyncGetAllThreads extends AsyncTask<Void, Void, BlubbThread[]> {

        private Exception exception;

        @Override
        protected BlubbThread[] doInBackground(Void... voids) {
            BlubbComManager manager = new BlubbComManager();
            try {
                return manager.getAllThreads();
            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllThreads", e.getMessage());
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
                Log.e("getAllThreads", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(final BlubbThread[] response) {
            ListView lv = (ListView) findViewById(R.id.thread_list);
            final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                    ThreadOverview.this, R.layout.thread_list_entry, response);
            lv.setAdapter(adapter);
            final List<BlubbThread> list = new ArrayList<BlubbThread>(Arrays.asList(response));

            lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Intent intent = new Intent(ThreadOverview.this, SingleThreadActivity.class);
                    assert ((BlubbThread) parent.getItemAtPosition(position)) != null;
                    BlubbThread bThread = (BlubbThread) parent.getItemAtPosition(position);
                    String threadId = bThread.gettId();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_ID, threadId);
                    String tTitle = bThread.getThreadTitle();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_TITLE, tTitle);
                    ThreadOverview.this.startActivity(intent);

                }

            });

        }
    }

    private class ThreadArrayAdapter extends ArrayAdapter<BlubbThread> {

        HashMap<BlubbThread, Integer> mIdMap = new HashMap<BlubbThread, Integer>();

        public ThreadArrayAdapter(Context context, int textViewResourceId,
                                  BlubbThread[] objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.length; ++i) {
                mIdMap.put(objects[i], i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) super.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.thread_list_entry, parent, false);
            TextView title = (TextView) rowView.findViewById(R.id.thread_list_item_head),
                description = (TextView) rowView.findViewById(R.id.thread_list_item_body);
            BlubbThread blubbThread = getItem(position);
            title.setText(blubbThread.getThreadTitle());
            description.setText(blubbThread.gettDesc());

            return rowView;
        }

        @Override
        public long getItemId(int position) {
            BlubbThread item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


}
