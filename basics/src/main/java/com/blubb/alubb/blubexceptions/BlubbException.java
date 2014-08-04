package com.blubb.alubb.blubexceptions;

/**
 * Basic type for all exceptions from blubb.
 * <p/>
 * Created by Benjamin Richter on 24.07.2014.
 */
public class BlubbException extends Exception {
    /**
     * Constructs a BlubbDBConnectionException containing the message.
     *
     * @param message Message for the exception.
     */
    public BlubbException(String message) {
        super(message);
    }

    public BlubbException() {
        super();
    }
}
