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
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.DatabaseHandler;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.beapcom.BlubbResponse;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.PasswordInitException;
import com.blubb.alubb.blubexceptions.SessionException;

public class ActivityLogin extends Activity {
    public static final String NAME = "BlubbLoginActivity";
    public static final String EXTRA_LOGIN_TYPE = "loginType";
    public static final String USERNAME_PREFAB = "username_prefab",
            PASSWORD_PREFAB = "password_prefab";
    ;
    private LoginType loginType;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blubb_login);
        Intent intent = getIntent();
        LoginType loginType = (LoginType) intent.getSerializableExtra(EXTRA_LOGIN_TYPE);
        if (loginType != null) this.loginType = loginType;
        else this.loginType = LoginType.LOGIN;
    }

    private void fillInCredentialPrefs(EditText unEt, EditText pwEt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String un = prefs.getString(getString(R.string.pref_username), "");
        String pw = prefs.getString(getString(R.string.pref_password), "");
        unEt.setText(un);
        pwEt.setText(pw);
        if (pw.equals("")) pwEt.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button button = (Button) findViewById(R.id.blubb_login_button);
        final EditText resetPw = (EditText) findViewById(R.id.blubb_login_password_reset),
                confirmPw = (EditText) findViewById(R.id.blubb_login_password_reset_confirm),
                username = (EditText) findViewById(R.id.blubb_login_username),
                password = (EditText) findViewById(R.id.blubb_login_password);
        CheckBox stayloggedIn = (CheckBox) findViewById(R.id.login_stay_logged_cb);
        fillInCredentialPrefs(username, password);
        switch (loginType) {
            case RESET:
                button.setText(getString(R.string.blubb_reset_button_text));
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
                    }
                });
                stayloggedIn.setVisibility(View.INVISIBLE);
                break;
            case LOGIN:
                button.setText(getString(R.string.action_sign_in_short));
                resetPw.setVisibility(View.INVISIBLE);
                confirmPw.setVisibility(View.INVISIBLE);
                addLoginButtonListener();
                stayloggedIn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private BlubbApplication getApp() {
        return (BlubbApplication) getApplication();
    }

    private void addLoginButtonListener() {
        Button testButton = (Button) findViewById(R.id.blubb_login_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText un = (EditText) findViewById(R.id.blubb_login_username);
                EditText pw = (EditText) findViewById(R.id.blubb_login_password);

                username = un.getText().toString();
                password = pw.getText().toString();
                Log.i("Login", "LoginButton clicked. Username: " + username);
                login(username, password);
            }
        });
    }

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
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showPasswordInitDialog() {
        final Dialog dialog = new Dialog(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.password_init_dialog, null);
        dialog.setContentView(dialogLayout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View divider = dialog.findViewById(
                dialog.getContext().getResources()
                        .getIdentifier("android:id/titleDivider", null, null)
        );
        divider.setBackground(new ColorDrawable(Color.TRANSPARENT));
        // builder.setView(dialogLayout);

        //builder.setInverseBackgroundForced(true);
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
                R.id.y_button_dialog);
        Button xBtn = (Button) dialogLayout.findViewById(
                R.id.x_button_dialog);

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
            }
        });
        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
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
        dialog.show();
    }

    public enum LoginType {LOGIN, RESET, INIT}

    public class AsyncLogin extends AsyncTask<String, Void, String> {

        PasswordInitException passwordInitException;

        @Override
        protected String doInBackground(String... params) {
            String username = params[0],
                    password = params[1];
            try {
                if (getApp().getSessionManager().login(username, password)) {
                    return getApp().getSessionManager()
                            .getSessionID(ActivityLogin.this.getApplicationContext());
                }
            } catch (BlubbDBException e) {
                return e.getMessage();
            } catch (SessionException e) {
                return e.getMessage();
            } catch (BlubbDBConnectionException e) {
                return e.getMessage();
            } catch (PasswordInitException e) {
                this.passwordInitException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (passwordInitException != null) {
                showPasswordInitDialog();
                return;
            }

            if (response != null) {
                CheckBox stayLogged = (CheckBox) findViewById(R.id.login_stay_logged_cb);
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
/*
                Intent intent = new Intent(ActivityLogin.this, ActivityThreadOverview.class);
                ActivityLogin.this.startActivity(intent);
                finish();*/
            }
        }
    }

    private class AsyncResetPassword extends AsyncTask<String, Void, BlubbResponse> {
        @Override
        protected BlubbResponse doInBackground(String... params) {
            if (params.length != 4) return null;
            String un = params[0],
                    oldPw = params[1],
                    newPw = params[2],
                    conPw = params[3];
            try {
                return SessionManager.getInstance().resetPassword(un, oldPw, newPw, conPw);
            } catch (BlubbDBException e) {
                Log.e(NAME, e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(BlubbResponse response) {
            switch (response.getStatus()) {
                case OK:
                    String toastText = getString(R.string.login_pw_reset_ok_toast);
                    Toast.makeText(ActivityLogin.this, toastText, Toast.LENGTH_SHORT).show();
                    loginType = LoginType.LOGIN;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                            ActivityLogin.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(USERNAME_PREFAB, "");
                    editor.putString(PASSWORD_PREFAB, "");
                    editor.commit();
                    break;
                default:
                    Toast.makeText(ActivityLogin.this,
                            response.getStatusDescr(), Toast.LENGTH_SHORT).show();
                    loginType = LoginType.RESET;
                    break;
            }
            ActivityLogin.this.onResume();
        }
    }


}
