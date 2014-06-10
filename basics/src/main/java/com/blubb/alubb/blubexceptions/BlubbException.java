package com.blubb.alubb.blubexceptions;

import android.content.res.Resources;

import com.blubb.alubb.R;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Prototype for all blubb exceptions
 * gets the standard message from ExceptionMessages.xml
 */
public class BlubbException extends Exception {

    public BlubbException() {
        super();
    }
    public BlubbException(String message) {
        super(message);
    }
}
