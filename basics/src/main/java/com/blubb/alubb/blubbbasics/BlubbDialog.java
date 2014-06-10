package com.blubb.alubb.blubbbasics;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blubb.alubb.R;

/**
 * Created by Benjamin Richter on 06.06.2014.
 */
public class BlubbDialog extends Dialog {
    private static final String NAME = "BlubbDialog";

    /**
     * Create a Dialog window that uses the default dialog frame style.
     *
     * @param context The Context the Dialog is to run it.  In particular, it
     *                uses the window manager and theme in this context to
     *                present its UI.
     */
    public BlubbDialog(Context context) {
        super(context);
    }

    /**
     * Create a Dialog window that uses a custom dialog style.
     *
     * @param context The Context in which the Dialog should run. In particular, it
     *                uses the window manager and theme from this context to
     *                present its UI.
     * @param theme   A style resource describing the theme to use for the
     *                window. See <a href="{@docRoot}guide/topics/resources/available-resources.html#stylesandthemes">Style
     *                and Theme Resources</a> for more information about defining and using
     *                styles.  This theme is applied on top of the current theme in
     */
    public BlubbDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BlubbDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

}
