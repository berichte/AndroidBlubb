package com.blubb.alubb.beapcom;

import android.app.IntentService;
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
import com.blubb.alubb.basics.MessageManager;
import com.blubb.alubb.blubbbasics.SingleThreadActivity;
import com.blubb.alubb.blubbbasics.ThreadOverview;

import java.util.ArrayList;
import java.util.List;

public class MessagePullService extends Service {
    public static final String NAME = MessagePullService.class.getName();
    private static final int NOTIFICATION_ID = 5683;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    @Override
    public void onCreate() {


    }

    @Override
    public void onDestroy() {
        if(mNotificationManager!=null) {
            mNotificationManager.cancel(R.string.alarm_service_started);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        MessagePullAsync messagePullAsync = new MessagePullAsync();
        messagePullAsync.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String mAuthor, String mTitle, String mContent) {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.alubb);
        builder.setContentTitle(mTitle);
        builder.setContentText(mAuthor);
        builder.setDefaults(Notification.DEFAULT_ALL);

        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(mContent));

        Intent resultIntent = new Intent(this, ThreadOverview.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        issueNotification(builder);

        /*
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.alarm_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.alubb, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SingleThreadActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.alarm_service_started),
                text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.string.alarm_service_started, notification); */
    }

    private void issueNotification(NotificationCompat.Builder builder) {
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Including the notification ID allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    /**
     * This is the object that receives interactions from clients.  See RemoteService
     * for a more complete example.
     */
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    private class MessagePullAsync extends AsyncTask<Void, Void, List<BlubbMessage>> {

        @Override
        protected List<BlubbMessage> doInBackground(Void... voids) {
            Log.i(NAME, "try to pull msgs from server.");/**
            try {
                if(MessageManager.getInstance().newMessagesAvailable(MessagePullService.this)) {

                    List<BlubbMessage> list =
                            MessageManager.getNewMessages(MessagePullService.this);
                    Log.i(NAME, "huray, " + list.size() + "new Messages.");
                    return list;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(NAME, e.getMessage());
            }*/
            return new ArrayList<BlubbMessage>();
        }

        @Override
        public void onPostExecute(List<BlubbMessage> newMsgs) {
            if(!newMsgs.isEmpty()) {
                if(newMsgs.size()>1) {
                    Log.i(NAME, "More than one Message!");
                    String mCreator = newMsgs.size() + " new Messages.",
                            mTitle = "";
                    String mContent = "";
                    for(BlubbMessage bm: newMsgs) {
                        mContent = mContent + bm.getmCreator() + ": " + bm.getmTitle() + "\n";
                    }
                    showNotification(mCreator, mTitle, mContent);
                } else {
                    Log.i(NAME, "at least one new message.");
                    BlubbMessage msg = newMsgs.get(0);
                    showNotification(msg.getmCreator(), msg.getmTitle(), msg.getmContent());
                }
            }


            MessagePullService.this.stopSelf();
        }
    }

}
