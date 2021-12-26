package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

class ImagesAPI implements Callable<String> {
    private String token, urlService;
    private final Activity callerActivity;
    private InputStream inputStream;
    private String returnJson;

    public ImagesAPI (String token, String urlService, Activity callerActivity){
        this.urlService = String.valueOf(urlService);
        this.token = token;
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
            int responseCode = connect(token);

            if(responseCode==201){
                Login.didUserSignin = 1;
                return callerActivity.getResources().getString(R.string.login_successfully);
            }
            if(responseCode==401){
                return callerActivity.getResources().getString(R.string.login_wrong_password);
            }
            else{
                return callerActivity.getResources().getString(R.string.login_wrong_username);
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

    private int connect(String token) throws IOException {
        URL url = new URL(urlService+"/"+token);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000 /* milliseconds */);
        conn.setConnectTimeout(10000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        try {
            conn.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conn.getResponseCode() == 201) {
            inputStream = conn.getInputStream();
            returnJson = convertStreamToString(inputStream);
            try {
                JSONArray jsonArray = new JSONArray(returnJson);
                String imageNames[] =new String[jsonArray.length()];
                String imagePaths[] =new String[jsonArray.length()];
                Context applicationContext = Login.getContextOfApplication();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                SharedPreferences.Editor editor = prefs.edit();

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObjTemp = (JSONObject) jsonArray.get(i);
                    imageNames[i] = jsonObjTemp.get("imageName").toString();
                    imagePaths[i] = jsonObjTemp.get("path").toString();
                }
                Set<String> mySet = new HashSet<>(Arrays.asList(imageNames));
                Set<String> mySet1 = new HashSet<>(Arrays.asList(imagePaths));

                editor.putStringSet("imageName", mySet);
                editor.putStringSet("imagePath", mySet1);
                editor.apply();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return conn.getResponseCode();
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
