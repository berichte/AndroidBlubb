package com.blubb.alubb.blubbbasics;

import android.app.Application;
import android.graphics.Typeface;
import android.widget.TextView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.MessageManager;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.basics.ThreadManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.BlubbNullException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

/**
 * Custom application class, mainly to enable the singleton pattern for SessionManager,
 * ThreadManager and MessageManager. These need to be instantiated the first time from the
 * Application to be available from all Contexts.
 * This although provides a central exception handling and a function to set the layout font, e.g.
 * to the beapIconic font do show icons from it.
 * <p/>
 * Created by Benjamin Richter on 03.06.2014.
 */
public class BlubbApplication extends Application {

    /**
     * Instance of the SessionManager to ensure it will not be a victim to the garbage collector.
     */
    SessionManager sessionManager;
    /**
     * Instance of the ThreadManager to ensure it will not be a victim to the garbage collector.
     */
    ThreadManager threadManager;
    /**
     * Instance of the MessageManager to ensure it will not be a victim to the garbage collector.
     */
    MessageManager messageManager;

    /**
     * Set a layout font to some TextViews.
     *
     * @param tf     The typeface for the font.
     * @param params TextViews the typeface will be set to.
     */
    public static void setLayoutFont(Typeface tf, TextView... params) {
        for (TextView tv : params) {
            tv.setTypeface(tf);
        }
    }

    /**
     * Initializes the singletons SessionManager, ThreadManager and MessageManager and sets the
     * font for the whole application to Exo2.0-Regular.otf.
     */
    public void onCreate() {
        super.onCreate();
        String fontName = getString(R.string.application_font);
        FontsOverride.setDefaultFont(this, "MONOSPACE", fontName);
        initSingletons();
    }

    /**
     * Initializes the singletons SessionManager, ThreadManager and MessageManager.
     */
    protected void initSingletons() {
        this.sessionManager = SessionManager.getInstance();
        this.threadManager = ThreadManager.getInstance();
        this.messageManager = MessageManager.getInstance();
    }

    /**
     * Handles the different exceptions that occur at the execution of the app and toasts a
     * message for the user according to the type of Exception.
     *
     * @param e Exception that will be handled, only if it's not null:
     */
    public void handleException(Exception e) {
        if (e != null) {
            Class exClass = e.getClass();
            String toastMessage;
            if (exClass.equals(BlubbDBConnectionException.class)) {
                toastMessage = this.getResources().getString(
                        R.string.blubb_db_connection_exception_message);
            } else if (exClass.equals(BlubbDBException.class)) {
                toastMessage = this.getResources().getString(
                        R.string.blubb_db_exception_message);
            } else if (exClass.equals(BlubbNullException.class)) {
                toastMessage = this.getResources().getString(
                        R.string.blubb_null_exception_message);
            } else if (exClass.equals(InvalidParameterException.class)) {
                toastMessage = this.getResources().getString(
                        R.string.invalid_parameter_exception_message);
            } else if (exClass.equals(PasswordInitException.class)) {
                toastMessage = this.getResources().getString(
                        R.string.password_init_exception_message);
            } else if (exClass.equals(SessionException.class)) {
                toastMessage = this.getResources().getString(
                        R.string.session_exception_message);
            } else {
                toastMessage = this.getResources().getString(
                        R.string.unknown_exception_message);
            }
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }
}
