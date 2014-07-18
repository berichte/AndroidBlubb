package com.blubb.alubb.basics;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents all the data for the session info.
 * Created by Benjamin Richter on 22.05.2014.
 */
public class SessionInfo {

    /**
     * Id string for this session.
     */
    private String sessionId;
    /**
     * Id of the user who ows the session.
     */
    private String blubbUser;
    /**
     * Role of the user: admin, PL or user.
     */
    private BlubbUserRole role;
    /**
     * Boolean value either the session is active.
     */
    private boolean sessionActive;
    /**
     * Date when the session will expire.
     */
    private String expDate;

    /**
     * Constructor for the SessionInfo.
     *
     * @param jsonObject from beap, with all data needed to create a SessionInfo-Object.
     *                   sessInfo : {
     *                   sessId : <MD5-string>,
     *                   sessUser : <string>,
     *                   sessRole: <string>,
     *                   sessActive : <bool>,
     *                   expires: <GMT-Date>   lässt sich auch als ISO-Date ausgeben
     *                   }
     * @throws JSONException
     */
    public SessionInfo(JSONObject jsonObject)
            throws JSONException {
        this.sessionId = jsonObject.getString(
                "sessId");
        this.blubbUser = jsonObject.getString(
                "sessUser");
        this.sessionActive = jsonObject.getBoolean(
                "sessActive");
        this.expDate = jsonObject.getString(
                "expires");
        this.role = getUserRoleViaString(jsonObject.getString(
                "sessRole"));
    }

    /**
     * Constructor for a not valid SessionInfo.
     */
    public SessionInfo() {
        this.sessionId = "noSessionId";
        this.blubbUser = "noBlubbUser";
        this.sessionActive = false;
        this.expDate = "";
        this.role = BlubbUserRole.BLUBB_USER;
    }

    /**
     * Parses the json string of a user role to a BlubbUserRole object.
     *
     * @param role json string representing the role of a user.
     * @return a BlubbUserRole user, admin or PL = project leader/manager.
     */
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

    /**
     * @return the sessionId of this SessionInfo
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return id of the user who owns this session.
     */
    public String getBlubbUser() {
        return blubbUser;
    }

    /**
     * @return BlubbUserRole of the user of this session.
     */
    public BlubbUserRole getRole() {
        return role;
    }

    /**
     * @return true if the session is active.
     */
    public boolean isSessionActive() {
        return sessionActive;
    }

    /**
     * @return the date when this session will expire at the beap database.
     */
    public String getExpDate() {
        return expDate;
    }

    /**
     * @return a string showing all data of this SessionInfo object.
     */
    public String toString() {
        return "SessionInfo:\n"
                + "SessionId:\t" + this.sessionId + "\n"
                + "User:\t" + this.blubbUser + "\n"
                + "Role:\t" + this.role + "\n"
                + "Active:\t" + this.isSessionActive() + "\n"
                + "Expires:\t" + this.getExpDate();
    }

}
