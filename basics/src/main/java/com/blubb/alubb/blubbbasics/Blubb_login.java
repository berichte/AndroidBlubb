package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.beapcom.SessionManager;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

public class Blubb_login extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blubb_login);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String un = sharedPreferences.getString("username_prefab", "NULL"),
                pw = sharedPreferences.getString("password_prefab", "NULL");
        if(!un.equals("NULL") && !pw.equals("NULL")) {
            login(un, pw);
        }
        addLoginButtonListener();
    }

    private void addLoginButtonListener() {
        Button testButton = (Button) findViewById(R.id.blubb_login_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText un = (EditText) findViewById(R.id.blubb_login_username);
                EditText pw = (EditText) findViewById(R.id.blubb_login_password);
                login(un.getText().toString(), pw.getText().toString());
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
            BlubbComManager manager = new BlubbComManager();
            try {
                if (manager.login(username, password)){
                    return SessionManager.getInstance().getSession().toString();
                }
            } catch (InvalidParameterException e) {
                return e.getMessage();
            } catch (BlubbDBException e) {
                return e.getMessage();
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