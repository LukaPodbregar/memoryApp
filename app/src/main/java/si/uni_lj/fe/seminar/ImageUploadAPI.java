package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

class ImageUploadAPI implements Callable<String> {
    private String token, urlService, gender, name, responseBody, selectedImageName, selectedImageBase64, extension;
    private final Activity callerActivity;
    private InputStream inputStream;
    private int responseCode;

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
                connect(token);
                if (responseCode == 201) {
                    return callerActivity.getResources().getString(R.string.image_upload_successful);
                } else {
                    return callerActivity.getResources().getString(R.string.image_upload_error);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return callerActivity.getResources().getString(R.string.service_error);
            }
        } else {
            return callerActivity.getResources().getString(R.string.network_error);
        }
    }

    private void connect(String token) throws IOException {
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
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

