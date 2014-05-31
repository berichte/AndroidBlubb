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
import android.util.Log;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.MessageManager;
import com.blubb.alubb.blubbbasics.SingleThreadActivity;

import java.util.List;

public class MessagePullService extends Service {
    public static final String NAME = MessagePullService.class.getName();
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(R.string.alarm_service_started);
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
    private void showNotification() {
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
        notificationManager.notify(R.string.alarm_service_started, notification);
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

    private class MessagePullAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.i("Pulling", "try to pull msgs from server.");
            try {
                return MessageManager.newMessagesAvailable(MessagePullService.this);
            } catch (Exception e) {
                Log.i(NAME, e.getMessage());
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean newMsgs) {
            if(newMsgs) showNotification();
        }
    }

}
