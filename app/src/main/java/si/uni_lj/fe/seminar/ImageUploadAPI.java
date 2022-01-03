package si.uni_lj.fe.seminar;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class ImageUploadAPI implements Callable<String> {
    private String token, urlService, imagePath, gender, name, responseBody;
    private final Activity callerActivity;
    private InputStream inputStream;
    private int responseCode;

    public ImageUploadAPI (String token, Uri imagePath, String gender, String name, String urlService, Activity callerActivity){
        this.urlService = String.valueOf(urlService);
        this.token = token;
        this.imagePath = String.valueOf(imagePath);
        this.gender = String.valueOf(gender);
        this.name = String.valueOf(name);
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
                    return callerActivity.getResources().getString(R.string.image_upload_successful);
                }
                else{
                    return callerActivity.getResources().getString(R.string.image_upload_error);
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
        URL url = new URL(urlService + "/" + token);
        try {
            OkHttpClient client = new OkHttpClient();
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", name+imagePath, RequestBody.create(imagePath, MediaType.parse("image/jpeg")))
                    .addFormDataPart("gender", gender)
                    .addFormDataPart("name", name)
                    .build();
            //Todo: fix imageExtensions

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                responseCode = response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode;
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
