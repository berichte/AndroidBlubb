package com.blubb.alubb.blubbbasics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.DatabaseHandler;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.blubexceptions.PasswordInitException;

/**
 * Activity that displays the login screen, the initialize password and the reset password dialog.
 * <p/>
 * Created by Benjamin Richter
 */
public class ActivityLogin extends Activity {

    /**
     * Name for Logging purposes.
     */
    public static final String NAME = "BlubbLoginActivity";

    /**
     * Name for the extra of the intent for decide witch login screen should be shown,
     * either the regular login with username and password or the extendet to reset the pw.
     */
    public static final String EXTRA_LOGIN_TYPE = "loginType";

    /**
     * Username and password preferences key.
     */
    public static final String USERNAME_PREFAB = "username_prefab",
            PASSWORD_PREFAB = "password_prefab";
    /**
     * Dialog shown if the user needs to initialize the password.
     */
    private Dialog initDialog;
    /**
     * The login type for this login screen.
     */
    private LoginType loginType;
    /**
     * Temporarily held Strings of the username and password.
     */
    private String username, password;

    /**
     * Set the content view, get the login type from the intend and set the LoginType field.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        Intent intent = getIntent();
        LoginType loginType = (LoginType) intent.getSerializableExtra(EXTRA_LOGIN_TYPE);
        if (loginType != null) this.loginType = loginType;
        else this.loginType = LoginType.LOGIN;
    }

    /**
     * Fill in the EditTexts the username and the password from the preferences.
     *
     * @param unEt EditText for the username.
     * @param pwEt EditText for the password.
     */
    private void fillInCredentialPrefs(EditText unEt, EditText pwEt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String un = prefs.getString(getString(R.string.pref_username), "");
        String pw = prefs.getString(getString(R.string.pref_password), "");
        unEt.setText(un);
        pwEt.setText(pw);
        if (pw.equals("")) pwEt.requestFocus();
    }

    /**
     * Show the login screen according to the LoginType, either the simple login or
     * the password reset.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Button button = (Button) findViewById(R.id.login_activity_login_btn);
        final EditText resetPw = (EditText) findViewById(R.id.login_activity_password_reset_et),
                confirmPw = (EditText) findViewById(R.id.login_activity_password_reset_confirm_et),
                username = (EditText) findViewById(R.id.login_activity_username_et),
                password = (EditText) findViewById(R.id.login_activity_password_et);
        CheckBox stayloggedIn = (CheckBox) findViewById(R.id.login_activity_stayloggedin_cb);
        fillInCredentialPrefs(username, password);
        switch (loginType) {
            case RESET:
                button.setText(getString(R.string.login_activity_reset_btn_text));
                resetPw.setVisibility(View.VISIBLE);
                confirmPw.setVisibility(View.VISIBLE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String un = username.getText().toString(),
                                pw = password.getText().toString(),
                                rpw = resetPw.getText().toString(),
                                cpw = confirmPw.getText().toString();
                        new AsyncResetPassword().execute(un, pw, rpw, cpw);
                        spinnerOn();
                    }
                });
                stayloggedIn.setVisibility(View.INVISIBLE);
                break;
            case LOGIN:
                button.setText(getString(R.string.login_activity_sign_in_btn_text));
                resetPw.setVisibility(View.INVISIBLE);
                confirmPw.setVisibility(View.INVISIBLE);
                addLoginButtonListener();
                stayloggedIn.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Get the custom BlubbApplication, e.g. to handle exceptions.
     *
     * @return The BlubbApplication.
     */
    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    /**
     * Add the ButtonListener for the login button, which performs a login with
     * the username and password in the EditTexts if clicked.
     */
    private void addLoginButtonListener() {
        Button loginButton = (Button) findViewById(R.id.login_activity_login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText un = (EditText) findViewById(R.id.login_activity_username_et);
                EditText pw = (EditText) findViewById(R.id.login_activity_password_et);

                username = un.getText().toString();
                password = pw.getText().toString();
                Log.i("Login", "LoginButton clicked. Username: " + username);
                login(username, password);
            }
        });
    }

    /**
     * Execute a login via a AsyncLogin. If the username has changed the sqlite
     * database will be deleted, though a user can only see the threads and messages
     * that he needs.
     *
     * @param username String containing the username, if this is different from the
     *                 username of the prefabs the database will be reset for the new user.
     * @param password String containing the password.
     */
    private void login(String username, String password) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                this.getApplicationContext());
        String prefName = this.getString(R.string.pref_username);
        prefName = prefs.getString(prefName, "NULL");
        if (!username.equals(prefName) && !prefName.equals("NULL")) {
            DatabaseHandler.deleteDatabase(this);
        }
        String[] params = new String[2];
        params[0] = username;
        params[1] = password;

        new AsyncLogin().execute(params);
        spinnerOn();
    }

    /**
     * Show the dialog for the password initialisation.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showPasswordInitDialog() {
        initDialog = new Dialog(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.password_init_dialog, null);
        initDialog.setContentView(dialogLayout);
        initDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        assert dialogLayout != null;
        final EditText username = (EditText) dialogLayout.findViewById(
                R.id.password_init_dialog_username_te),
                oldPassword = (EditText) dialogLayout.findViewById(
                        R.id.password_init_dialog_old_password_et),
                newPassword = (EditText) dialogLayout.findViewById(
                        R.id.password_init_dialog_new_password_et),
                confirmPassword = (EditText) dialogLayout.findViewById(
                        R.id.password_init_dialog_confirm_password_et);

        username.setText(ActivityLogin.this.username);
        oldPassword.setText(ActivityLogin.this.password);
        final Button yBtn;
        yBtn = (Button) dialogLayout.findViewById(
                R.id.password_init_dialog_y_btn);
        Button xBtn = (Button) dialogLayout.findViewById(
                R.id.password_init_dialog_x_btn);

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                    yBtn.setEnabled(true);
                } else {
                    yBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                    yBtn.setEnabled(true);
                } else {
                    yBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Typeface tf = Typeface.createFromAsset(this.getAssets(), "BeapIconic.ttf");
        BlubbApplication.setLayoutFont(tf, yBtn);
        BlubbApplication.setLayoutFont(tf, xBtn);

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncResetPassword asyncResetPassword = new AsyncResetPassword();
                String un = username.getText().toString(),
                        oldPw = oldPassword.getText().toString(),
                        newPw = newPassword.getText().toString(),
                        conPw = confirmPassword.getText().toString();
                asyncResetPassword.execute(un, oldPw, newPw, conPw);
                spinnerOn();
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDialog.cancel();
            }
        });
        confirmPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String newPW = newPassword.getText().toString(),
                        confirmPW = confirmPassword.getText().toString();
                if (confirmPW.equals(newPW)) {
                    yBtn.setEnabled(true);
                } else {
                    yBtn.setEnabled(false);
                }
                return false;
            }
        });
        initDialog.show();
    }

    /**
     * Activates the spinner indicating that messages are loading from  the beapDB.
     */
    public void spinnerOn() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.login_activity_pb);
        pb.setVisibility(View.VISIBLE);
    }

    /**
     * Deactivates the spinner indicating that messages are loading from  the beapDB.
     */
    public void spinnerOff() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.login_activity_pb);
        pb.setVisibility(View.INVISIBLE);
    }

    /**
     * Enumeration for the different kinds of logins, e.g. regular login and password reset.
     */
    public enum LoginType {
        LOGIN, RESET
    }

    /**
     * AsyncTask to perform a login. The username must be the first and the password the second
     * parameter.
     */
    public class AsyncLogin extends AsyncTask<String, Void, String> {

        PasswordInitException passwordInitException;
        Exception e;

        @Override
        protected String doInBackground(String... params) {
            String username = params[0],
                    password = params[1];
            try {
                if (SessionManager.getInstance().login(username, password)) {
                    return SessionManager.getInstance()
                            .getSessionID(ActivityLogin.this.getApplicationContext());
                }
            } catch (PasswordInitException e) {
                this.passwordInitException = e;
            } catch (Exception e) {
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            getApp().handleException(e);
            spinnerOff();
            if (passwordInitException != null) {
                showPasswordInitDialog();
                return;
            }

            if (response != null) {
                CheckBox stayLogged = (CheckBox) findViewById(R.id.login_activity_stayloggedin_cb);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(
                                ActivityLogin.this.getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String unPref = "", pwPref = "";
                if (stayLogged.isChecked()) {
                    Log.i("Login", "User wants to stay logged in. Editing savedPrefs.\n" +
                            "saving: " + username + " as Username");
                    unPref = username;
                    pwPref = password;
                }
                editor.putString(
                        ActivityLogin.this.getString(R.string.pref_username), unPref);
                editor.putString(
                        ActivityLogin.this.getString(R.string.pref_password), pwPref);
                editor.commit();
                onBackPressed();
            }
        }
    }

    /**
     * AsyncTask to perform a password reset.
     * The username must be the first, the old password the second, the new password
     * the third and the fourth parameter must be the verification of the new password.
     */
    private class AsyncResetPassword extends AsyncTask<String, Void, Boolean> {
        private Exception e;

        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length != 4) return false;
            String un = params[0],
                    oldPw = params[1],
                    newPw = params[2],
                    conPw = params[3];
            try {
                return SessionManager.getInstance().resetPassword(un, oldPw, newPw, conPw);
            } catch (Exception e) {
                this.e = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean response) {
            getApp().handleException(e);
            spinnerOff();
            if (response) {
                String toastText = getString(R.string.login_pw_reset_ok_toast);
                Toast.makeText(ActivityLogin.this, toastText, Toast.LENGTH_SHORT).show();
                loginType = LoginType.LOGIN;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                        ActivityLogin.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(USERNAME_PREFAB, "");
                editor.putString(PASSWORD_PREFAB, "");
                editor.commit();
                if (initDialog != null) {
                    initDialog.cancel();
                    initDialog = null;
                }
            } else {
                Toast.makeText(ActivityLogin.this,
                        getResources().getString(R.string.unknown_exception_message),
                        Toast.LENGTH_LONG).show();
                loginType = LoginType.RESET;
            }
            ActivityLogin.this.onResume();
        }
    }
}


