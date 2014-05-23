package com.blubb.alubb.blubexceptions;

import android.content.res.Resources;

import com.blubb.alubb.blubbbasics.R;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Prototype for all blubb exceptions
 * gets the standard message from ExceptionMessages.xml
 */
public class BlubbException extends Exception {
    protected static int messageID =  R.string.BlubbException;

    public BlubbException() {
        super(Resources.getSystem().getString(messageID));
    }
    public BlubbException(String message) {
        super(message);
    }
}
