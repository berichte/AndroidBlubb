package com.blubb.alubb.beapcom;

import android.util.Log;

import com.blubb.alubb.basics.BPC;
import com.blubb.alubb.basics.BlubbMessage;
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
    private static final String F_SELF = "(self, '",
                                 F_SEP = "', '",
                                F_END = "')";

    public BlubbComManager() {

    }

    public BlubbMessage[] getLatestMessages(
            BlubbThread blubbThread, int messageCounter)
            throws BlubbDBException, BlubbDBConnectionException {
        return new BlubbMessage[0];
    }

    public BlubbMessage[] getMessages(String blubbThreadId) throws BlubbDBException {
        // Build the request for the BeapDB
        String url = BlubbRequestBuilder.buildQuery(
                "tree.functions.getMsgsForThread"       + F_SELF +
                        blubbThreadId                   + F_END);
        BlubbMessage[] messages;
        // execute the request
        BlubbResponse response = executeRequest(url);
        // if the response is empty return an empty array of Messages
        if(response.getStatus() == BlubbDBReplyStatus.NO_CONTENT){
            BlubbMessage m = new BlubbMessage("no-thread-for-this",
                    "It's empty oO", "Quick be the first who posts here.");
            return new BlubbMessage[] {m};

        }
        // Check whether the response is an array
        if(response.getResultObj().getClass().getName().equals(
                JSONArray.class.getName())) {
            // fill the array
            JSONArray jsonArray = (JSONArray) response.getResultObj();
            try {
                // check whether the type is right - expect mType for messages
                if (jsonArray.getJSONObject(0).has("mType")) {
                    messages = new BlubbMessage[jsonArray.length()];
                    for(int i = 0; i < jsonArray.length(); i++) {
                        messages[i] = new BlubbMessage(jsonArray.getJSONObject(i));
                    }
                    return messages;
                }
                // throw dbException if the response type is wrong
                else {
                    Log.e("BlubbComManager.getMsgsForThread", "Received wrong " +
                            "response type, expected 'mType'");
                    throw new BlubbDBException("Received wrong response type. " +
                            "Expected 'mType'");
                }
                // if there is no object in the array throw a db exception.
            } catch (JSONException e) {
                Log.e("BlubbComManager.getMsgsForThread", "No json object in msg_array.");
                throw new BlubbDBException("Database response has some error: " +
                        "No json object in msg_array.");
            }
            // throw a dbException if the result is not a json array.
        } else throw new BlubbDBException("did not get a jsonArray as expected. :(");
    }

    public BlubbDBReplyStatus sendMessage(String tId, String mTitle, String mContent)
            throws BlubbDBException, BlubbDBConnectionException, InvalidParameterException {
        // Build the request for the BeapDB .createMsg(self, "tID", "mTitle", "mContent")
        String url = BlubbRequestBuilder.buildQuery(
                "tree.functions.createMsg"          + F_SELF +
                    tId                             + F_SEP +
                    BPC.parseStringToDB(mTitle)     + F_SEP +
                    BPC.parseStringToDB(mContent)   + F_END);

        // execute the request
        BlubbResponse response = executeRequest(url);
        // return the reply status
        return response.getStatus();
    }

    public BlubbThread[] getAllThreads() throws BlubbDBException, BlubbDBConnectionException {
        //get valid query request string
        String url = BlubbRequestBuilder.buildQuery("tree.functions.getAllThreads(self)");
        BlubbThread[] threads = null;
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
        return threads;
    }

    public BlubbDBReplyStatus openNewBlubbThread(String tTitle, String tDescr)
            throws BlubbDBException, InvalidParameterException {
        BPC.parseStringToDB(tTitle);
        BPC.parseStringToDB(tDescr);
        String url = BlubbRequestBuilder.buildQuery(
                "tree.functions.createThread(self, '" + tTitle + "', '" + tDescr + "')");
        BlubbResponse response = executeRequest(url);
        return response.getStatus();
    }

    public boolean login(String username, String password)
            throws InvalidParameterException, BlubbDBException {
        //check the parameter
        username = BPC.parseStringToDB(username);
        password = BPC.parseStringToDB(password);
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

    private BlubbResponse executeRequest(String url) {
        Log.i("BlubbComManager", "Executing http-request:\n" + url);
        // request via http
        String httpResponse = BlubbHttpRequest.request(url);
        //parse the response to an object
        return new BlubbResponse(httpResponse);
    }
}