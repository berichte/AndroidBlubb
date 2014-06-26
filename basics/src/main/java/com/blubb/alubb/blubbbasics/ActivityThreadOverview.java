package com.blubb.alubb.blubbbasics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.DatabaseHandler;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.beapcom.MessagePullService;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;


public class ActivityThreadOverview extends Activity {
    private static final String NAME = "ThreadOverview";

    private static final int RESULT_SETTINGS = 1,
                            RESULT_LOGIN = 2;
    private static final int NOTIFICATION_ID = 1904;
    private static String titleInput = "", descInput = "";
    final Context context = this;
    private boolean loginSpinn = false,
            getAllBeapSpinn = false,
            getAllLocalSpinn = false,
            createThreadSpinn = false,
            storeDraft = true;
    private MenuItem loggedInItem;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        start();
    }

    private void start(){

        setContentView(R.layout.activity_thread_overview);
        checkForLogin();

        AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
        this.getAllBeapSpinn = true;
        spinnerOn();
        asyncGetAllThreads.execute();
        startMessagePullService();
    }

    private void addHeader() {
        ListView lv = (ListView) findViewById(R.id.thread_list);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout header = (LinearLayout)
                inflater.inflate(R.layout.thread_ov_header, lv, false);

        lv.addHeaderView(header);
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
        PendingIntent mAlarmSender = PendingIntent.getService(ActivityThreadOverview.this,
                0, new Intent(ActivityThreadOverview.this, MessagePullService.class), 0);
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
        //addHeader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_overview_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_thread_action:
                newThreadDialog();
                break;
            case R.id.menu_action_refresh:
                AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
                this.getAllBeapSpinn = true;
                spinnerOn();
                asyncGetAllThreads.execute();
                break;
            case R.id.menu_blubb_settings:
                Intent i = new Intent(this, ActivitySettings.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
            case R.id.menu_action_login:
                Intent loginIntent = new Intent(this, ActivityLogin.class);
                loginIntent.putExtra(ActivityLogin.EXTRA_LOGIN_TYPE,
                        ActivityLogin.LoginType.LOGIN);
                startActivityForResult(loginIntent, RESULT_LOGIN);
                break;
            case R.id.menu_action_resetpassword:
                Intent resetPwIntent = new Intent(this, ActivityLogin.class);
                resetPwIntent.putExtra(ActivityLogin.EXTRA_LOGIN_TYPE,
                        ActivityLogin.LoginType.RESET);
                startActivityForResult(resetPwIntent, RESULT_LOGIN);
                break;
            case R.id.menu_action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.logout_warning_message)
                        .setPositiveButton(R.string.logout_positive_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AlarmManager am = (AlarmManager)
                                        getSystemService(Context.ALARM_SERVICE);
                                PendingIntent mAlarmSender = PendingIntent.getService(
                                        ActivityThreadOverview.this, 0,
                                        new Intent(ActivityThreadOverview.this,
                                                MessagePullService.class), 0
                                );
                                am.cancel(mAlarmSender);
                                SessionManager.getInstance().logout(ActivityThreadOverview.this);
                                finish();

                            }
                        })
                        .setNegativeButton(R.string.logout_negativ_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newThreadDialog() {
        final Dialog dialog = new Dialog(ActivityThreadOverview.this);

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

        if (storeDraft) {
            title.setText(titleInput);
            descr.setText(descInput);
        }
        storeDraft = true;
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(NAME, "onCancel(Dialog)");
                titleInput = title.getText().toString();
                descInput = descr.getText().toString();
            }
        });

        yBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String titleString = title.getText().toString(),
                        descString = descr.getText().toString();
                if (titleString.length() > 40 || titleString.length() == 0) {
                    String message = ActivityThreadOverview.this.getString(
                            R.string.thread_title_size_warning);
                    Toast.makeText(ActivityThreadOverview.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }
                char[] de = descString.toCharArray();
                int nlCounter = 0;
                for (char c : de) {
                    if (c == '\n') nlCounter++;
                }
                if (nlCounter > 2) {
                    String message = ActivityThreadOverview.this.getString(
                            R.string.thread_descr_nl_warning);
                    Toast.makeText(ActivityThreadOverview.this,
                            message, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (descString.length() < 1) {
                    String message = ActivityThreadOverview.this.getString(
                            R.string.thread_descr_size_warning);
                    Toast.makeText(ActivityThreadOverview.this,
                            message, Toast.LENGTH_SHORT).show();
                    return;
                }
                AsyncNewThread asyncNewThread = new AsyncNewThread(
                        title.getText().toString(),
                        descr.getText().toString()
                );
                createThreadSpinn = true;
                spinnerOn();
                asyncNewThread.execute();
                titleInput = "";
                descInput = "";
                dialog.cancel();
                storeDraft = false;
            }
        });

        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                storeDraft = false;
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        if (shouldSpinn()) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    public void spinnerOff()
    {
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        if (!shouldSpinn()) {
            pb.setVisibility(View.INVISIBLE);
            //setProgressBarVisibility(false);
        }
    }

    private void showOnlineStatus() {
        /*View layout = findViewById(R.id.thread_ov_rl);
        layout.setBackground(getResources().getDrawable(R.drawable.waterdrop_wallpaper));*/
    }

    private void showOfflineStatus(){/*
        View layout = findViewById(R.id.thread_ov_rl);
        layout.setBackgroundColor(getResources().getColor(R.color.beap_medium_yellow));*/
    }

    private void cancelNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    private void createNotification(String nTitle) {
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.blubb_logo);
        Intent resultIntent;
        builder.setContentTitle(nTitle);

        builder.setAutoCancel(true);

        // Including the notification ID allows you to update the notification later on.
        Notification notification = builder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        nManager.notify(NOTIFICATION_ID, notification);
        Log.w(NAME, "pushing notification");
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
                        ActivityThreadOverview.this.getApplicationContext(), title, descr);
            } catch (BlubbDBException e) {
                createNotification(e.getMessage());
                this.exception = e;
            } catch (InvalidParameterException e) {
                this.exception = e;
            } catch (SessionException e) {
                this.exception = e;
            } catch (JSONException e) {
                this.exception = e;
            } catch (BlubbDBConnectionException e) {
                createNotification(e.getMessage());
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
                Toast.makeText(ActivityThreadOverview.this, msg, Toast.LENGTH_LONG).show();
                cancelNotification();
            } // if null there has been a toast.
            handleException(exception);
            AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
            asyncGetAllThreads.execute();
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
            return blubbThread.getView(ActivityThreadOverview.this, parent);
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

    /**
     * private class AsyncGetLocalThreads extends AsyncTask<Void, Void, List<BlubbThread>> {
     *
     * @Override protected List<BlubbThread> doInBackground(Void... voids) {
     * Log.v("AsyncGetAllLocalThreads", "execute()");
     * return getApp().getThreadManager().getAllThreadsFromSqlite(
     * ThreadOverview.this.getApplicationContext());
     * }
     * @Override protected void onPostExecute(final List<BlubbThread> response) {
     * Log.v("AsyncGetAllLocalThreads", "received " + response.size() + " threads.");
     * ListView lv = (ListView) findViewById(R.id.thread_list);
     * final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
     * ThreadOverview.this, R.layout.thread_list_entry, response);
     * lv.setAdapter(adapter);
     * <p/>
     * lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
     * @Override public void onItemClick(AdapterView<?> parent, final View view,
     * int position, long id) {
     * Intent intent = new Intent(ThreadOverview.this, SingleThreadActivity.class);
     * assert parent.getItemAtPosition(position) != null;
     * BlubbThread bThread = (BlubbThread) parent.getItemAtPosition(position);
     * String threadId = bThread.gettId();
     * intent.putExtra(SingleThreadActivity.EXTRA_THREAD_ID, threadId);
     * String tTitle = bThread.getThreadTitle();
     * intent.putExtra(SingleThreadActivity.EXTRA_THREAD_TITLE, tTitle);
     * String tCreator = bThread.gettCreator();
     * intent.putExtra(SingleThreadActivity.EXTRA_THREAD_CREATOR, tCreator);
     * String tDesc = bThread.gettDesc();
     * intent.putExtra(SingleThreadActivity.EXTRA_THREAD_DESCRIPTION, tDesc);
     * ThreadOverview.this.startActivity(intent);
     * }
     * <p/>
     * });
     * lv.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
     * @Override public boolean onItemLongClick(AdapterView<?> parent, View view,
     * int position, long id) {
     * BlubbThread thread = (BlubbThread) parent.getItemAtPosition(position);
     * thread.toggleViewSize();
     * @SuppressWarnings("unchecked") ArrayAdapter<BlubbThread> adapter = (ArrayAdapter<BlubbThread>)
     * parent.getAdapter();
     * adapter.notifyDataSetChanged();
     * return true;
     * }
     * });
     * getAllLocalSpinn = false;
     * spinnerOff();
     * }
     * }
     */

    private class AsyncGetAllThreads extends AsyncTask<Void, Void, List<BlubbThread>> {

        @Override
        protected List<BlubbThread> doInBackground(Void... voids) {
            Log.v("AsyncGetAllThreads", "execute()");
            return getApp().getThreadManager().getAllThreads(
                    ActivityThreadOverview.this.getApplicationContext());
        }

        @Override
        protected void onPostExecute(final List<BlubbThread> response) {
            ListView lv = (ListView) findViewById(R.id.thread_list);

            final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                    ActivityThreadOverview.this, R.layout.thread_list_entry, response);

            if (adapter.getCount() > response.size()) {
                spinnerOff();
                return;
            }

            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Intent intent = new Intent(ActivityThreadOverview.this, ActivitySingleThread.class);
                    assert parent.getItemAtPosition(position) != null;
                    BlubbThread bThread = (BlubbThread) parent.getItemAtPosition(position);
                    String threadId = bThread.gettId();
                    intent.putExtra(ActivitySingleThread.EXTRA_THREAD_ID, threadId);
                    String tTitle = bThread.getThreadTitle();
                    intent.putExtra(ActivitySingleThread.EXTRA_THREAD_TITLE, tTitle);
                    String tCreator = bThread.gettCreator();
                    intent.putExtra(ActivitySingleThread.EXTRA_THREAD_CREATOR, tCreator);
                    String tDesc = bThread.gettDesc();
                    intent.putExtra(ActivitySingleThread.EXTRA_THREAD_DESCRIPTION, tDesc);
                    ActivityThreadOverview.this.startActivity(intent);
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
                        ActivityThreadOverview.this.getApplicationContext());
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
                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show();
                // if the exception is a ConnectionException don't let user perform manual login
                if (exception.getClass().equals(SessionException.class)) {
                    DatabaseHandler db = new DatabaseHandler(ActivityThreadOverview.this);
                    int counter = db.getThreadCount();
                    if(counter == 0) {
                        Intent intent = new Intent(ActivityThreadOverview.this, ActivityLogin.class);
                        intent.putExtra(ActivityLogin.EXTRA_LOGIN_TYPE, ActivityLogin.LoginType.LOGIN);
                        ActivityThreadOverview.this.startActivity(intent);
                    }
                }
                showOfflineStatus();
                //createNotification("Oh no we are offline. :-C ");
            } else {
                showOnlineStatus();
            } //cancelNotification();
            loginSpinn = false;
            spinnerOff();
        }
    }
}
