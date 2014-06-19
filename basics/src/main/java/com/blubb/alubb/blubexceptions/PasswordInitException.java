package com.blubb.alubb.blubexceptions;

/**
 * Created by Benjamin Richter on 19.06.2014.
 */
public class PasswordInitException extends BlubbException {
    private static final String NAME = "PasswordInitException";

    public PasswordInitException() {
        super(NAME);
    }

    public PasswordInitException(String message) {
        super(message);
    }
}
