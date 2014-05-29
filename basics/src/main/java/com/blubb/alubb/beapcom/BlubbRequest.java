package com.blubb.alubb.beapcom;

import android.os.AsyncTask;

import com.blubb.alubb.blubexceptions.InvalidParameterException;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */

public class BlubbRequest extends AsyncTask <String, Integer, String> {

    private BlubbReplyReceiver receiver;

    public BlubbRequest(BlubbReplyReceiver receiver) {
        this.receiver = receiver;
    }

     @Override
     protected String doInBackground(String... uri) {
        return BlubbHttpRequest.request(uri[0]);
     }

     @Override
     protected void onPostExecute(String result) {
         super.onPostExecute(result);
         BlubbResponse response = new BlubbResponse(result);
         receiver.receiveResponse(response);
     }
}
