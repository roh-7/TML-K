package siesgst.tml17.idscan;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class ListAsync extends AsyncTask<String, Void, String> {
    String event_id;
    public List<Player> player;
    public Context context;
    ProgressDialog prog;
    SessionManager session;
    RecyclerView recyclerView;
    boolean isRefresh = false;
    RecyclerViewAdapter recyclerViewAdapter;


    public ListAsync(String event_id, Context context, RecyclerView recyclerView,RecyclerViewAdapter recyclerViewAdapter, boolean isRefresh) {
        this.event_id = event_id;
        this.player = player;
        this.context = context;
        session = new SessionManager(context);
        this.recyclerView = recyclerView;
        this.recyclerViewAdapter=recyclerViewAdapter;
        this.isRefresh = isRefresh;
        //this.prog = prog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        prog = new ProgressDialog(context);
        prog.setCancelable(true);
        prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prog.setMessage("Getting List...");
        prog.setTitle("TML");
        Log.v("tag", "before prog.show");
        prog.show();
    }

    @Override
    protected String doInBackground(String... params) {

        String urlForList = "http://development.siesgst.ac.in/list.php?event_id=" + event_id;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {

            URL url = new URL(urlForList);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        String result = stringBuilder.toString();
        Log.v("tag", result);
        return result;

    }

    @Override
    protected void onPostExecute(String s) {
        Log.v("responsePost", "OnPostExcecute !");
        Log.v("response", s);
        try {
            JSONObject root = new JSONObject(s);
            String status = root.optString("status");
            JSONArray MessageArray = root.optJSONArray("message");
            Log.d("TML MSG ARRAY LEN", MessageArray.length() + "");
            for (int i = 0; i < MessageArray.length(); i++) {
                JSONObject jsonObject = MessageArray.optJSONObject(i);
                String id = jsonObject.optString("id");
                String user_id = jsonObject.optString("user_id");
                String event_id = jsonObject.optString("event_id");
                String event_name = jsonObject.optString("event_name");
                String event_credit = jsonObject.optString("event_credit");
                String statusPlayer = jsonObject.optString("status");

                if (statusPlayer.equalsIgnoreCase("0")) {
                    Log.d("TML PLAY", "can play");
                    session.addPlayer(new Player(user_id, event_name, " ", "Can Play"));
                } else {
                    Log.d("TML PLAY", "can't play");
                    session.addPlayer(new Player(user_id, event_name, " ", "Can't Play"));

                }

            }

            prog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isRefresh) {
            recyclerView.getAdapter().notifyDataSetChanged();
        } else {
            recyclerView.setAdapter(new RecyclerViewAdapter(context, session.getPlayer()));
        }

    }


}
