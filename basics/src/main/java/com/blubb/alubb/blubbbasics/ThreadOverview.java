package com.blubb.alubb.blubbbasics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import android.widget.ListView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.beapcom.MessagePullService;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;


public class ThreadOverview extends Activity {
    private static final String NAME = "ThreadOverview";

    private static final int RESULT_SETTINGS = 1,
                            RESULT_LOGIN = 2;
    final Context context = this;
    private boolean loginSpinn = false,
            getAllBeapSpinn = false,
            getAllLocalSpinn = false,
            createThreadSpinn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        start();
    }

    private void start(){

        setContentView(R.layout.activity_thread_overview);
        checkForLogin();

        this.getAllLocalSpinn = true;
        new AsyncGetLocalThreads().execute();
        spinnerOn();
        AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
        this.getAllBeapSpinn = true;
        spinnerOn();
        asyncGetAllThreads.execute();
        startMessagePullService();
    }

    private void checkForLogin() {
        Log.v(NAME, "checkForLogin()");
        this.loginSpinn = true;
        spinnerOn();
        new AsyncCheckLogin().execute();
    }

    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    private void startMessagePullService() {
        Log.v(NAME, "startMessagePullService()");
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent mAlarmSender = PendingIntent.getService(ThreadOverview.this,
                0, new Intent(ThreadOverview.this, MessagePullService.class), 0);
        am.cancel(mAlarmSender);
        SharedPreferences sharedPrefs;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
        Log.v(NAME, "onResume()");
        start();
        startMessagePullService();
        if (shouldSpinn())
        {
            spinnerOn();
        }
        else
        {
            spinnerOff();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_overview_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_thread_action:
                newThreadDialog();
                break;
            case R.id.blubb_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
            case R.id.blubb_settings_login:
                Intent loginIntent = new Intent(this, BlubbLoginActivity.class);
                startActivityForResult(loginIntent, RESULT_LOGIN);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newThreadDialog() {
        final Dialog dialog = new Dialog(ThreadOverview.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.create_thread_dialog, null);
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
                R.id.thread_new_title_dialog),
                descr = (EditText) dialogLayout.findViewById(
                        R.id.thread_new_descr_dialog);
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
                AsyncNewThread asyncNewThread = new AsyncNewThread(
                        title.getText().toString(),
                        descr.getText().toString()
                );
                createThreadSpinn = true;
                spinnerOn();
                asyncNewThread.execute();
                dialog.cancel();
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

    private void handleException(Exception e) {
        if (e != null) {
            Log.e(NAME, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean shouldSpinn() {
        return (getAllBeapSpinn || getAllLocalSpinn || loginSpinn || createThreadSpinn);
    }

    public void spinnerOn() {
        if (shouldSpinn()) {
            setProgressBarIndeterminateVisibility(true);
        }
    }

    public void spinnerOff()
    {
        if (!shouldSpinn()) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private class AsyncNewThread extends AsyncTask<Void, Void, BlubbThread> {

        String title, descr;
        Exception exception;

        public AsyncNewThread(String title, String descr) {
            this.title = title;
            this.descr = descr;
        }

        @Override
        protected BlubbThread doInBackground(Void... voids) {
            Log.v("AsyncNewThread", "execute()");

            try {
                return getApp().getThreadManager().createThread(
                        ThreadOverview.this.getApplicationContext(), title, descr);
            } catch (BlubbDBException e) {
                this.exception = e;
            } catch (InvalidParameterException e) {
                this.exception = e;
            } catch (SessionException e) {
                this.exception = e;
            } catch (JSONException e) {
                this.exception = e;
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(BlubbThread thread) {
            if (thread != null) {
                String msg = "Created new Thread:\n" +
                        "tId: " + thread.gettId() + "\n" +
                        "tTitle: " + thread.getThreadTitle();
                Log.i("AsyncNewThread", msg);
                Toast.makeText(ThreadOverview.this, msg, Toast.LENGTH_LONG).show();
            } // if null there has been a toast.
            handleException(exception);
            createThreadSpinn = false;
            spinnerOff();
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
            BlubbThread blubbThread = getItem(position);
            return blubbThread.getView(ThreadOverview.this, parent);
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

    private class AsyncGetLocalThreads extends AsyncTask<Void, Void, List<BlubbThread>> {

        @Override
        protected List<BlubbThread> doInBackground(Void... voids) {
            Log.v("AsyncGetAllLocalThreads", "execute()");
            return getApp().getThreadManager().getAllThreadsFromSqlite(
                        ThreadOverview.this.getApplicationContext());
        }

        @Override
        protected void onPostExecute(final List<BlubbThread> response) {
            Log.v("AsyncGetAllLocalThreads", "received " + response.size() + " threads.");
            ListView lv = (ListView) findViewById(R.id.thread_list);
            final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                    ThreadOverview.this, R.layout.thread_list_entry, response);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Intent intent = new Intent(ThreadOverview.this, SingleThreadActivity.class);
                    assert parent.getItemAtPosition(position) != null;
                    BlubbThread bThread = (BlubbThread) parent.getItemAtPosition(position);
                    String threadId = bThread.gettId();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_ID, threadId);
                    String tTitle = bThread.getThreadTitle();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_TITLE, tTitle);
                    String tCreator = bThread.gettCreator();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_CREATOR, tCreator);
                    String tDesc = bThread.gettDesc();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_DESCRIPTION, tDesc);
                    ThreadOverview.this.startActivity(intent);
                }

            });
            lv.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    BlubbThread thread = (BlubbThread) parent.getItemAtPosition(position);
                    thread.toggleViewSize();
                    @SuppressWarnings("unchecked")
                    ArrayAdapter<BlubbThread> adapter = (ArrayAdapter<BlubbThread>)
                            parent.getAdapter();
                    adapter.notifyDataSetChanged();
                    return true;
                }
            });
            getAllLocalSpinn = false;
            spinnerOff();
        }
    }

    private class AsyncGetAllThreads extends AsyncTask<Void, Void, List<BlubbThread>> {

        private Exception exception;

        @Override
        protected List<BlubbThread> doInBackground(Void... voids) {
            Log.v("AsyncGetAllThreads", "execute()");
            try {
                return getApp().getThreadManager().getAllThreadsFromBeap(
                        ThreadOverview.this.getApplicationContext());
            } catch (InvalidParameterException e) {
                exception = e;
            } catch (SessionException e) {
                exception = e;
            } catch (BlubbDBException e) {
                exception = e;
            } catch (JSONException e) {
                exception = e;
            } catch (BlubbDBConnectionException e) {
                exception = e;
            }
            return getApp().getThreadManager().getAllThreadsFromSqlite(
                    ThreadOverview.this.getApplicationContext());
        }

        @Override
        protected void onPostExecute(final List<BlubbThread> response) {
            handleException(exception);

            ListView lv = (ListView) findViewById(R.id.thread_list);

            final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                    ThreadOverview.this, R.layout.thread_list_entry, response);

            if(adapter.getCount() > response.size()) {
                spinnerOff();
                return;
            }

            lv.setAdapter(adapter);

            lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Intent intent = new Intent(ThreadOverview.this, SingleThreadActivity.class);
                    assert parent.getItemAtPosition(position) != null;
                    BlubbThread bThread = (BlubbThread) parent.getItemAtPosition(position);
                    String threadId = bThread.gettId();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_ID, threadId);
                    String tTitle = bThread.getThreadTitle();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_TITLE, tTitle);
                    String tCreator = bThread.gettCreator();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_CREATOR, tCreator);
                    String tDesc = bThread.gettDesc();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_DESCRIPTION, tDesc);
                    ThreadOverview.this.startActivity(intent);
                }

            });
            getAllBeapSpinn = false;
            spinnerOff();
        }
    }

    private class AsyncCheckLogin extends AsyncTask<Void, Void, Boolean> {

        private Exception exception;
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v(NAME, "AsyncLogin");
            try {
                getApp().getSessionManager().getSessionID(
                        ThreadOverview.this.getApplicationContext());
                return true;
            } catch (InvalidParameterException e) {
                this.exception = e;
            } catch (SessionException e) {
                this.exception = e;
            } catch (BlubbDBException e) {
                this.exception = e;
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
            } catch (PasswordInitException e) {
                Log.e(NAME, e.getMessage() + " can not happen at this point!");
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isLoggedIn) {
            // if there was no exception toast will be null - everything is alright.
            if (!isLoggedIn) {
                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                // if the exception is a ConnectionException don't let user perform manual login
                if(!exception.getClass().equals(BlubbDBConnectionException.class)) {
                    Intent intent = new Intent(ThreadOverview.this, BlubbLoginActivity.class);
                    ThreadOverview.this.startActivity(intent);
                }


            }
            loginSpinn = false;
            spinnerOff();
        }
    }




}
