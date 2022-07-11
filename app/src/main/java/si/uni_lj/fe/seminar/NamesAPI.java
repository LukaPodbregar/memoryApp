package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

// This API gets 10 random names of certain gender
class NamesAPI implements Callable<String> {
    private String gender, urlService;
    private final Activity callerActivity;
    private InputStream inputStream;
    private String returnJson;

    public NamesAPI (String gender, String urlService, Activity callerActivity){
        this.urlService = String.valueOf(urlService);
        this.gender = gender;
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
                int responseCode = connect(gender);

                if(responseCode==201){
                    return callerActivity.getResources().getString(R.string.names_download_successful);
                }
                else{
                    return callerActivity.getResources().getString(R.string.names_download_error);
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

    private int connect(String gender) throws IOException {
        // Create the correct url
        URL url = new URL(urlService+"/"+gender);

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
                // Save the returned names into database
                JSONArray jsonArray = new JSONArray(returnJson);
                String names[] =new String[jsonArray.length()];
                Context applicationContext = MainActivity.getContextOfApplication();
                TinyDB tinydb = new TinyDB(applicationContext);

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObjTemp = (JSONObject) jsonArray.get(i);
                    names[i] = jsonObjTemp.get("name").toString();
                }
                ArrayList<String> mySet = new ArrayList(Arrays.asList(names));
                tinydb.putListString("names", mySet);

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
