package com.blubb.alubb.beapcom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.blubbbasics.ActivitySingleThread;
import com.blubb.alubb.blubbbasics.ActivityThreadOverview;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MessagePullService extends Service {
    public static final String NAME = MessagePullService.class.getName();
    private static final int NOTIFICATION_ID = 5683;
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(R.string.alarm_service_started);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MessagePullAsync messagePullAsync = new MessagePullAsync();
        messagePullAsync.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    private void issueNotification(NotificationCompat.Builder builder) {
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Including the notification ID allows you to update the notification later on.
        Notification notification = builder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
        Log.w(NAME, "pushing notification");
    }

    private void createMessageNotification(List<BlubbMessage> messages) {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
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

            resultIntent = new Intent(this, ActivityThreadOverview.class);
        } else if (messages.size() == 1) {
            BlubbMessage message = messages.get(0);
            builder.setContentTitle(message.getmCreator() + "\n" + message.getmTitle());
            builder.setContentText(message.getmContent());

            resultIntent = new Intent(this, ActivitySingleThread.class);

            String threadId = message.getmThread();
            resultIntent.putExtra(ActivitySingleThread.EXTRA_THREAD_ID, threadId);

        } else return;

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setAutoCancel(true);
        builder.setContentIntent(resultPendingIntent);
        issueNotification(builder);
    }

    private void createThreadNotification(List<BlubbThread> threads) {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.blubb_logo);
        if (threads.size() > 1) {
            String nTitle = threads.size() + " threads have been created.";
            builder.setContentTitle(nTitle);
        } else if (threads.size() == 1) {
            BlubbThread thread = threads.get(0);
            builder.setContentTitle(thread.getThreadTitle());
            builder.setContentText(thread.gettDesc());
        } else return;
        Intent resultIntent = new Intent(this, ActivityThreadOverview.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        issueNotification(builder);
    }

    private class MessagePullAsync extends AsyncTask<Void, Void, QuickCheck> {

        @Override
        protected QuickCheck doInBackground(Void... voids) {
            Log.i(NAME, "try to pull msgs from server.");
            SessionManager sManager = SessionManager.getInstance();
            try {
                return sManager.quickCheck(MessagePullService.this);
            } catch (SessionException e) {
                Log.e(NAME, e.getMessage());
            } catch (BlubbDBException e) {
                Log.e(NAME, e.getMessage());
            } catch (BlubbDBConnectionException e) {
                Log.e(NAME, e.getMessage());
            } catch (JSONException e) {
                Log.e(NAME, e.getMessage());
            } catch (PasswordInitException e) {
                Log.e(NAME, e.getMessage());
            }
            return new QuickCheck(new ArrayList<BlubbThread>(), new ArrayList<BlubbMessage>());
        }

        @Override
        public void onPostExecute(QuickCheck quickCheck) {
            if (quickCheck.hasResult()) {
                if (quickCheck.messages.size() > 0) {
                    createMessageNotification(quickCheck.messages);
                }
                if (quickCheck.threads.size() > 0) {
                    createThreadNotification(quickCheck.threads);
                }
            }
            MessagePullService.this.stopSelf();
        }
    }

}
