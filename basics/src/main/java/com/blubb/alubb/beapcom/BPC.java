package com.blubb.alubb.beapcom;

import android.util.Log;

import com.blubb.alubb.blubexceptions.BlubbNullException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * BPC stands for Blubb Parameter Checker
 * This is a collection of functions for checking and parsing string and date parameter
 * from and to the beapDB.
 *
 * Created by Benjamin Richter on 17.05.2014.
 */
public class BPC {

    public static final String UNDEFINED = "undefined";
    public static final String ENCODING = "UTF-8";
    //2014-06-25T13:40:40.312Z
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static DateFormat df = new SimpleDateFormat(DATE_PATTERN);
    /**
     * Name for Logging purposes
     */
    private static final String NAME = "BPC";
    /**
     * Map for escape character which need to be escaped for the DB.
     * Key is the character from/for Android value from/for the DB.
     */

    private static final Map<String, String> ESC_MAP;

    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("\n", "\\\n");
        aMap.put("\t", "\\\t");
        aMap.put("\"", "\\\"");
        ESC_MAP = Collections.unmodifiableMap(aMap);
    }

    /**
     * Checks whether a string is null or empty and throws an Exception if though.
     *
     * @param toCheck
     * @throws com.blubb.alubb.blubexceptions.InvalidParameterException if toCheck is null or empty.
     */
    public static void checkString(String toCheck) throws InvalidParameterException {
        checkForNull(toCheck);
        if (toCheck.equals("")) throw new InvalidParameterException();

    }

    public static void checkForNull(Object toCheck) throws BlubbNullException {
        if (toCheck == null) throw new BlubbNullException();
    }

    /**
     * Function to parse Escape character to the DB.
     *
     * @param parameter String from Android
     * @return String for the BEAP-DB
     */
    public static String parseStringParameterToDB(String parameter) {
        try {
            checkString(parameter);
        } catch (InvalidParameterException e) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(parameter);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '\n') {
                result.append("\\n");
            } else if (character == '\t') {
                result.append("\\t");
            } else if (character == '\"') {
                result.append("\\\"");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return encode("\"" + result.toString() + "\"");
    }

    private static String encode(String s) {
        try {
            String enc = URLEncoder.encode(s, ENCODING);
            return enc;
        } catch (UnsupportedEncodingException e) {
            Log.e(NAME, e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Function to parse Escape character from the DB to Android
     *
     * @param s String from BEAP-DB
     * @return String for Android.
     */
    public static String parseStringFromDB(String s) {
        Set<Map.Entry<String, String>> entries = ESC_MAP.entrySet();
        for (Iterator<Map.Entry<String, String>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            if (s.contains(entry.getValue())) {
                s.replace(entry.getValue(), entry.getKey());
            }
        }
        return s;
    }

    public static String findStringInJsonObj(JSONObject obj, String toFind) {
        if (obj.has(toFind)) try {
            return parseStringFromDB(obj.getString(toFind));
        } catch (JSONException e) {
            return UNDEFINED;
        }
        return UNDEFINED;
    }

    //2014-06-25T13:40:40.312Z
    public static Date parseDate(String dateString) throws ParseException {
        return df.parse(dateString);
    }

    public static String parseDate(Date date) {
        return df.format(date);
    }
}
