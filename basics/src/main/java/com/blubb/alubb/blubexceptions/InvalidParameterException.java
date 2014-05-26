package com.blubb.alubb.blubexceptions;

import com.blubb.alubb.R;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Given parameter is not valid.
 */
public class InvalidParameterException extends BlubbException {
    protected static int messageID = R.string.InvalidParameterException;

    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(String message) {
        super(message);
    }
}
