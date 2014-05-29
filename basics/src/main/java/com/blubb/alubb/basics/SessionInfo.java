package com.blubb.alubb.basics;

import com.blubb.alubb.R;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class SessionInfo {

    private String sessionId;
    private String blubbUser;
    private BlubbUserRole role;
    private boolean sessionActive;
    private String expDate;

    public SessionInfo(String sessionId, String blubbUser, BlubbUserRole blubbUserRole,
                       boolean sessionActive, String expDate) {
        this.sessionId = sessionId;
        this.blubbUser = blubbUser;
        this.expDate = expDate;
        this.sessionActive = sessionActive;
    }

    public SessionInfo(JSONObject jsonObject)
            throws JSONException {
        this.sessionId = jsonObject.getString(
                "sessId");//RA.getString(R.string.json_session_id));
        this.blubbUser = jsonObject.getString(
                "sessUser");//RA.getString(R.string.json_session_user));
        this.sessionActive = jsonObject.getBoolean(
                "sessActive");//RA.getString(R.string.json_session_active));
        this.expDate = jsonObject.getString(
                "expires");//RA.getString(R.string.json_expires));
        this.role = getUserRoleViaString(jsonObject.getString(
                "sessRole"));//RA.getString(R.string.json_session_role)));
    }

    private BlubbUserRole getUserRoleViaString(String role) {
        if (role.equals("user")) {
            return BlubbUserRole.BLUBB_USER;
        }
        if (role.equals("admin")) {
            return BlubbUserRole.BLUBB_ADMIN;
        }
        if (role.equals("PL")) {
            return BlubbUserRole.BLUBB_MANAGER;
        }
        return BlubbUserRole.UNDEFINED;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getBlubbUser() {
        return blubbUser;
    }

    public BlubbUserRole getRole() {
        return role;
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    public String getExpDate() {
        return expDate;
    }


    public String toString() {
        return "SessionInfo:\n"
                + "SessionId:\t" + this.sessionId + "\n"
                + "User:\t" + this.blubbUser + "\n"
                + "Role:\t" + this.role + "\n"
                + "Active:\t" + this.isSessionActive() + "\n"
                + "Expires:\t" + this.getExpDate();
    }

}
