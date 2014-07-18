package com.blubb.alubb.basics;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Different Roles for the different kinds of
 * users with different permissions, e.g. could a Blubb admin change a inappropriate message.
 */
public enum BlubbUserRole {
    /**
     * Standard user of blubb without special permissions.
     */
    BLUBB_USER,
    /**
     * Blubb administrator.
     */
    BLUBB_ADMIN,
    /**
     * Manager of a project.
     */
    BLUBB_MANAGER,
    /**
     * undefined if there is no role
     */
    UNDEFINED

}
