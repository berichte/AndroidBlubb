package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.beapcom.BlubbDBReplyStatus;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WriteMessageActivity extends Activity {

    private String threadId;
    private String tTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_message);
        threadId = getIntent().getStringExtra(SingleThreadActivity.EXTRA_THREAD_ID);
        tTitle = getIntent().getStringExtra(SingleThreadActivity.EXTRA_THREAD_TITLE);
        Button sendButton = (Button) findViewById(R.id.write_new_message_send_button);
        TextView tTitle = (TextView) findViewById(R.id.write_new_message_thread_name_tv);
        tTitle.setText(threadId);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText titleEdit = (EditText) findViewById(R.id.write_new_message_title_editText);
                String mTitle = titleEdit.getText().toString();
                if (mTitle == null) mTitle = "";

                EditText contentEdit = (EditText) findViewById(
                        R.id.write_new_message_content_editText);
                String mContent = contentEdit.getText().toString();
                if (mContent == null) mContent = "";

                AsyncSendMessage asyncSendMessage =
                        new AsyncSendMessage(WriteMessageActivity.this.threadId, mTitle, mContent);
                asyncSendMessage.execute();
            }
        });
    }

    private class AsyncSendMessage extends AsyncTask <Void, String, BlubbDBReplyStatus> {

        private Exception exception;
        private String tId, mTitle, mContent;

        public AsyncSendMessage(String tId, String mTitle, String mContent) {
            this.tId = tId;
            this.mTitle = mTitle;
            this.mContent = mContent;
        }

        @Override
        protected BlubbDBReplyStatus doInBackground(Void... blubbs) {

            BlubbComManager manager = new BlubbComManager();
            try {
                return manager.sendMessage(tId, mTitle, mContent);

            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllMessages", e.getMessage());
            } catch (BlubbDBConnectionException e) {
                this.exception = e;
                Log.e("getAllMessages", e.getMessage());
            } catch(InvalidParameterException e) {
                this.exception = e;
            }
            return BlubbDBReplyStatus.REQUEST_FAILURE;
        }

        @Override
        protected void onPostExecute(BlubbDBReplyStatus result) {
           if(result == BlubbDBReplyStatus.OK) {
               Intent intent = new Intent(WriteMessageActivity.this, SingleThreadActivity.class);
               intent.putExtra(SingleThreadActivity.EXTRA_THREAD_ID,
                       WriteMessageActivity.this.threadId);
               WriteMessageActivity.this.startActivity(intent);
           }
           else {
               Context context = getApplicationContext();
               CharSequence text = "something went terribly wrong: " + result.toString();
               int duration = Toast.LENGTH_LONG;

               Toast toast = Toast.makeText(context, text, duration);
               toast.show();
           }
        }
    }

}
