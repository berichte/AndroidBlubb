package com.blubb.alubb.blubbbasics;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.QuickCheck;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbException;

import java.util.ArrayList;
import java.util.List;

/**
 * The PullService extends the android.app.Service and performs a quickCheck at the
 * beapDB server. If there are new messages or threads available it will post a notification.
 */
public class PullService extends Service {

    /**
     * Name for Logging purposes.
     */
    public static final String NAME = PullService.class.getName();
    /**
     * The id for the notifications.
     */
    private static final int NOTIFICATION_ID = 5683;
    /**
     * Instance of the notification manager used.
     */
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    /**
     * Starts a AsyncMessagePull.
     *
     * @param intent  See super.onStartCommand(..).
     * @param flags   See super.onStartCommand(..).
     * @param startId See super.onStartCommand(..).
     * @return See super.onStartCommand(..).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new AsyncMessagePull().execute();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Push a notification to the android system.
     *
     * @param builder The NotificationCompat.Builder building the notification.
     */
    private void issueNotification(NotificationCompat.Builder builder) {
        // Including the notification ID allows you to update the notification later on.
        Notification notification = builder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
        Log.v(NAME, "pushing notification");
    }

    /**
     * Creates a new message notification, issues it and builds it with a
     * NotificationCompat.Builder. If there are more than one new message it will show only
     * the authors of the messages.
     *
     * @param messages List containing one or more BlubbMessages.
     */
    private void createMessageNotification(List<BlubbMessage> messages) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.blubb_logo);
        Intent resultIntent;
        if (messages.size() > 1) {
            String nTitle = "You received " + messages.size() + " new messages";
            String nContent = "From:\n";
            for (BlubbMessage m : messages) {
                nContent = nContent + m.getmCreator() + "\n";
            }
            builder.setContentTitle(nTitle);

            builder.setContentText(nContent);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(nContent));

            resultIntent = new Intent(this, ActivityThreads.class);
        } else if (messages.size() == 1) {
            BlubbMessage message = messages.get(0);
            builder.setContentTitle(message.getmCreator() + "\n" + message.getmTitle());
            builder.setContentText(message.getmContent().getStringRepresentation());

            resultIntent = new Intent(this, ActivityMessages.class);

            String threadId = message.getmThread();
            resultIntent.putExtra(ActivityMessages.EXTRA_THREAD_ID, threadId);

        } else return;

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setAutoCancel(true);
        builder.setContentIntent(resultPendingIntent);
        issueNotification(builder);
    }

    /**
     * Creates a new threads notification, issues it and builds it with a
     * NotificationCompat.Builder. If there are more than one new message it will show only
     * the
     *
     * @param threads List containing one or more BlubbThreads.
     */
    private void createThreadNotification(List<BlubbThread> threads) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.blubb_logo);
        if (threads.size() > 1) {

            String nTitle = "You received " + threads.size() + " new threads";
            String nContent = "";
            for (BlubbThread t : threads) {
                nContent = nContent + t.getThreadTitle() + "\n";
            }
            builder.setContentTitle(nTitle);

            builder.setContentText(nContent);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(nContent));
        } else if (threads.size() == 1) {
            BlubbThread thread = threads.get(0);
            builder.setContentTitle(thread.getThreadTitle());
            builder.setContentText(thread.gettDesc());
        } else return;
        Intent resultIntent = new Intent(this, ActivityThreads.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        issueNotification(builder);
    }

    /**
     * Get the custom BlubbApplication, e.g. to handle exceptions.
     *
     * @return The custom Application instance.
     */
    @SuppressWarnings("UnusedDeclaration")
    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    /**
     * AsyncTask which performs a quickCheck at the blubbDB and triggers the notification building
     * if there are new threads or messages.
     */
    private class AsyncMessagePull extends AsyncTask<Void, Void, QuickCheck> {
        /**
         * Exception caught while executing doInBackground.
         */
        BlubbException blubbException;

        /**
         * Performs the quickCheck at the SessionManager.
         *
         * @param voids Just no parameter needed.
         * @return QuickCheck object containing the new messages or threads.
         */
        @Override
        protected QuickCheck doInBackground(Void... voids) {
            Log.i(NAME, "try to pull msgs from server.");
            SessionManager sManager = SessionManager.getInstance();
            try {
                return sManager.quickCheck(PullService.this);
            } catch (BlubbDBConnectionException e) {
                // User must not be notified if service has no connection.
                Log.e(NAME, e.getClass().getName() + ": " + e.getMessage());
                return new QuickCheck(new ArrayList<BlubbThread>(), new ArrayList<BlubbMessage>());
            } catch (BlubbException e) {
                blubbException = e;
                return new QuickCheck(new ArrayList<BlubbThread>(), new ArrayList<BlubbMessage>());
            }
        }

        /**
         * If the quickCheck has a result the notification for threads or messages will be
         * triggered.
         *
         * @param quickCheck The QuickCheck object containing lists of BlubbMessages and
         *                   BlubbThreads.
         */
        @Override
        public void onPostExecute(QuickCheck quickCheck) {
            getApp().handleException(blubbException);
            if (quickCheck.hasResult()) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (quickCheck.messages.size() > 0) {
                    createMessageNotification(quickCheck.messages);
                }
                if (quickCheck.threads.size() > 0) {
                    createThreadNotification(quickCheck.threads);
                }
                mNotificationManager.cancel(R.string.alarm_service_started);
            }
            PullService.this.stopSelf();
        }
    }
}