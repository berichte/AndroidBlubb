package com.blubb.alubb.basics;

import android.content.res.Resources;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class RA {
    public static String getString(int id) {
        return Resources.getSystem().getString(id);
    }
}
