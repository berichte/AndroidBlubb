package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.basics.SessionManager;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;

public class Blubb_login extends Activity {

    public static final String  USERNAME_PREFAB = "username_prefab",
                                PASSWORD_PREFAB = "password_prefab";
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
                String unS = un.getText().toString(),
                        pwS = pw.getText().toString();
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(Blubb_login.this);
                sharedPreferences.edit().putString(
                        Blubb_login.this.getString(R.string.pref_username), unS);
                sharedPreferences.edit().putString(
                        Blubb_login.this.getString(R.string.pref_password), pwS);
                login(unS, pwS);
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
                    return SessionManager.getInstance().getSessionID(Blubb_login.this);
                }
            } catch (InvalidParameterException e) {
                return e.getMessage();
            } catch (BlubbDBException e) {
                return e.getMessage();
            } catch (SessionException e) {
                e.printStackTrace();
            }
            return "no Results! :(";
        }

        @Override
        protected void onPostExecute(String response) {
            Intent intent = new Intent(Blubb_login.this, ThreadOverview.class);
            Blubb_login.this.startActivity(intent);
        }
    }
}
