package com.blubb.alubb.beapcom;

import android.util.Log;

import com.blubb.alubb.basics.SessionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.blubb.alubb.blubexceptions.InvalidParameterException;

/**
 * Created by Benjamin Richter on 22.05.2014.
 * General Response object
 */
public class BlubbResponse {

    public BlubbDBReplyStatus getStatus() {
        return status;
    }

    public String getStatusDescr() {
        return statusDescr;
    }

    public Object getResultObj() {
        return resultObj;
    }

    private BlubbDBReplyStatus status;
    private String statusDescr;
    private Object resultObj;
    private SessionInfo sessionInfo;


    public BlubbResponse(String jsonResponse) throws InvalidParameterException {
        JSONObject response;
        try {
            response = new JSONObject(jsonResponse );
            this.status = this.parseReply(response.getInt("BeapStatus"));
                   // RA.getString(R.string.json_beap_status)));
            this.statusDescr = response.getString("StatusDescr");
                    //RA.getString(R.string.json_beap_status_desc));
            this.resultObj = this.parseResponseObject(response);
        } catch (JSONException e) {
            Log.e("json exception", e.getMessage());
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
            throws InvalidParameterException {
        try {
            JSONObject obj = json.getJSONObject("sessInfo");
            this.sessionInfo =  new SessionInfo(obj);
        } catch (JSONException e) {
            Log.i("parsing json", "there's no sessioninfo.");
        }
      /*  try {
            JSONObject obj = json.getJSONObject("Result");
            String jsonType = obj.getString("tType");
            if(jsonType.equals("Thread")) {

            }
            return new SessionInfo(obj);
        } catch (JSONException e) {
            Log.i("parsing json", "it's not a session");
        } */

        try {
            if(json.has("Result")){
                return json.getJSONArray("Result");
            }

        } catch (JSONException e) {
            Log.i("parsing json", "it's not a even an array. oO");
        }
        return null;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }
}