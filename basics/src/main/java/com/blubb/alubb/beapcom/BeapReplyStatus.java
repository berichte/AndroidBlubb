package com.blubb.alubb.beapcom;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * enum for the diffent kinds of DB Replies.
 */
public enum BeapReplyStatus {
    /** 200 - all right reply. */
    OK,
    /** 203 - request was ok but is empty. */
    NO_CONTENT,
    /** 204 - in case of a double logout. */
    SESSION_ALREADY_DELETED,
    /** 400 - mostly because of missing parameter. */
    REQUEST_FAILURE,
    /** 401 - no login or session expired.*/
    LOGIN_REQUIRED,
    /** 403 - session is not valid.*/
    PERMISSION_DENIED,
    /** 407 - no connection :( */
    CONNECTION_ERROR,
    /** 406 & 409 - missing or wrong parameter*/
    PARAMETER_ERROR,
    /** 418 - syntax or reference error - at query string.*/
    SYNTAX_ERROR,
    /** - unknown status */
    UNKNOWN_STATUS
}