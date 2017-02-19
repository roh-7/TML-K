package siesgst.tml17.idscan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {
    Button scan;
    boolean flag = false;
    String barcode_scan;
    private Request request;
    String responseString;
    SessionManager session;
    RecyclerViewAdapter recyclerViewAdapter;
    TextView EventIdName;
    TextView Email;
    TextView Event;
    TextView Contact_number;
    Button logout;
    TextView add;
    RecyclerView recyclerView;
     View parentLayout   ;
    List<Player> listOfPlayers;
    AlertDialog alert;
    StringBuffer stringBuffer = new StringBuffer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_detail);
        Log.v("tag", "back to OnCreate");

        session = new SessionManager(DetailActivity.this);

         parentLayout = findViewById(R.id.contentdetail);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        PlayerList(false);// call to async task

        recyclerView.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_scan) {
            scan();
            return true;

        }

        else if (id == R.id.action_manually) {
            Log.d("add manually", "in");
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
            LayoutInflater inflater = DetailActivity.this.getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_box_layout, null);
            builder.setView(view);
            final EditText input = (EditText) view.findViewById(R.id.ManualUID);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    barcode_scan = input.getText().toString();
                    Toast.makeText(DetailActivity.this, "Input=" + barcode_scan, Toast.LENGTH_LONG).show();
                    play(barcode_scan);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return true;

        }
        else if (id == R.id.update_manually) {
            RecyclerViewAdapter adapter = (RecyclerViewAdapter) recyclerView.getAdapter();
            listOfPlayers = adapter.getselectedList();

            if (listOfPlayers.size() != 0) {

                for (int i = 0; i < listOfPlayers.size(); i++) {
                    String prn = listOfPlayers.get(i).getUID();
                    UIDupdate(prn);
                }
                adapter.clear();
                adapter.notifyDataSetChanged();

            }
            else {

                Toast.makeText(DetailActivity.this, "No items selected", Toast.LENGTH_SHORT).show();
            }

            return true;

        }
        else if (id == R.id.log_out_menu) {

            session.logoutUser(DetailActivity.this);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    public void scan() {

        final Activity activity = this;
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Set over barcode of SIES GST College ID");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
        integrator.setOrientationLocked(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "cancelled scan");
                flag = false;
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                barcode_scan = "fail";
            } else {
                Log.d("MainActivity", "Scanned");
                barcode_scan = result.getContents();
                flag = true;
                Toast.makeText(this, "Scanned: " + barcode_scan, Toast.LENGTH_LONG).show();
                play(barcode_scan);
            }
        }
    }

    public void play(String prn) {
        String id1 = session.getID();
        Log.v("tag", id1);
        String url = "http://development.siesgst.ac.in/play.php";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("prn", prn)
                .add("event_id", id1)
                .build();
        request = new Request.Builder()
                .url(url)
                .method("POST", body.create(null, new byte[0]))
                .post(body)
                .build();
        Log.v("url", body.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("onfailure", "fail");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseString = response.body().string();
                Log.v("responseplay", responseString);
                try {
                    JSONObject root = new JSONObject(responseString);
                    String status = root.optString("s" +
                            "status");
                    String message = root.optString("message");
                    if (message.equalsIgnoreCase("play")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildDialog(true);
                            }
                        });
                        Log.v("tag", "Can play");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildDialog(false);
                            }
                        });
                        Log.v("tag", "Cannot play");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TML", "scanned barcode");
                        PlayerList(true);
                    }
                });
            }
        });// call to update the list
    }

    public void UIDupdate(String prn) {
        Log.v("prn", prn);
        String id1 = session.getID();
        String url = "http://development.siesgst.ac.in/update.php";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("prn", prn)
                .add("event_id", id1)
                .build();
        request = new Request.Builder()
                .url(url)
                .method("POST", body.create(null, new byte[0]))
                .post(body)
                .build();
        Log.v("url", body.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseString = response.body().string();
                Log.v("responseUpdate", responseString);
                try {
                    JSONObject root = new JSONObject(responseString);
                    String status = root.optString("status");
                    String message = root.optString("message");
                    if (message.equalsIgnoreCase("true")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               // Toast.makeText(DetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                Snackbar snackbar= Snackbar.make(parentLayout,"Updated",Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                   }
                               });

                                snackbar.show();
                                PlayerList(true);
                            }
                        });
                        Log.v("tag", "Can play again");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailActivity.this, "NO", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.v("tag", "Cannot play");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
       // call to update the list
    }

    void buildDialog(boolean decide) {
        //custom Dialog Alert
        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
        LayoutInflater inflater = DetailActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.uid_result, null);

        // Set up the input
        builder.setView(view);
        final TextView status=(TextView)view.findViewById(R.id.play_status) ;

        final TextView button = (TextView) view.findViewById(R.id.scan_status);
        final ImageView img = (ImageView) view.findViewById(R.id.uid_result_image);

        //GradientDrawable imgBackground = (GradientDrawable) img.getBackground();
        Log.v("Detail Name",session.getName());
        Log.v("Detail ID",session.getID());



        if (decide) {
            img.setBackgroundColor(Color.parseColor("#00E676"));
            button.setBackgroundColor(Color.parseColor("#00E676"));
            img.setImageResource(R.drawable.tick);
            status.setText("Play!");
        } else {
            img.setBackgroundColor(Color.parseColor("#F44336"));
            button.setBackgroundColor(Color.parseColor("#F44336"));
            status.setText("Can't Play");

            img.setImageResource(R.drawable.cross);
        }
        // Set up the buttons
        final AlertDialog d =builder.show();
        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                d.dismiss();
            }});
    }

    public void PlayerList(boolean isRefresh) {
        //checking if user is offline
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null) {

            Log.v("tag", "inplayerlist");
            ListAsync listAsync = new ListAsync(session.getID(), DetailActivity.this, recyclerView,recyclerViewAdapter, isRefresh);
            listAsync.execute();
        } else {
            Snackbar snackbar = Snackbar.make(parentLayout, "No Network connection!", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            snackbar.show();
        }
    }
}
