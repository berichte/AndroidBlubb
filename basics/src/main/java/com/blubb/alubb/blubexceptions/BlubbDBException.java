package com.blubb.alubb.blubexceptions;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Exception thrown if there is some error response
 * from the beapDB.
 */
public class BlubbDBException extends Exception {

    /**
     * Constructs a BlubbDBException containing the message.
     *
     * @param message Message for the exception.
     */
    public BlubbDBException(String message) {
        super(message);
    }

}
