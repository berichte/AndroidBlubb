package com.blubb.alubb.blubbbasics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blubb.alubb.R;
import com.blubb.alubb.basics.BlubbThread;
import com.blubb.alubb.beapcom.BlubbComManager;
import com.blubb.alubb.blubexceptions.BlubbDBConnectionException;
import com.blubb.alubb.blubexceptions.BlubbDBException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ThreadOverview extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.activity_thread_overview);

        AsyncGetAllThreads asyncGetAllThreads = new AsyncGetAllThreads();
        asyncGetAllThreads.execute();

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
        protected void onPostExecute(final BlubbThread[] response) {
            ListView lv = (ListView) findViewById(R.id.thread_list);
            final ThreadArrayAdapter adapter = new ThreadArrayAdapter(
                    ThreadOverview.this, R.layout.thread_list_entry, response);
            lv.setAdapter(adapter);
            final List<BlubbThread> list = new ArrayList<BlubbThread>(Arrays.asList(response));

            lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Intent intent = new Intent(ThreadOverview.this, SingleThreadActivity.class);
                    assert ((BlubbThread) parent.getItemAtPosition(position)) != null;
                    String threadId = ((BlubbThread) parent.getItemAtPosition(position)).getId();
                    intent.putExtra(SingleThreadActivity.EXTRA_THREAD_ID, threadId);
                    ThreadOverview.this.startActivity(intent);

                    /*final String item = (String) parent.getItemAtPosition(position).toString();
                    view.animate().setDuration(2000).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    list.remove(item);
                                    adapter.notifyDataSetChanged();
                                    view.setAlpha(1);
                                }
                            });*/
                }

            });
           /* String allThreadsString = "";
            if(response != null) {
                for(BlubbThread bt: response) {
                    allThreadsString = allThreadsString + bt.toString() + "\n";
                }
            } else allThreadsString = this.exception.getMessage();

            TextView tv = (TextView) findViewById(R.id.thread_textv);
            tv.setText(allThreadsString);*/
        }
    }

    private class ThreadArrayAdapter extends ArrayAdapter<BlubbThread> {

        HashMap<BlubbThread, Integer> mIdMap = new HashMap<BlubbThread, Integer>();

        public ThreadArrayAdapter(Context context, int textViewResourceId,
                                  BlubbThread[] objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.length; ++i) {
                mIdMap.put(objects[i], i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) super.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.thread_list_entry, parent, false);
            TextView title = (TextView) rowView.findViewById(R.id.thread_list_item_head),
                description = (TextView) rowView.findViewById(R.id.thread_list_item_body);
            BlubbThread blubbThread = getItem(position);
            title.setText(blubbThread.getThreadTitle());
            description.setText(blubbThread.getDescription());

            return rowView;
        }

        @Override
        public long getItemId(int position) {
            BlubbThread item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


}
