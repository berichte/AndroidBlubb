package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import com.blubb.alubb.beapcom.MessagePullService;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ThreadOverview extends Activity {

    private static final int RESULT_SETTINGS = 1;
    final Context context = this;
    private PendingIntent mAlarmSender;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_thread_overview);
        checkForLogin();

        AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
        asyncGetAllThreads.execute();
        addNewThreadButtonListener();
        startMessagePullService();
    }

    private void checkForLogin() {
        try {
            getApp().getSessionManager().getSessionID(this);
        } catch (InvalidParameterException e) {
            Intent intent = new Intent(ThreadOverview.this, Blubb_login.class);
            ThreadOverview.this.startActivity(intent);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (SessionException e) {
            Intent intent = new Intent(ThreadOverview.this, Blubb_login.class);
            ThreadOverview.this.startActivity(intent);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (BlubbDBException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    private void startMessagePullService() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmSender = PendingIntent.getService(ThreadOverview.this,
                0, new Intent(ThreadOverview.this, MessagePullService.class), 0);
        am.cancel(mAlarmSender);
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        String intName = this.getString(R.string.pref_message_pull_interval);
        String intervalStr = sharedPrefs.getString(intName, "0");

        int interval = Integer.parseInt(intervalStr);
        if(interval==0) return;

        long firstTime = SystemClock.elapsedRealtime();

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, interval*1000, mAlarmSender);
    }

    @Override
    public void onResume() {
        super.onResume();
        //login();
        startMessagePullService();
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
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();

            }
        });
    }

    private class AsyncNewThread extends AsyncTask<Void, Void, BlubbThread>{

        String title, descr;
        public AsyncNewThread(String title, String descr) {
            this.title = title;
            this.descr = descr;
        }
        @Override
        protected BlubbThread doInBackground(Void... voids) {
            try {
                return getApp().getThreadManager().createThread(
                        ThreadOverview.this, title, descr);
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
            } catch (SessionException e) {
                Intent intent = new Intent(ThreadOverview.this, Blubb_login.class);
                ThreadOverview.this.startActivity(intent);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(BlubbThread thread) {
            if(thread != null) {
                String msg = "Created new Thread:\n" +
                        "tId: " + thread.gettId() + "\n" +
                        "tTitle: " + thread.getThreadTitle();
                Toast.makeText(ThreadOverview.this, msg, Toast.LENGTH_LONG).show();
            } // if null there has been a toast.
        }
    }

    private class AsyncGetAllThreads extends AsyncTask<Void, Void, List<BlubbThread>> {

        private Exception exception;

        @Override
        protected List<BlubbThread> doInBackground(Void... voids) {
            try {
                return getApp().getThreadManager().getAllThreadsFromBeap(ThreadOverview.this);
            } catch (InvalidParameterException e) {
                exception = e;
            } catch (SessionException e) {
                exception = e;
            } catch (BlubbDBException e) {
                exception = e;
            } catch (JSONException e) {
                exception = e;
            }
            return getApp().getThreadManager().getAllThreadsFromSqlite(ThreadOverview.this);
        }

        @Override
        protected void onPostExecute(final List<BlubbThread> response) {
            if(exception != null) Toast.makeText(ThreadOverview.this,
                    "Blubb " + exception.getMessage(),
                    Toast.LENGTH_LONG).show();

            ListView lv = (ListView) findViewById(R.id.thread_list);
            final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                    ThreadOverview.this, R.layout.thread_list_entry, response);
            lv.setAdapter(adapter);
            final List<BlubbThread> list = new ArrayList<BlubbThread>(response);

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
                                  List<BlubbThread> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
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
