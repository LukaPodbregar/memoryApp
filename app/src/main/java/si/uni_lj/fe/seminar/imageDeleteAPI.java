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

// This API allows user to delete images
class imageDeleteAPI implements Callable<String> {
    private final String urlService, imagePath, token;
    private final Activity callerActivity;

    public imageDeleteAPI(String token, String imagePath, String urlService, Activity callerActivity) {
        this.urlService = String.valueOf(urlService);
        this.callerActivity = callerActivity;
        this.imagePath = String.valueOf(imagePath);
        this.token = token;
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
                int responseCode = connect(token, imagePath);

                if(responseCode==201){
                    return callerActivity.getResources().getString(R.string.image_delete_successful);
                }
                else{
                    return callerActivity.getResources().getString(R.string.service_error)+" "+responseCode;
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

    private int connect(String token, String imagePath) throws IOException {
        // Create the correct url
        URL url = new URL(urlService + "/" + token + '&' + imagePath);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000 /* milliseconds */);
        conn.setConnectTimeout(10000 /* milliseconds */);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoInput(true);

        try {
            JSONObject json = new JSONObject();
            json.put("path", imagePath);

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
