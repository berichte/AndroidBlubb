package com.blubb.alubb.beapcom;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MessagePullService extends IntentService {
    public static final String NAME = MessagePullService.class.getName();

    public MessagePullService() {
        super(NAME);
    }

    private BlubbComManager manager;
    private MessageReceiveActivity activity;

    public MessagePullService(MessageReceiveActivity activity) {
        super(NAME);
        this.activity = activity;
        this.manager = new BlubbComManager();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
