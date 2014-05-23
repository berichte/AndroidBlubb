package com.blubb.alubb.basics;

import com.blubb.alubb.blubexceptions.BlubbNullException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

/**
 * Created by Benjamin Richter on 17.05.2014.
 */
public class BlubbParameterChecker {

    /**
     * Checks whether a string is null or empty and throws an Exception if though.
     * @param toCheck
     * @throws com.blubb.alubb.blubexceptions.InvalidParameterException if toCheck is null or empty.
     */
    public static void checkString(String toCheck) throws InvalidParameterException {
        checkForNull(toCheck);
        if(toCheck.equals("")) throw new InvalidParameterException();

    }

    public static void checkForNull(Object toCheck) throws BlubbNullException {
        if (toCheck == null) throw new BlubbNullException();
    }
}
