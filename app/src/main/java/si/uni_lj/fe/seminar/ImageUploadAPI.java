package si.uni_lj.fe.seminar;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

import okhttp3.OkHttp;
import okhttp3.Response;

class ImageUploadAPI implements Callable<String> {
    private String token;
    private String urlService;
    private String gender;
    private String name;
    private int serverResponse;
    private String selectedImageName;
    private String selectedImageBase64;
    private String extension;
    private final Activity callerActivity;
    private InputStream inputStream;

    public ImageUploadAPI(String token, String imageName, String imageBase64, String gender, String name, String urlService, Activity callerActivity) {
        this.urlService = String.valueOf(urlService);
        this.token = token;
        this.selectedImageName = String.valueOf(imageName);
        this.gender = String.valueOf(gender);
        this.name = String.valueOf(name);
        this.callerActivity = callerActivity;
        this.selectedImageBase64 = imageBase64;

        extension = selectedImageName.substring(selectedImageName.lastIndexOf(".")+1);

    }

    @Override
    public String call() {
        ConnectivityManager connMgr = (ConnectivityManager) callerActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;

        try {
            networkInfo = connMgr.getActiveNetworkInfo();
        } catch (Exception e) {
            return callerActivity.getResources().getString(R.string.network_error);
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                int responseCode = connect(token);
                return callerActivity.getResources().getString(R.string.image_upload_successful);

            } catch (IOException e) {
                e.printStackTrace();
                return callerActivity.getResources().getString(R.string.service_error);
            }
        } else {
            return callerActivity.getResources().getString(R.string.network_error);
        }
    }

    private int connect(String token) throws IOException {
        URL url = new URL(urlService + "/" + token);
        try {
            AndroidNetworking.setParserFactory(new JacksonParserFactory());
            AndroidNetworking.post(String.valueOf(url))
                    .addBodyParameter("gender", gender)
                    .addBodyParameter("extension", extension)
                    .addBodyParameter("name", name)
                    .addBodyParameter("image", selectedImageBase64)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsOkHttpResponse(new OkHttpResponseListener() {
                        @Override
                        public void onResponse(Response response) {
                            serverResponse = response.code();
                        }
                        @Override
                        public void onError(ANError error) {
                            if (error.getErrorCode() != 0) {
                                Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                            } else {
                                Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponse;
    }
}

