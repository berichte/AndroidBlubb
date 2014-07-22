package com.blubb.alubb.basics;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Simple text content for a blubb message.
 * <p/>
 * Created by Benjamin Richter on 10.07.2014.
 */
public class TextContent implements MContent {
    /**
     * Name for logging purposes.
     */
    private static final String NAME = "TextContent";

    /**
     * The contents text.
     */
    private String contentText;

    /**
     * TextView displaying the contentText
     */
    private View contentView;

    public TextContent(String contentText) {
        this.contentText = contentText;
    }

    /**
     * Returns the view which displays the content of a message.
     *
     * @param context Context of the View
     * @return TextView showing the String of the message content.
     */
    @Override
    public View getContentView(Context context) {
        if (this.contentView != null) return contentView;
        TextView textView = new TextView(context);
        textView.setText(contentText);
        textView.setTextSize(18);
        this.contentView = textView;
        return this.contentView;
    }

    @Override
    public String getStringRepresentation() {
        return this.contentText;
    }

    @Override
    public TextContent createFromStringRepresentation(String stringRepresentation) {
        return new TextContent(stringRepresentation);
    }
}
