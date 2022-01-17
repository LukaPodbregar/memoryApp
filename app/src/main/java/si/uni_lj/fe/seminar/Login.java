package si.uni_lj.fe.seminar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Login extends AppCompatActivity {

    private int SELECT_PICTURE = 200;
    private String username, password, urlService, urlServiceImages, urlServiceImagesUpload, token, urlNames, rightAnswerNotification, wrongAnswerNotification, uploadName, uploadGender,
            selectedImageName, imageBase64;
    private TextView createAccount, faceNumber, rightAnswerCount, wrongAnswerCount;
    private Button signinButton, signupButton, startButton, faceLibrary, nameButton1, nameButton2, nameButton3, nameButton4, newFaceButton, uploadButton;
    private ImageView backButton, signoutButton, imageView, summaryBackButton, libraryBackButton, libraryNextPageButton, libraryPreviousPageButton, uploadBackButton,
            imageUploadSelectImageButton, imageUploadPreview;
    private EditText usernameField, passwordField, usernameFieldSignup, passwordFieldSignup, uploadNameField;
    static int didUserSignin, rightAnswer, imagesPerPage;
    private int pageNumber, rightAnswerCounter, wrongAnswerCounter, pageNumberLibrary, gameLength;
    public static Context contextOfApplication;
    private TinyDB tinydb;
    private ArrayList imagePath, imageGender, imageName, randomNames;
    private RadioGroup uploadGenderRadio;
    private Bitmap bitmap;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextOfApplication = getApplicationContext();

        if (didUserSignin == 0) {
            setContentView(R.layout.login_tab_fragment);
            signinAction();
        }
    }


    // Functions

    private void mainMenu() {
        setContentView(R.layout.activity_main);
        signoutButton = findViewById(R.id.signoutButton);
        startButton = findViewById(R.id.startGame);
        faceLibrary = findViewById(R.id.myFaces);
        newFaceButton = findViewById(R.id.addNewFaces);

        signoutButton.setOnClickListener(v -> {
            setContentView(R.layout.login_tab_fragment);
            didUserSignin = 0;
            signinAction();
        });
        startButton.setOnClickListener(v -> {
            game();
        });
        faceLibrary.setOnClickListener(v -> {
            library();
        });
        newFaceButton.setOnClickListener(v -> {
            faceUpload();
        });
    }

    private void faceUpload(){
        setContentView(R.layout.new_face_upload);
        uploadButton = findViewById(R.id.uploadButton);
        uploadNameField = findViewById(R.id.uploadNameField);
        uploadGenderRadio = findViewById(R.id.radioGroupGender);
        uploadBackButton = findViewById(R.id.uploadBack);
        imageUploadSelectImageButton = findViewById(R.id.imageUploadSelectImage);

        uploadBackButton.setOnClickListener(v -> {
            mainMenu();
        });

        imageUploadSelectImageButton.setOnClickListener(v -> {
            selectImage();
        });

        uploadButton.setOnClickListener(v -> {
            uploadName = String.valueOf(uploadNameField.getText());

            int checkedGender = uploadGenderRadio.getCheckedRadioButtonId();
            if (checkedGender != -1) {
                RadioButton selectedRadioButton = uploadGenderRadio.findViewById(checkedGender);
                String selectedText = (String) selectedRadioButton.getText();
                if (selectedText.equals("M")){
                    uploadGender = "male";
                }
                else {
                    uploadGender = "female";
                }
                if (!uploadName.equals("")) {
                    Context applicationContext = Login.getContextOfApplication();
                    TinyDB tinydb = new TinyDB(applicationContext);
                    token = tinydb.getString("token");
                    urlServiceImagesUpload = getResources().getString(R.string.URL_images_upload);
                    new AsyncTaskExecutor().execute(new ImageUploadAPI(token, selectedImageName, imageBase64, uploadGender, uploadName, urlServiceImagesUpload, this), (result) -> {});
                }
                else {
                    notificationToast(getResources().getString(R.string.pleaseSelectAllParameters));
                }
            }
            else{
                notificationToast(getResources().getString(R.string.pleaseSelectAllParameters));
            }
        });
    }

    private void selectImage(){
        openGallery();
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imageUploadPreview = findViewById(R.id.imageUploadPreview);
                    imageUploadPreview.setImageURI(selectedImageUri);
                    selectedImageName = getRealNameFromURI(selectedImageUri);
                    Context applicationContext = getContextOfApplication();
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), selectedImageUri);
                        imageBase64 = ConvertBitmapToBase64Format(imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private String ConvertBitmapToBase64Format(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        String imageString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        return imageString;
    }

    private String getRealNameFromURI(Uri contentURI) {
        String thePath = "no-path-found";
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(contentURI, filePathColumn, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            thePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return  thePath;
    }

    private void library() {
        setContentView(R.layout.library_selection);
        Context applicationContext = Login.getContextOfApplication();
        TinyDB tinydb = new TinyDB(applicationContext);
        pageNumberLibrary = 0;
        imagesPerPage = 10;
        token = tinydb.getString("token");
        urlServiceImages = getResources().getString(R.string.URL_images);
        new AsyncTaskExecutor().execute(new ImagesAPI(token, urlServiceImages, this), (result) -> {
            loadLibrary(tinydb, pageNumberLibrary);
        });

        libraryBackButton = findViewById(R.id.libraryBack);
        libraryBackButton.setOnClickListener(v -> {
            mainMenu();
        });

        imagePath = tinydb.getListString("imagePath");
        Object[] imagePathArray = imagePath.toArray();

        libraryNextPageButton = findViewById(R.id.libraryNextPage);
        double imageArrayLength = imagePathArray.length;
        double imagesPerPage2 = imagesPerPage;
        double maximumNumberOfPages = Math.ceil(imageArrayLength / imagesPerPage2)-1;
        libraryNextPageButton.setOnClickListener(v -> {
            if (pageNumberLibrary < maximumNumberOfPages) {
                pageNumberLibrary = pageNumberLibrary + 1;
                loadLibrary(tinydb, pageNumberLibrary);
            }
        });

        libraryPreviousPageButton = findViewById(R.id.libraryPreviousPage);
        libraryPreviousPageButton.setOnClickListener(v -> {
            if (pageNumberLibrary != 0) {
                pageNumberLibrary = pageNumberLibrary - 1;
                loadLibrary(tinydb, pageNumberLibrary);
            }
        });
    }

    private void loadLibrary(TinyDB tinydb, int pageNumberLibrary) {
        imagePath = tinydb.getListString("imagePath");
        imageGender = tinydb.getListString("imageGender");
        imageName = tinydb.getListString("imageName");
        urlNames = getResources().getString(R.string.URL_names);

        Object[] imagePathArray = imagePath.toArray();
        //Object[] imageNameArray = imageName.toArray();
        //Object[] imageGenderArray = imageGender.toArray();

        ImageView[] imageViews = new ImageView[imagesPerPage];

        for (int j = 0; j < imagesPerPage; j++) {
            String viewImage = "libraryImage" + j;
            int resIDImage = getResources().getIdentifier(viewImage, "id", getPackageName());
            imageViews[j] = ((ImageView) findViewById(resIDImage));
        }
        newImagePageLibrary(imagePathArray, imageViews, pageNumberLibrary);
    }

    private void newImagePageLibrary(Object[] imagePathArray, ImageView[] imageViews, int pageNumberLibrary) {
        for (int k = (pageNumberLibrary * imagesPerPage); k < (imagesPerPage + pageNumberLibrary * imagesPerPage); k++) {
            if (k < imagePathArray.length){
                String urlImage = "http://10.0.2.2/application/" + imagePathArray[k];
                Glide.with(this).load(urlImage).centerCrop().into(imageViews[(k-(pageNumberLibrary * 10))]);
            }
            else{
                imageViews[(k-(pageNumberLibrary * imagesPerPage))].setImageResource(0);
            }
        }
}


    private void imageSettingsLibrary(ImageView[] imageViews, int k){
        setContentView(R.layout.library_selection);
        //Glide.with(this).load(urlImage).into(imageViews[k]);
    }

    private void game() {
        Context applicationContext = Login.getContextOfApplication();
        TinyDB tinydb = new TinyDB(applicationContext);
        token = tinydb.getString("token");
        urlServiceImages = getResources().getString(R.string.URL_images);
        setContentView(R.layout.game_main);
        new AsyncTaskExecutor().execute(new ImagesAPI(token, urlServiceImages, this), (result) -> {
            startGame();
        });
    }

    private void startGame(){
        pageNumber = 0;
        rightAnswerCounter = 0;
        wrongAnswerCounter = 0;
        Context applicationContext = Login.getContextOfApplication();
        TinyDB tinydb = new TinyDB(applicationContext);
        imagePath = tinydb.getListString("imagePath");
        Object[] imagePathArray = imagePath.toArray();
        gameLength = 10;
        if (imagePathArray.length<gameLength){
            gameLength =imagePathArray.length;
        }

        loadNewFace(pageNumber, applicationContext);

        nameButton1 = findViewById(R.id.nameButton1);
        nameButton2 = findViewById(R.id.nameButton2);
        nameButton3 = findViewById(R.id.nameButton3);
        nameButton4 = findViewById(R.id.nameButton4);

        nameButton1.setOnClickListener(v -> {
            if (rightAnswer == 1){
                rightAnswer();
            }
            else{
                wrongAnswer();
            }
            imagePath = tinydb.getListString("imagePath");
            if (pageNumber<gameLength) {
                loadNewFace(pageNumber, applicationContext);
            }
            else {
                android.os.SystemClock.sleep(500);
                afterGameSummary();
            }
        });

        nameButton2.setOnClickListener(v -> {
            if (rightAnswer == 2){
                rightAnswer();
            }
            else{
                wrongAnswer();
            }
            if (pageNumber<gameLength) {
                loadNewFace(pageNumber, applicationContext);
            }
            else {
                android.os.SystemClock.sleep(500);
                afterGameSummary();
            }
        });

        nameButton3.setOnClickListener(v -> {
            if (rightAnswer == 3){
                rightAnswer();
            }
            else{
                wrongAnswer();
            }
            if (pageNumber<gameLength) {
                loadNewFace(pageNumber, applicationContext);
            }
            else {
                android.os.SystemClock.sleep(500);
                afterGameSummary();
            }
        });

        nameButton4.setOnClickListener(v -> {
            if (rightAnswer == 4){
                rightAnswer();
            }
            else{
                wrongAnswer();
            }
            if (pageNumber<gameLength) {
                loadNewFace(pageNumber, applicationContext);
            }
            else {
                android.os.SystemClock.sleep(500);
                afterGameSummary();
            }
        });
    }

    private void afterGameSummary(){
        setContentView(R.layout.after_game_report);
        wrongAnswerCount = findViewById(R.id.wrongAnswerCountSummary);
        wrongAnswerCount.setText(Integer.toString(wrongAnswerCounter));
        rightAnswerCount = findViewById(R.id.rightAnswerCountSummary);
        rightAnswerCount.setText(Integer.toString(rightAnswerCounter));
        summaryBackButton = findViewById(R.id.summaryBack);
        summaryBackButton.setOnClickListener(v -> {
            mainMenu();
        });
    }

    private void rightAnswer(){
        rightAnswerNotification = "You got the right answer!";
        notificationToast(rightAnswerNotification);
        pageNumber = pageNumber+1;
        rightAnswerCounter = rightAnswerCounter+1;
        rightAnswerCount = findViewById(R.id.rightAnswerCount);
        rightAnswerCount.setText(Integer.toString(rightAnswerCounter));
        android.os.SystemClock.sleep(500);
    }

    private void wrongAnswer(){
        wrongAnswerNotification = "You got the wrong answer!";
        notificationToast(wrongAnswerNotification);
        pageNumber = pageNumber+1;
        wrongAnswerCounter = wrongAnswerCounter+1;
        wrongAnswerCount = findViewById(R.id.wrongAnswerCount);
        wrongAnswerCount.setText(Integer.toString(wrongAnswerCounter));
        android.os.SystemClock.sleep(500);
    }

    private void loadNewFace(int i, Context applicationContext){
        TinyDB tinydb = new TinyDB(applicationContext);
        imagePath = tinydb.getListString("imagePath");
        imageGender = tinydb.getListString("imageGender");
        imageName = tinydb.getListString("imageName");
        urlNames = getResources().getString(R.string.URL_names);

        Object[] imagePathArray = imagePath.toArray();
        Object[] imageNameArray = imageName.toArray();
        Object[] imageGenderArray = imageGender.toArray();

        imageView = findViewById(R.id.downloadedImage);
        String urlImage = "http://10.0.2.2/application/"+imagePathArray[i];
        Glide.with(this).load(urlImage).centerCrop().into(imageView);

        String tempGender = (String) imageGenderArray[i];
        new AsyncTaskExecutor().execute(new NamesAPI(tempGender, urlNames,this), (result) -> {
            randomNames = tinydb.getListString("names");
            Object[] randomNamesArray = randomNames.toArray();

            nameButton1 = findViewById(R.id.nameButton1);
            nameButton2 = findViewById(R.id.nameButton2);
            nameButton3 = findViewById(R.id.nameButton3);
            nameButton4 = findViewById(R.id.nameButton4);

            faceNumber = findViewById(R.id.faceNumber);
            faceNumber.setText(Integer.toString(i+1));

            for (int k = 0; k<4; k++){
                if (randomNamesArray[k].equals(imageNameArray[i])){
                    randomNamesArray[k] = randomNamesArray[6];
                }
            }

            Random r = new Random();
            rightAnswer = r.nextInt(5 - 1) + 1;

            nameButton1.setText((String) randomNamesArray[0]);
            nameButton2.setText((String) randomNamesArray[1]);
            nameButton3.setText((String) randomNamesArray[2]);
            nameButton4.setText((String) randomNamesArray[3]);
            switch (rightAnswer) {
                case 1:
                    nameButton1.setText((String) imageNameArray[i]);
                    break;
                case 2:
                    nameButton2.setText((String) imageNameArray[i]);
                    break;
                case 3:
                    nameButton3.setText((String) imageNameArray[i]);
                    break;
                case 4:
                    nameButton4.setText((String) imageNameArray[i]);
                    break;
            }});
    }

    private void signinAction() {
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        signinButton = findViewById(R.id.loginButton);
        urlService = getResources().getString(R.string.URL_signin);
        signinButton.setOnClickListener(v -> {
            username = String.valueOf(usernameField.getText());
            password = String.valueOf(passwordField.getText());
            if(!username.equals("") && !password.equals("")) {
               new AsyncTaskExecutor().execute(new LoginAPI(username, password, urlService, this), (result) -> didUserSignin(result));
            }
        });
        createAccount = findViewById(R.id.createAccount);

        createAccount.setOnClickListener(v -> {
            setContentView(R.layout.signup_tab_fragment);
            signupAction();
        });
    }

    private void signupAction() {
        usernameFieldSignup = findViewById(R.id.usernameFieldSignup);
        passwordFieldSignup = findViewById(R.id.passwordFieldSignup);
        signupButton = findViewById(R.id.signupButton);
        backButton = findViewById(R.id.signupBack);
        urlService = getResources().getString(R.string.URL_signup);

        signupButton.setOnClickListener(v -> {
            username = String.valueOf(usernameFieldSignup.getText());
            password = String.valueOf(passwordFieldSignup.getText());
            if(!username.equals("") && !password.equals("")) {
                new AsyncTaskExecutor().execute(new SignupAPI(username, password, urlService, this), (result) -> didUserSignup(result));
            }
        });

        backButton.setOnClickListener(v -> {
            setContentView(R.layout.login_tab_fragment);
            signinAction();
            return;
        });
    }

    private void didUserSignup(String result) {
        if (result.equals(getResources().getString(R.string.signup_successfully))){
            setContentView(R.layout.login_tab_fragment);
            signinAction();
        }
        notificationToast(result);
    }

    private void didUserSignin(String result) {
        if (didUserSignin == 1){
            mainMenu();
        }
        notificationToast(result);
    }

    private void notificationToast(String notification){
        Context context = getApplicationContext();
        CharSequence text = notification;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void notificationAlert(String notification){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(notification)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.napis_OK),
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
}