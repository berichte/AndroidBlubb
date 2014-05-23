package com.blubb.alubb.basics;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * A BlubbUser will be the author of Blubb Massages.
 */
public interface BlubbUser {
    /** The firstname of the user */
    String getFirstName();
    /** The lastname of the user */
    String getLastName();
    /** If the user has a nickname it's this. */
    String getNickname();
    /** The role of this User within Blubb. */
    BlubbUserRole getRole();
}
