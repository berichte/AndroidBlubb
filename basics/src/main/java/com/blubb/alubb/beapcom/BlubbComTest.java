package com.blubb.alubb.beapcom;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

public class BlubbComTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blubb_com_test);

        addLoginButtonListener();
        addGetAllThreadsButtonListener();

    }

    private void addLoginButtonListener() {
        Button testButton = (Button) findViewById(R.id.blubb_com_test_button_login);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] params = new String[2];
                EditText un = (EditText) findViewById(R.id.blubb_com_test_username);
                params[0] = un.getText().toString();
                EditText pw = (EditText) findViewById(R.id.blubb_com_test_password);
                params[1] = pw.getText().toString();
               new AsyncLogin().execute(params);
            }
        });
    }

    private void addGetAllThreadsButtonListener() {
        Button testButton = (Button) findViewById(R.id.blubb_com_test_button_getAllThreads);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncGetAllThreads().execute();
            }
        });
    }

    private class AsyncGetAllThreads extends AsyncTask<Void, Void, BlubbThread[]> {

        private Exception exception;

        @Override
        protected BlubbThread[] doInBackground(Void... voids) {
            BlubbComManager manager = new BlubbComManager();
            try {
                return manager.getAllThreads();
            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllThreads", e.getMessage());
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
                Log.e("getAllThreads", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(BlubbThread[] response) {
            String allThreadsString = "";
            if(response != null) {
                for(BlubbThread bt: response) {
                    allThreadsString = allThreadsString + bt.toString();
                }
            } else allThreadsString = this.exception.getMessage();

            TextView tview = (TextView) findViewById(R.id.blubb_com_test_view);
            tview.setText(allThreadsString);
        }
    }

    private class AsyncLogin extends AsyncTask<String, Void, String> {

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
            TextView tview = (TextView) findViewById(R.id.blubb_com_test_view);
            tview.setText(response);
        }
    }
}
