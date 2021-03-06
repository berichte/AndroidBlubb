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
import java.util.Map;
import java.util.Set;

/**
 * BPC stands for Blubb Parameter Checker
 * This is a collection of functions for checking and parsing string and date parameter
 * from and to the beapDB.
 * <p/>
 * Created by Benjamin Richter on 17.05.2014.
 */
public class BPC {
    /**
     * For undefined String values like "" or null.
     */
    public static final String UNDEFINED = "undefined";
    /**
     * For the url encoding.
     */
    public static final String ENCODING = "UTF-8";
    /**
     * Pattern for date format, 2014-06-25T13:40:40.312Z
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    /**
     * Simple date format to encode and decode a Date object.
     */
    private static DateFormat df = new SimpleDateFormat(DATE_PATTERN);
    /**
     * Name for Logging purposes.
     */
    private static final String NAME = "BPC";
    /**
     * Map for escape character which need to be escaped for the DB.
     * Key is the character from/for Android value from/for the DB.
     */
    private static final Map<String, String> ESC_MAP;

    /** Function to fill the ESC_MAP statically. */
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
     * @param toCheck String parameter which needs to be checked.
     * @throws com.blubb.alubb.blubexceptions.InvalidParameterException if toCheck is null or empty.
     */
    public static void checkString(String toCheck) throws InvalidParameterException {
        checkForNull(toCheck);
        if (toCheck.equals("")) throw new InvalidParameterException();

    }

    /**
     * Checks whether an object is initialized and throws an exception if it is null.
     *
     * @param toCheck Object to check for null.
     * @throws BlubbNullException Thrown if toCheck == null.
     */
    public static void checkForNull(Object toCheck) throws BlubbNullException {
        if (toCheck == null) throw new BlubbNullException();
    }

    /**
     * Function to parse Escape character to the DB.
     *
     * @param parameter String from Android.
     * @return String for the beapDB.
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

    /**
     * Executes a url encoding for a string with utf-8.
     *
     * @param s String to encode.
     * @return encoded string s.
     */
    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.e(NAME, e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Parses Escape character from beapDB to Android.
     *
     * @param s String from beapDB.
     * @return String for Android.
     */
    public static String parseStringFromDB(String s) {
        Set<Map.Entry<String, String>> entries = ESC_MAP.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (s.contains(entry.getValue())) {
                s = s.replace(entry.getValue(), entry.getKey());
            }
        }
        return s;
    }

    /**
     * Finds a given key string within a json object and returns the value for this.
     *
     * @param jsonObject Json object within which will be searched for a key.
     * @param key        Key for the value.
     * @return The value from the json object for the given key or 'undefined' if key is not found.
     */
    public static String findStringInJsonObj(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) try {
            return parseStringFromDB(jsonObject.getString(key));
        } catch (JSONException e) {
            return UNDEFINED;
        }
        return UNDEFINED;
    }

    /**
     * Parse a date string of format '2014-06-25T13:40:40.312Z' to a Date object.
     *
     * @param dateString String with the date.
     * @return Date object created from dateString.
     * @throws ParseException If dateString has the wrong format.
     */
    public static Date parseDate(String dateString) throws ParseException {
        return df.parse(dateString);
    }

    /**
     * Parse a Date object to a string with the format: '2014-06-25T13:40:40.312Z'.
     *
     * @param date Date object.
     * @return String showing the Date.
     */
    public static String parseDate(Date date) {
        return df.format(date);
    }
}
