package com.blubb.alubb.blubexceptions;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Exception thrown if there are connection problems to the blubbDB.
 */
public class BlubbDBConnectionException extends Exception {

    /**
     * Constructs a BlubbDBConnectionException containing the message.
     *
     * @param message Message for the exception.
     */
    public BlubbDBConnectionException(String message) {
        super(message);
    }
}
