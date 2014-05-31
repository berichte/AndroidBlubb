package com.blubb.alubb.basics;

import android.util.Log;

import com.blubb.alubb.blubexceptions.BlubbNullException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** BPC stands for Blubb Parameter Checker
 * Created by Benjamin Richter on 17.05.2014.
 */
public class BPC {
    private static final String UNDEFINED = "undefined";

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


    /**
     * Map for escape character which need to be escaped for the DB.
     * Key is the character from/for Android value from/for the DB.
     */

    private static final Map<String, String> ESC_MAP;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("\n", "\\\n");
        aMap.put("\t", "\\\t");
        ESC_MAP = Collections.unmodifiableMap(aMap);
    }

    /**
     * Function to parse Escape character to the DB.
     * @param parameter String from Android
     * @return String for the BEAP-DB
     * @throws InvalidParameterException if s is empty or null.
     */
    public static String parseStringToDB(String parameter) throws InvalidParameterException {
        checkString(parameter);
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(parameter);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '\n') {
                result.append("\\n");
            } else if (character == '\t') {
                result.append("\\t");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    /**
     * Function to parse Escape character from the DB to Android
     * @param s String from BEAP-DB
     * @return String for Android.
     */
    public static String parseStringFromDB(String s){
        Set<Map.Entry<String, String>> entries = ESC_MAP.entrySet();
        for (Iterator<Map.Entry<String, String>> it = entries.iterator(); it.hasNext();){
            Map.Entry<String, String> entry = it.next();
            if(s.contains(entry.getValue())) {
                Log.i("BPC-parse to DB", "parsing: " + entry.getValue() + " -> " + entry.getKey());
                s.replace(entry.getValue(), entry.getKey());
            }
        }
        return s;
    }

    public static String findStringInJsonObj(JSONObject obj, String toFind) {
        if(obj.has(toFind)) try {
            return parseStringFromDB(obj.getString(toFind));
        } catch (JSONException e) {
            return UNDEFINED;
        }
        return UNDEFINED;
    }
}
