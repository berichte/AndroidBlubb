package com.blubb.alubb.blubexceptions;

/**
 * The SessionException is thrown if the actual session is not valid any more or no valid
 * login parameter available.
 * <p/>
 * Created by Benjamin Richter on 02.06.2014.
 */
public class SessionException extends BlubbException {

    /**
     * Constructs a SessionException with a message.
     *
     * @param message Message for the SessionException.
     */
    public SessionException(String message) {
        super(message);
    }
}
