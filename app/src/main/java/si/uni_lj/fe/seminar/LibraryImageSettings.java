package si.uni_lj.fe.seminar;

import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class LibraryImageSettings extends Login implements View.OnClickListener {
    private ImageView currentImageView;
    private Object currentImageName, currentImageGender, currentImagePath;

    public void libraryImageSettings(ImageView imageView, Object imageNameArray, Object imageGenderArray, Object imagePathArray){
        this.currentImageView = imageView;
        this.currentImageName = imageNameArray;
        this.currentImageGender = imageGenderArray;
        this.currentImagePath = imageGenderArray;
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Login login = new Login();
        //TODO: content layout not working, fix :D
        login.setContentView(R.layout.image_settings_library);

        ImageView imageSettingsImageView = findViewById(R.id.imageSettingImage);
        EditText imageSettingGender = findViewById(R.id.imageSettingGender);
        EditText imageSettingName = findViewById(R.id.imageSettingName);
        imageSettingGender.setText((String) currentImageGender);
        imageSettingName.setText((String) currentImageName);

        String urlImage = "http://10.0.2.2/application/" + currentImagePath;

        Glide.with(this).load(urlImage).centerCrop().into(imageSettingsImageView);
    }
}
