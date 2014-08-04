package com.blubb.alubb.blubexceptions;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * <p/>
 * Exception thrown if a parameter is not valid.
 */
public class InvalidParameterException extends BlubbException {

    private static final String EXCEPTION_MESSAGE = "The given Parameter is not valid.";

    /**
     * Constructor for the InvalidParameterException.
     */
    public InvalidParameterException() {
        super(EXCEPTION_MESSAGE);
    }

}
