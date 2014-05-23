package com.blubb.alubb.beapcom;

import android.util.JsonReader;

import com.blubb.alubb.basics.SessionInfo;
import com.blubb.alubb.blubbbasics.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.blubb.alubb.basics.RA;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import java.util.List;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbResponse {

    public BlubbDBReplyStatus getStatus() {
        return status;
    }

    public String getStatusDescr() {
        return statusDescr;
    }

    public Object getResponseObj() {
        return responseObj;
    }

    private BlubbDBReplyStatus status;
    private String statusDescr;
    private Object responseObj;


    public BlubbResponse(String jsonResponse) throws InvalidParameterException {
        JSONObject response;
        try {
            response = new JSONObject(jsonResponse );
            this.status = this.parseReply(response.getInt("BeapStatus"));
                   // RA.getString(R.string.json_beap_status)));
            this.statusDescr = response.getString("StatusDescr");
                    //RA.getString(R.string.json_beap_status_desc));
            this.responseObj = this.parseResponseObject(response);
        } catch (JSONException e) {

        }
    }

    private BlubbDBReplyStatus parseReply(int replyStatus) {
        switch (replyStatus) {
            case 200:
                return BlubbDBReplyStatus.OK;
            case 203:
                return BlubbDBReplyStatus.NO_CONTENT;
            case 204:
                return BlubbDBReplyStatus.SESSION_ALREADY_DELETED;
            case 400:
                return BlubbDBReplyStatus.REQUEST_FAILURE;
            case 401:
                return BlubbDBReplyStatus.LOGIN_REQUIRED;
            case 403:
                return BlubbDBReplyStatus.PERMISSION_DENIED;
            case 407:
                return BlubbDBReplyStatus.CONNECTION_ERROR;
            case 406:
                return BlubbDBReplyStatus.PARAMETER_ERROR;
            case 409:
                return BlubbDBReplyStatus.PARAMETER_ERROR;
            case 418:
                return BlubbDBReplyStatus.SYNTAX_ERROR;
            default:
                return BlubbDBReplyStatus.UNKNOWN_STATUS;
        }
    }

    private Object parseResponseObject(JSONObject json)
            throws InvalidParameterException, JSONException {
        JSONObject obj = json.getJSONObject("sessInfo");
                //RA.getString(R.string.json_session_info));
        return new SessionInfo(obj);
    }
}