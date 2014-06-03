package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbMessage;
import com.blubb.alubb.blubexceptions.BlubbDBException;
import com.blubb.alubb.blubexceptions.InvalidParameterException;
import com.blubb.alubb.blubexceptions.SessionException;


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

    private class AsyncSendMessage extends AsyncTask <Void, String, BlubbMessage> {

        private Exception exception;
        private String tId, mTitle, mContent;

        public AsyncSendMessage(String tId, String mTitle, String mContent) {
            this.tId = tId;
            this.mTitle = mTitle;
            this.mContent = mContent;
        }

        private BlubbApplication getApp() {
            return (BlubbApplication) getApplication();
        }

        @Override
        protected BlubbMessage doInBackground(Void... blubbs) {
            try {
                return getApp().getMessageManager().createMsg(
                        WriteMessageActivity.this, tId, mTitle, mContent);

            } catch (BlubbDBException e) {
                this.exception = e;
                Log.e("getAllMessages", e.getMessage());
            } catch(InvalidParameterException e) {
                this.exception = e;
            } catch (SessionException e) {
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(BlubbMessage message) {
           if(message != null) {
               String msg = "Created new Message:\n" +
                       "tId: " + message.getmThread() + "\n" +
                       "tTitle: " + message.getmTitle();
           }
        }
    }

}
