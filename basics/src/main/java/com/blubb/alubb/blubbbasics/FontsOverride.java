package com.blubb.alubb.blubbbasics;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Handy class to override the default font of android.
 * <p/>
 * Based on: http://stackoverflow.com/a/16883281/3742306
 * From: weston -> http://stackexchange.com/users/148440/weston?tab=accounts
 * <p/>
 * Created by Benjamin Richter
 */
public final class FontsOverride {

    /**
     * Set the default font for the android application.
     *
     * @param context                 The context from the app.
     * @param staticTypefaceFieldName Field name of the typeface that will be replaced,
     *                                e.g. 'MONOSPACE'.
     * @param fontAssetName           Name of the font in the assets folder.
     */
    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    /**
     * Replaces a font from the system with another font.
     *
     * @param staticTypefaceFieldName Name of the font that will be replaced.
     * @param newTypeface             Font that will replace the other font.
     */
    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field StaticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            StaticField.setAccessible(true);
            StaticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}