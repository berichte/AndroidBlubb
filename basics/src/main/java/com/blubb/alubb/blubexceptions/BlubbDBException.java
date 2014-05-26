package com.blubb.alubb.blubexceptions;

/**
 * Created by Benjamin Richter on 17.05.2014.
 * Exception if there is some error response
 * from the BlubbDB.(BEAP-DB).
 */
public class BlubbDBException extends Exception {
    public BlubbDBException(String message) {
        super(message);
    }

}
