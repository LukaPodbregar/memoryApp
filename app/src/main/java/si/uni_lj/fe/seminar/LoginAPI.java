package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

class LoginAPI implements Callable<String> {
    private final String username;
    private final String password;
    private final String urlService;
    private final Activity callerActivity;
    private static final String TAG = "MyActivity";

    public LoginAPI(String username, String password, String urlService,Activity callerActivity) {
        this.username = String.valueOf(username);
        this.password = String.valueOf(password);
        this.urlService = String.valueOf(urlService);
        this.callerActivity = callerActivity;
    }

    @Override
    public String call() {
        ConnectivityManager connMgr = (ConnectivityManager) callerActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;

        try {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        catch (Exception e){
            return callerActivity.getResources().getString(R.string.napaka_omrezje);
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                int responseCode = connect(username, password);

                if(responseCode==204){
                    return callerActivity.getResources().getString(R.string.rest_rezultat_dodan);
                }
                else{
                    return callerActivity.getResources().getString(R.string.rest_nepricakovan_odgovor)+" "+responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return callerActivity.getResources().getString(R.string.napaka_storitev);
            }
        }
        else{
            return callerActivity.getResources().getString(R.string.napaka_omrezje);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the content as a InputStream, which it returns as a string.
    private int connect(String username, String password) throws IOException {
        URL url = new URL(urlService+"/"+username+"/"+password);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000 /* milliseconds */);
        conn.setConnectTimeout(10000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoInput(true);

        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            // Starts the query
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(json.toString());
            writer.flush();
            writer.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn.getResponseCode();
    }
}
