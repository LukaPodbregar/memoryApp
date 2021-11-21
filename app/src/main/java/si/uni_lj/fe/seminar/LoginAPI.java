package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

class LoginAPI implements Callable<String> {
    private final String username, password, urlService;
    private final Activity callerActivity;

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
            return callerActivity.getResources().getString(R.string.network_error);
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                int responseCode = connect(username, password);

                if(responseCode==200){
                    Login.didUserSignin = 1;
                    return callerActivity.getResources().getString(R.string.login_successfully);
                }
                if(responseCode==401){
                    return callerActivity.getResources().getString(R.string.login_wrong_password);
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

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the content as a InputStream, which it returns as a string.
    private int connect(String username, String password) throws IOException {
        URL url = new URL(urlService+"/"+username+"/"+password);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000 /* milliseconds */);
        conn.setConnectTimeout(10000 /* milliseconds */);
        conn.setRequestMethod("GET");

        try {
            conn.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn.getResponseCode();
    }
}
