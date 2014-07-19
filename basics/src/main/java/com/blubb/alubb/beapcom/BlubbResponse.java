package com.blubb.alubb.beapcom;

import android.util.Log;

import com.blubb.alubb.basics.SessionInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Benjamin Richter on 22.05.2014.
 * General Response object representing the response from beap.
 */
public class BlubbResponse {

    /**
     * Name for Logging purposes
     */
    private static final String NAME = "BlubbResponse";

    /**
     * Reply status for this response.
     */
    private BeapReplyStatus status;

    /**
     * The status description.
     */
    private String statusDesc;

    /**
     * The result json object.
     */
    private Object resultObj;

    /**
     * SessionInfo object for this response.
     */
    private SessionInfo sessionInfo;

    /**
     * Constructor for the response via a json object.
     *
     * @param jsonResponse Must be a valid json object formed like:
     *                     {
     *                     BeapStatus:     200,
     *                     StatusDescr:    "OK",
     *                     Result :        [ ...  ], //Array of json objects.
     *                     sessInfo :      { ... }
     *                     }
     */
    public BlubbResponse(String jsonResponse) {
        JSONObject response;
        try {
            response = new JSONObject(jsonResponse);
            this.status = this.parseReply(response.getInt("BeapStatus"));
            this.statusDesc = response.getString("StatusDescr");
            this.sessionInfo = parseSessionInfo(response);
            if (status.equals(BeapReplyStatus.OK)) {
                this.resultObj = this.parseResponseObject(response);
            }
        } catch (JSONException e) {
            Log.e("json exception", e.getMessage());
            this.status = BeapReplyStatus.UNKNOWN_STATUS;
        }
        Log.v(NAME, "Constructed new BlubbResponse - Status: " + status);

    }

    /**
     * Get the BeapReplyStatus.
     *
     * @return BeapReplyStatus.
     */
    public BeapReplyStatus getStatus() {
        return status;
    }

    /**
     * Get the status description.
     *
     * @return String with the status description.
     */
    public String getStatusDesc() {
        return statusDesc;
    }

    /**
     * Get the result of the response. This can either be a json array, a json object or an integer.
     *
     * @return The result object.
     */
    public Object getResultObj() {
        return resultObj;
    }

    /**
     * Parse the beap status integer to a BeapReplyStatus.
     *
     * @param replyStatus Integer value representing a certain status of the response.
     * @return BeapReplyStatus according to the given integer.
     */
    private BeapReplyStatus parseReply(int replyStatus) {
        switch (replyStatus) {
            case 200:
                return BeapReplyStatus.OK;
            case 203:
                return BeapReplyStatus.NO_CONTENT;
            case 204:
                return BeapReplyStatus.SESSION_ALREADY_DELETED;
            case 400:
                return BeapReplyStatus.REQUEST_FAILURE;
            case 401:
                return BeapReplyStatus.LOGIN_REQUIRED;
            case 403:
                return BeapReplyStatus.PERMISSION_DENIED;
            case 407:
                return BeapReplyStatus.CONNECTION_ERROR;
            case 406:
                return BeapReplyStatus.PARAMETER_ERROR;
            case 409:
                return BeapReplyStatus.PARAMETER_ERROR;
            case 418:
                return BeapReplyStatus.SYNTAX_ERROR;
            case 423:
                return BeapReplyStatus.PASSWORD_INIT;
            default:
                return BeapReplyStatus.UNKNOWN_STATUS;
        }
    }

    /**
     * Get the session info out of a beap response.
     *
     * @param response The response json object with the session info object.
     * @return A SessionInfo with the data from the response or if the response is not valid
     * an 'empty' SessionInfo.
     */
    private SessionInfo parseSessionInfo(JSONObject response) {
        try {
            JSONObject obj = response.getJSONObject("sessInfo");
            return new SessionInfo(obj);
        } catch (JSONException e) {
            Log.e("parsing json", "there's no session info.");
            return new SessionInfo();
        }
    }

    /**
     * Parse a json object to either a json array, json object or an integer.
     *
     * @param json
     * @return
     */
    private Object parseResponseObject(JSONObject json) {
        Object o = null;
        if (json.has("Result")) {
            try {
                o = json.getJSONArray("Result");
            } catch (JSONException e) {
                Log.v("parsing json", "There's no \"Result\" for JsonArray.");
            }
            if (o == null) {
                try {
                    o = json.getJSONObject("Result");
                } catch (JSONException e) {
                    Log.v("parsing json", "Threre's no \"Result\" for JsonObject. o.O");
                }
            }
            if (o == null) {
                try {
                    o = json.getInt("Result");
                } catch (JSONException e) {
                    Log.v("parsing json", "Threre's no \"Result\" for int. o.O");
                }
            }
            return o;
        }
        return null;
    }

    /**
     * Get the SessionInfo.
     *
     * @return SessionInfo from this response.
     */
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }
}