package com.blubb.alubb.blubexceptions;

/**
 * A PasswordInitException is thrown if the blubbReplyStatus of a login is PASSWORD_INIT and
 * the password of the user is still "init" and must be set to a own password.
 * <p/>
 * Created by Benjamin Richter on 19.06.2014.
 */
public class PasswordInitException extends BlubbException {
    private static final String MESSAGE = "Password is still 'init' and must be initialized.";

    /**
     * Constructs a PasswordInitException.
     */
    public PasswordInitException() {
        super(MESSAGE);
    }

}
