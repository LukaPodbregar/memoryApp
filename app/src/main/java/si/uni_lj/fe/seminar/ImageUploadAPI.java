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
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import okhttp3.Response;

// This API allows user to upload new Image
class ImageUploadAPI implements Callable<String> {
    private String token, urlService, gender, name, selectedImageName, selectedImageBase64, extension;
    private int serverResponse;
    private final Activity callerActivity;

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
        // Create the correct url
        URL url = new URL(urlService + "/" + token);
        try {
            //TODO: do better

            // Build an object to upload (used okHttp library), image to upload is in base64 format
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

