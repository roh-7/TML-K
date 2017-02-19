package siesgst.tml17.idscan;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by leprechaun on 15/1/17.
 */


public class BackgroundWorker extends AsyncTask<String,String,String> {

    SessionManager sessionManager;
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    String username = "sql6154047";
    String password = "21UNKukyG1";
    Context context;
    BackgroundWorker(Context ctx){
        context = ctx;
    }

      ProgressDialog pdLoading = new ProgressDialog(context);
       HttpURLConnection conn;
       URL url = null;

       @Override
       protected void onPreExecute() {
           super.onPreExecute();

           //this method will be running on UI thread
           pdLoading.setMessage("\tLoading...");
           pdLoading.setCancelable(false);
           pdLoading.show();

       }


       @Override
       protected String doInBackground(String... params) {
           try {

               // Enter URL address where your php file resides
               url = new URL("http://www.phpmyadmin.co/");

           } catch (MalformedURLException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
               return "exception";
           }
           try {
               // Setup HttpURLConnection class to send and receive data from php and mysql
               conn = (HttpURLConnection)url.openConnection();
               conn.setReadTimeout(READ_TIMEOUT);
               conn.setConnectTimeout(CONNECTION_TIMEOUT);
               conn.setRequestMethod("POST");

               // setDoInput and setDoOutput method depict handling of both send and receive
               conn.setDoInput(true);
               conn.setDoOutput(true);

               // Append parameters to URL
               Uri.Builder builder = new Uri.Builder()
                       .appendQueryParameter("username", params[0])
                       .appendQueryParameter("password", params[1]);
               String query = builder.build().getEncodedQuery();

               // Open connection for sending data
               OutputStream os = conn.getOutputStream();
               BufferedWriter writer = new BufferedWriter(
                       new OutputStreamWriter(os, "UTF-8"));
               writer.write(query);
               writer.flush();
               writer.close();
               os.close();
               conn.connect();

           } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
               return "exception";
           }

           try {

               int response_code = conn.getResponseCode();

               // Check if successful connection made
               if (response_code == HttpURLConnection.HTTP_OK) {

                   // Read data sent from server
                   InputStream input = conn.getInputStream();
                   BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                   StringBuilder result = new StringBuilder();
                   String line;

                   while ((line = reader.readLine()) != null) {
                       result.append(line);
                   }

                   // Pass data to onPostExecute method
                   return(result.toString());

               }else{

                   return("unsuccessful");
               }

           } catch (IOException e) {
               e.printStackTrace();
               return "exception";
           } finally {
               conn.disconnect();
           }


       }



    @Override
    protected void onPostExecute(String result) {
        pdLoading.dismiss();
        if(result.equalsIgnoreCase("true"))
        {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
            if(!sessionManager.isLoggedIn()){

            }


        }else if (result.equalsIgnoreCase("false")){

            // If username and password does not match display a error message
            Toast.makeText(context.getApplicationContext(), "Invalid email or password", Toast.LENGTH_LONG).show();

        } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

            Toast.makeText(context.getApplicationContext(), "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
