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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.DatabaseHandler;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.basics.ThreadManager;
import com.blubb.alubb.blubexceptions.SessionException;

import java.util.HashMap;
import java.util.List;

/**
 * Central activity which shows the threads available for the logged in user.
 * From this point of the user interface every other screen is accessible.
 */
public class ActivityThreadOverview extends Activity {
    /**
     * Name for Logging purposes.
     */
    private static final String NAME = "ThreadOverview";

    /**
     * If >= 0, this code will be returned in onActivityResult() when the activity exits.
     * This is the value for the settings and the login.
     */
    private static final int RESULT_SETTINGS = 1,
            RESULT_LOGIN = 2;

    /**
     * Unique id for the notification send from the ThreadOverview.
     */
    private static final int NOTIFICATION_ID = 1904;

    /**
     * Fields for the input of the thread title and description for the new thread dialog.
     * If the user disables the input dialog and clicks again 'new Thread' these will be set to the
     * EditText of the dialog.
     */
    private static String titleInput = "", descInput = "";

    /**
     * Boolean values to deside whether the spinner for loading threads should be displayed.
     */
    private boolean showSpinnerForLogin = false,
            showSpinnerForGetAllBeapThreads = false,
            showSpinnerForCreateThread = false,
            storeDraft = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_PROGRESS);
        start();
    }

    /**
     * Start the thread overview.
     * Set the content view, check for login, load the threads and start the message pull service.
     */
    private void start() {
        setContentView(R.layout.activity_thread_overview);
        checkForLogin();
        showThreadsInList(ThreadManager.getInstance().getAllThreadsFromSqlite(this));
        AsyncGetAllThreadsFromBeap asyncGetAllThreadsFromBeap = new AsyncGetAllThreadsFromBeap();
        this.showSpinnerForGetAllBeapThreads = true;
        spinnerOn();
        asyncGetAllThreadsFromBeap.execute();
        startMessagePullService();
    }

    /**
     * Start the AsyncTask to check whether the login is valid.
     */
    private void checkForLogin() {
        Log.v(NAME, "checkForLogin()");
        this.showSpinnerForLogin = true;
        spinnerOn();
        new AsyncCheckLogin().execute();
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
     * Starts the message pull service with the parameter of the settings.
     */
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
        if (interval == 0) return;

        long firstTime = SystemClock.elapsedRealtime();

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, interval * 1000, mAlarmSender);
    }

    /**
     * Handles whether the spinner should be displayed.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.v(NAME, "onResume()");
        start();
        startMessagePullService();
        if (shouldSpinn()) {
            spinnerOn();
        } else {
            spinnerOff();
        }
        //addHeader();
    }

    /**
     * Set the menu of thread_overview_actions.
     *
     * @param menu The menu for this activity.
     * @return True if the menu could be set.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_overview_actions, menu);
        return true;
    }

    /**
     * Starts the actions intended for the menu items.
     * - menu_new_thread_action -> starts the new thread dialog.
     * - menu_action_refresh    -> reloads the threads from the beapDB.
     * - menu_blubb_settings    -> starts the settings activity.
     * - menu_action_login      -> starts the login activity with extra LoginType.LOGIN.
     * - menu_action_resetpassw -> starts the login activity with extra LoginType.RESET.
     * - menu_action_logout     -> starts a dialog for performing a logout and disable the
     * message pull service.
     *
     * @param item Menu item selected.
     * @return True if action could be performed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_thread_action:
                newThreadDialog();
                break;
            case R.id.menu_action_refresh:
                showThreadsInList(ThreadManager.getInstance().getAllThreadsFromSqlite(this));
                AsyncGetAllThreadsFromBeap asyncGetAllThreadsFromBeap = new AsyncGetAllThreadsFromBeap();
                this.showSpinnerForGetAllBeapThreads = true;
                spinnerOn();
                asyncGetAllThreadsFromBeap.execute();
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

    /**
     * Shows a new thread dialog. The user can set the title and the description of a new thread.
     * If the user clicks the y-Button a AsyncNewThread will be started.
     */
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
                AsyncNewThread asyncNewThread = new AsyncNewThread();
                showSpinnerForCreateThread = true;
                spinnerOn();
                asyncNewThread.execute(
                        title.getText().toString(),
                        descr.getText().toString());
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

    /**
     * Shows a edit thread dialog. The user can alter either the title and the description for a
     * thread. If the user clicks the y-Button a AsyncSetThread will be started.
     * TODO add the thread status
     *
     * @param thread The BlubbThread which will be modified.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void editThreadDialog(final BlubbThread thread) {
        final Dialog dialog = new Dialog(ActivityThreadOverview.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.edit_thread_dialog, null);
        dialog.setContentView(dialogLayout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View divider = dialog.findViewById(
                dialog.getContext().getResources()
                        .getIdentifier("android:id/titleDivider", null, null)
        );
        divider.setBackground(new ColorDrawable(Color.TRANSPARENT));

        assert dialogLayout != null;
        final EditText titleET = (EditText) dialogLayout.findViewById(
                R.id.thread_list_item_title),
                descriptionET = (EditText) dialogLayout.findViewById(
                        R.id.thread_list_item_description);
        TextView creatorTv = (TextView) dialogLayout.findViewById(R.id.thread_list_item_author);
        creatorTv.setText(thread.gettCreator());

        titleET.setText(thread.getThreadTitle());
        descriptionET.setText(thread.gettDesc());

        Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.thread_status,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }

            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button yBtn = (Button) dialogLayout.findViewById(
                R.id.y_button_dialog);
        Button xBtn = (Button) dialogLayout.findViewById(
                R.id.x_button_dialog);

        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn, xBtn);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(NAME, "onCancel(Dialog)");
                titleInput = titleET.getText().toString();
                descInput = descriptionET.getText().toString();
            }
        });

        yBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String titleString = titleET.getText().toString(),
                        descString = descriptionET.getText().toString();
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
                thread.settTitle(titleET.getText().toString());
                thread.settDesc(descriptionET.getText().toString());
                AsyncSetThread asyncSetThread = new AsyncSetThread(thread);
                showSpinnerForCreateThread = true;
                spinnerOn();
                asyncSetThread.execute();
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

    /**
     * @return True if showSpinnerForGetAllBeapThreads, showSpinnerForLogin or
     * showSpinnerForCreateThread are true.
     */
    private boolean shouldSpinn() {
        return (showSpinnerForGetAllBeapThreads
                || showSpinnerForLogin || showSpinnerForCreateThread);
    }

    /**
     * Sets the blubb_progressbar visible. The blubb_progressbar indicates whether a
     * AsyncTask from ThreadOverview is running.
     */
    public void spinnerOn() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        if (shouldSpinn()) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets the blubb_progressbar invisible. The blubb_progressbar indicates whether a
     * AsyncTask from ThreadOverview is running.
     */
    public void spinnerOff() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.blubb_progressbar);
        if (!shouldSpinn()) {
            pb.setVisibility(View.INVISIBLE);
            //setProgressBarVisibility(false);
        }
    }

    /**
     * Cancel a notification with the NOTIFICATION_ID.
     */
    private void cancelNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    /**
     * Create a notification which shows notificationMessage.
     *
     * @param notificationMessage The message that will be shown with the notification.
     */
    @SuppressWarnings("UnusedDeclaration")
    private void createNotification(String notificationMessage) {
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.blubb_logo);
        builder.setContentTitle(notificationMessage);
        builder.setAutoCancel(true);
        // Including the notification ID allows you to update the notification later on.
        Notification notification = builder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        nManager.notify(NOTIFICATION_ID, notification);
        Log.w(NAME, "pushing notification");
    }

    /**
     * Fills the thread ListView with the threads from the list
     * and sets a OnItemClickListener and OnItemLongClickListener to it.
     * OnItemClick -> start a ActivitySingleThread for this thread.
     * OnItemLongClick -> toggle between the view style of the thread, big or small.
     *
     * @param threads Threads that will be displayed at the ListView.
     */
    private void showThreadsInList(List<BlubbThread> threads) {
        ListView lv = (ListView) findViewById(R.id.thread_list);

        final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                ActivityThreadOverview.this, R.layout.thread_list_entry_big, threads);

        if (adapter.getCount() > threads.size()) {
            spinnerOff();
            return;
        }

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

            }

        });
        lv.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                return true;
            }
        });
    }

    /**
     * Array adapter for the ListView of the threads.
     */
    private class ThreadArrayAdapter extends ArrayAdapter<BlubbThread> {

        /**
         * HashMap to find the id of a thread.
         * Key is the BlubbThread, value the integer of the id of the thread in the
         * array adapter.
         */
        HashMap<BlubbThread, Integer> mIdMap = new HashMap<BlubbThread, Integer>();

        /**
         * Constructor for the ThreadArrayAdapter.
         * Adds the click and longClick listener to the threads.
         *
         * @param context  The current context.
         * @param resource The resource ID for a layout file containing a TextView to use when
         *                 instantiating views.
         * @param objects  The list of BlubbThreads to represent in the ListView.
         */
        public ThreadArrayAdapter(Context context, int resource,
                                  List<BlubbThread> objects) {
            super(context, resource, objects);
            for (int i = 0; i < objects.size(); ++i) {
                final BlubbThread thread = objects.get(i);
                thread.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(NAME, v.toString() + " clicked.");
                        Intent intent = new Intent(
                                ActivityThreadOverview.this, ActivitySingleThread.class);
                        String threadId = thread.gettId();
                        intent.putExtra(ActivitySingleThread.EXTRA_THREAD_ID, threadId);
                        ActivityThreadOverview.this.startActivity(intent);
                    }
                });
                thread.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.d(NAME, v.toString() + " long clicked.");
                        thread.toggleViewSize();
                        ThreadArrayAdapter.this.notifyDataSetChanged();
                        return true;
                    }
                });
                mIdMap.put(objects.get(i), i);
            }
        }

        /**
         * Gets the view of a BlubbThread at a certain position in the ThreadArrayAdapter.
         *
         * @param position    Integer representing the position of a thread.
         * @param convertView Just used for the override.
         * @param parent      That will be the ThreadViews parent view.
         * @return The View representing a BlubbThread.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BlubbThread blubbThread = getItem(position);
            final View tView = blubbThread.getView(ActivityThreadOverview.this, parent);
            Button editBtn = (Button) tView.findViewById(R.id.thread_edit_btn);
            if (editBtn != null) {
                editBtn.setOnClickListener(new View.OnClickListener() {

                    /**
                     * Called when a edit button has been clicked.
                     *
                     * @param v The view that was clicked.
                     */
                    @Override
                    public void onClick(View v) {
                        editThreadDialog(blubbThread);
                    }
                });
            }
            return tView;
        }

        /**
         * Get the id of a BlubbThread at a certain position.
         *
         * @param position Integer representing the position of a thread.
         * @return The id of a BlubbThread in the ThreadArrayAdapter.
         */
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
     * AsyncTask to create a new thread at beapDB and if successful at the local sqlite database.
     */
    private class AsyncNewThread extends AsyncTask<String, Void, BlubbThread> {

        /**
         * Exception field for the exception handling.
         */
        Exception exception;

        /**
         * Executes the creation of a new BlubbThread on a background java.lang.Thread.
         *
         * @param parameter First must be the title, second the description of the new thread.
         * @return The newly created BlubbThread.
         */
        @Override
        protected BlubbThread doInBackground(String... parameter) {
            Log.v("AsyncNewThread", "execute()");
            String title = parameter[0],
                    description = parameter[1];
            try {
                return ThreadManager.getInstance().createThread(
                        ActivityThreadOverview.this.getApplicationContext(), title, description);
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        /**
         * Handles exceptions and if the creation of the thread was successful makes a toast.
         *
         * @param thread That has been created.
         */
        @Override
        protected void onPostExecute(BlubbThread thread) {
            getApp().handleException(exception);
            if (thread != null) {
                String msg = "Created new Thread:\n" +
                        "tId: " + thread.gettId() + "\n" +
                        "tTitle: " + thread.getThreadTitle();
                Log.i("AsyncNewThread", msg);
                Toast.makeText(ActivityThreadOverview.this, msg, Toast.LENGTH_LONG).show();
                cancelNotification();
            } // if null there has been a toast.

            showThreadsInList(ThreadManager.getInstance().getAllThreadsFromSqlite(
                    ActivityThreadOverview.this));
            AsyncGetAllThreadsFromBeap asyncGetAllThreadsFromBeap = new AsyncGetAllThreadsFromBeap();
            asyncGetAllThreadsFromBeap.execute();
            showSpinnerForCreateThread = false;
            spinnerOff();
        }
    }

    /**
     * AsyncTask which will get all Threads from the beapDB and update the sqlite database.
     */
    private class AsyncGetAllThreadsFromBeap extends AsyncTask<Void, Void, Boolean> {
        /**
         * Exception field for the exception handling.
         */
        Exception e;

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.v("AsyncGetAllThreads", "execute()");
            try {
                return ThreadManager.getInstance().updateAllThreadsFromBeap(
                        ActivityThreadOverview.this.getApplicationContext());
            } catch (Exception e) {
                this.e = e;
                return false;
            }
        }

        /**
         * Handles the exception, shows the thread in the thread list and stops the spinner.
         *
         * @param response List of BlubbThreads received from the beapDB.
         */
        @Override
        protected void onPostExecute(Boolean response) {
            getApp().handleException(e);
            if (response) {
                showThreadsInList(ThreadManager.getInstance().getAllThreadsFromSqlite(
                        ActivityThreadOverview.this));
            }
            showSpinnerForGetAllBeapThreads = false;
            spinnerOff();
        }
    }

    /**
     * AsyncTask to check whether the login can be performed, if no valid login parameter are
     * available and there are no threads in the sqlite db, the login screen will be started.
     */
    private class AsyncCheckLogin extends AsyncTask<Void, Void, Boolean> {
        /**
         * In case an e is thrown while executing doInBackground(..).
         */
        private Exception e;

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v(NAME, "AsyncLogin");
            try {
                SessionManager.getInstance().getSessionID(
                        ActivityThreadOverview.this.getApplicationContext());
                return true;
            } catch (Exception e) {
                this.e = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isLoggedIn) {
            getApp().handleException(e);
            // if there was no e toast will be null - everything is alright.
            if (!isLoggedIn) {
                // if the e is a ConnectionException don't let user perform manual login
                if (e.getClass().equals(SessionException.class)) {
                    DatabaseHandler db = new DatabaseHandler(ActivityThreadOverview.this);
                    int counter = db.getThreadCount();
                    if (counter == 0) {
                        Intent intent = new Intent(ActivityThreadOverview.this,
                                ActivityLogin.class);
                        intent.putExtra(ActivityLogin.EXTRA_LOGIN_TYPE,
                                ActivityLogin.LoginType.LOGIN);
                        ActivityThreadOverview.this.startActivity(intent);
                    }
                }
            }
            showSpinnerForLogin = false;
            spinnerOff();
        }
    }

    /**
     * AsyncTask to modify a thread at beapDB. If successful a Toast will be made.
     */
    private class AsyncSetThread extends AsyncTask<Void, Void, Boolean> {
        /**
         * In case an e is thrown while executing doInBackground(..).
         */
        private Exception e;

        /**
         * BlubbThread that will be modified.
         */
        private BlubbThread thread;

        /**
         * Constructor for the AsyncSetThread.
         *
         * @param thread The BlubbThread which will be modified.
         */
        public AsyncSetThread(BlubbThread thread) {
            this.thread = thread;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return ThreadManager.getInstance()
                        .setThread(ActivityThreadOverview.this, thread);
            } catch (Exception e) {
                this.e = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            getApp().handleException(e);
            if (response) {
                onResume();
                Toast.makeText(ActivityThreadOverview.this,
                        getResources().getString(R.string.set_thread_succsessful_text),
                        Toast.LENGTH_LONG).show();
            }
        }


    }
}
