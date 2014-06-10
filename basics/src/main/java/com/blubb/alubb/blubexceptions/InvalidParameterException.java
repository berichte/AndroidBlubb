package com.blubb.alubb.blubexceptions;

import com.blubb.alubb.R;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Given parameter is not valid.
 */
public class InvalidParameterException extends BlubbException {

    public InvalidParameterException() {
        super("The given Parameter is not valid.");
    }

    public InvalidParameterException(String message) {
        super(message);
    }
}
