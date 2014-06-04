package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

public class Blubb_login extends Activity {

    public static final String  USERNAME_PREFAB = "username_prefab",
                                PASSWORD_PREFAB = "password_prefab";
    private String username, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blubb_login);

        addLoginButtonListener();
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
        String[] params = new String[2];
        params[0] = username;
        params[1] = password;
        new AsyncLogin().execute(params);
    }

    public class AsyncLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String username = params[0],
                    password = params[1];
            try {
                if (getApp().getSessionManager().login(username, password)){

                    return getApp().getSessionManager()
                            .getSessionID(Blubb_login.this.getApplicationContext());
                }
            } catch (InvalidParameterException e) {
                return e.getMessage();
            } catch (BlubbDBException e) {
                return e.getMessage();
            } catch (SessionException e) {
                return e.getMessage();
            } catch (BlubbDBConnectionException e) {
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if(response != null) {
                CheckBox stayLogged = (CheckBox) findViewById(R.id.login_stay_logged_cb);
                if(stayLogged.isChecked()) {
                    Log.i("Login", "User wants to stay logged in. Editing savedPrefs.");
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(
                                    Blubb_login.this.getApplicationContext());
                    sharedPreferences.edit().putString(
                            Blubb_login.this.getString(R.string.pref_username), username);
                    sharedPreferences.edit().putString(
                            Blubb_login.this.getString(R.string.pref_password), password);
                    sharedPreferences.edit().commit();
                }

                Intent intent = new Intent(Blubb_login.this, ThreadOverview.class);
                Blubb_login.this.startActivity(intent);
            }
        }
    }
}
