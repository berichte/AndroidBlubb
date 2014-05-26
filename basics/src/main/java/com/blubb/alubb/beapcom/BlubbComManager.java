package com.blubb.alubb.beapcom;

import android.util.Log;

import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbParameterChecker;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.SessionInfo;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbComManager {

    public BlubbComManager() {

    }

       public BlubbMessage[] getLatestMessages(
            BlubbThread blubbThread, int messageCounter)
            throws BlubbDBException, BlubbDBConnectionException {
        return new BlubbMessage[0];
    }

    public BlubbMessage[] getMessages(String blubbThreadId) throws BlubbDBException {
        String url = BlubbRequestBuilder.buildQuery(
                "tree.functions.getMsgsForThread(self, '" + blubbThreadId + "')");
        BlubbMessage[] messages = null;
        try {
            BlubbResponse response = executeRequest(url);
            if(response.getResultObj().getClass().getName().equals(
                    JSONArray.class.getName())) {
                JSONArray jsonArray = (JSONArray) response.getResultObj();
                try {
                    if (jsonArray.getJSONObject(0).has("mType")) {
                        messages = new BlubbMessage[jsonArray.length()];
                        for(int i = 0; i < jsonArray.length(); i++) {
                            messages[i] = new BlubbMessage(jsonArray.getJSONObject(i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else throw new BlubbDBException("did not get a jsonArray as expected. :(");
        } catch (InvalidParameterException e) {
            Log.e("invalidParameter-getAllThreads", e.getMessage());
        }
        return messages;
    }

    public BlubbDBReplyStatus sendMessage(BlubbMessage message) throws BlubbDBException, BlubbDBConnectionException {
        return null;
    }

    public BlubbThread[] getAllThreads() throws BlubbDBException, BlubbDBConnectionException {
        //get valid query request string
        String url = BlubbRequestBuilder.buildQuery("tree.functions.getAllThreads(self)");
        BlubbThread[] threads = null;
        try {
            BlubbResponse response = executeRequest(url);
            if(response.getResultObj().getClass().getName().equals(
                    JSONArray.class.getName())) {
                    JSONArray jsonArray = (JSONArray) response.getResultObj();
                try {
                    if (jsonArray.getJSONObject(0).has("tType")) {
                        threads = new BlubbThread[jsonArray.length()];
                        for(int i = 0; i < jsonArray.length(); i++) {
                            threads[i] = new BlubbThread(jsonArray.getJSONObject(i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else throw new BlubbDBException("did not get a jsonArray as expected. :(");
        } catch (InvalidParameterException e) {
            Log.e("invalidParameter-getAllThreads", e.getMessage());
        }
        return threads;
    }

    public BlubbDBReplyStatus openNewBlubbThread(BlubbThread newThread) throws BlubbDBException, BlubbDBConnectionException {
        return null;
    }

    public boolean login(String username, String password)
            throws InvalidParameterException, BlubbDBException {
        //check the parameter
        BlubbParameterChecker.checkString(username);
        BlubbParameterChecker.checkString(password);
        // get a valid request string
        String requestString = BlubbRequestBuilder.buildLogin(username, password);

        BlubbResponse responseObj = executeRequest(requestString);
        // check whether the response is ok or there is some error
        if (responseObj.getStatus() == BlubbDBReplyStatus.OK) {
            SessionInfo info = responseObj.getSessionInfo();
            SessionManager.getInstance().setSession(info);
            return true;
        } else {
            throw new BlubbDBException(responseObj.getStatusDescr());
        }
    }

    private BlubbResponse executeRequest(String url) throws InvalidParameterException {
        // request via http
        String httpResponse = BlubbHttpRequest.request(url);
        //parse the response to an object
        return new BlubbResponse(httpResponse);
    }
}
