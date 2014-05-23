package com.blubb.alubb.beapcom;

import android.content.Context;

import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbParameterChecker;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.basics.SessionInfo;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

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

    public BlubbMessage[] getMessages(BlubbThread blubbThread) {
        return new BlubbMessage[0];
    }

    public BlubbDBReplyStatus sendMessage(BlubbMessage message) throws BlubbDBException, BlubbDBConnectionException {
        return null;
    }

    public BlubbThread[] getActualBlubbThreads() throws BlubbDBException, BlubbDBConnectionException {
        return new BlubbThread[0];
    }

    public BlubbDBReplyStatus openNewBlubbThread(BlubbThread newThread) throws BlubbDBException, BlubbDBConnectionException {
        return null;
    }

    public boolean login(String username, String password)
            throws InvalidParameterException {
        BlubbParameterChecker.checkString(username);
        BlubbParameterChecker.checkString(password);
        String requestString = BlubbRequestBuilder.buildLogin(username, password);
        String httpResponse = BlubbHttpRequest.request(requestString);
        BlubbResponse responseObj = new BlubbResponse(httpResponse);
        if (responseObj.getStatus() == BlubbDBReplyStatus.OK) {
            SessionInfo info = (SessionInfo) responseObj.getResponseObj();
            SessionManager.getInstance().setSessionId(info);
            return true;
        }
        return false;
    }
}
