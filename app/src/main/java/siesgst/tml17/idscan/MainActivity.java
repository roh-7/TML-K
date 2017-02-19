package siesgst.tml17.idscan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {
    String barcode_scan;
    SessionManager session;
    EditText username, password;
    TextView login;
    private Request request;
    String responseString;
    String event_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        session = new SessionManager(MainActivity.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Login_try();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                barcode_scan = result.getContents();
                Toast.makeText(this, "Scanned: " + barcode_scan, Toast.LENGTH_LONG).show();
            }

        } else {
            //to pass result to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void Login_try() {

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (TextView) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "http://development.siesgst.ac.in/login.php";
                final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                if (activeNetwork != null) {

                    if (!isEmpty(username.getText().toString()) && !isEmpty(password.getText().toString())) {

                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                        progressDialog.setCancelable(true);
                        progressDialog.setMessage("Signing In...");
                        Log.v("prog?", "prog.");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();

                        OkHttpClient client = new OkHttpClient();

                        RequestBody body = new FormBody.Builder()
                                .add("email", username.getText().toString())
                                .add("password", password.getText().toString())
                                .build();
                        request = new Request.Builder()
                                .url(url)
                                .method("POST", body.create(null, new byte[0]))
                                .post(body)
                                .build();
                        Log.v("login", body.toString());

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                responseString = response.body().string();
                                Log.v("response", responseString);
                                if (responseString.contains("true")) {

                                    JSONObject root = null;

                                    try {
                                        root = new JSONObject(responseString);
                                        String status = root.optString("status");
                                        String message = root.optString("message");
                                        JSONArray result = root.optJSONArray("result");

                                        for (int i = 0; i < result.length(); i++) {

                                            JSONObject resultArrayObject = result.optJSONObject(i);
                                            String id = resultArrayObject.optString("id");
                                            String fname = resultArrayObject.optString("fname");
                                            String lname = resultArrayObject.optString("lname");
                                            String email = resultArrayObject.optString("email");
                                            String contact = resultArrayObject.optString("contact");
                                            event_id = resultArrayObject.optString("event_id");
                                            String event_name = resultArrayObject.optString("event_name");
                                            String created = resultArrayObject.optString("created_at");
                                            String updated = resultArrayObject.optString("updated_at");
                                            session.createLoginSession(fname + "  " + lname, email, event_name, contact, event_id);


                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                    startActivity(intent);
                                    finish();
                                    progressDialog.dismiss();
                                } else {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.login_layout), "Login Failed! Check your credentials", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    });

                                    snackbar.show();
                                    progressDialog.dismiss();

                                }
                            }

                        });
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.login_layout), "Fill in all details", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        });

                        snackbar.show();

                    }
                }
                else{
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.login_layout), "No Network connection!", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    snackbar.show();
                }
            }
        });
    }



}