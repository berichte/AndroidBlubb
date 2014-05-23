package com.blubb.alubb.beapcom;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blubb.alubb.basics.SessionInfo;
import com.blubb.alubb.blubbbasics.R;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

public class BlubbComTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blubb_com_test);

        addButtonListener();

    }

    private void addButtonListener() {
        Button testButton = (Button) findViewById(R.id.blubb_com_test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] params = {"B-Richter", "carolina"};
               new AsyncLogin().execute(params);
            }
        });
    }

    private class AsyncLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String username = params[0],
                    password = params[1];
            BlubbComManager manager = new BlubbComManager();
            try {
                if (manager.login(username, password)){
                    return SessionManager.getInstance().getSessionId().toString();
                }
            } catch (InvalidParameterException e) {
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
