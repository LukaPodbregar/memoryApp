package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

// This API allows user to sign up
class SignupAPI implements Callable<String> {
    private final String username, password, urlService;
    private final Activity callerActivity;

    public SignupAPI(String username, String password, String urlService,Activity callerActivity) {
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
            return callerActivity.getResources().getString(R.string.network_error);
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                int responseCode = connect(username, password);

                if(responseCode==201){
                    return callerActivity.getResources().getString(R.string.signup_successfully);
                }
                if(responseCode==409){
                    return callerActivity.getResources().getString(R.string.signup_username_taken);
                }
                else{
                    return callerActivity.getResources().getString(R.string.login_error)+" "+responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return callerActivity.getResources().getString(R.string.service_error);
            }
        }
        else{
            return callerActivity.getResources().getString(R.string.network_error);
        }
    }

    private int connect(String username, String password) throws IOException {
        // Create the correct url
        URL url = new URL(urlService);

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
