package com.blubb.alubb.basics;

import android.content.Context;
import android.view.View;

/**
 * Interface to provide a blubb message with different contents.
 * <p/>
 * Created by Benjamin Richter on 10.07.2014.
 */
public interface MContent {

    /**
     * View which displays the content of a message.
     *
     * @param context Context of the View
     * @return View to display the content.
     */
    public View getContentView(Context context);

    /**
     * Returns a string representation of the content.
     *
     * @return Representation of this content as a string.
     */
    public String getStringRepresentation();

    /**
     * Returns a new MContent Object due to the representation
     *
     * @param stringRepresentation a string representing a MContent, e.g. a TextContent.
     * @return the created MContent-Object.
     */
    @SuppressWarnings("UnusedDeclaration")
    public MContent createFromStringRepresentation(String stringRepresentation);
}
