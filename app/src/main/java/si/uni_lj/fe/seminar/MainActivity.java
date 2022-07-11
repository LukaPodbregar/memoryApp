package si.uni_lj.fe.seminar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String token, selectedImageName, imageBase64;
    static int didUserSignin;
    private TextView faceNumber, rightAnswerCount, wrongAnswerCount;
    private ImageView imageView, imageUploadPreview;
    private int SELECT_PICTURE = 200;
    private int pageNumber, rightAnswerCounter, wrongAnswerCounter, pageNumberLibrary, gameLength, rightAnswer, imagesPerPage = 10;
    public static Context contextOfApplication;
    private ArrayList imagePath, imageGender, imageName;

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

    // Main menu function
    private void mainMenu() {
        setContentView(R.layout.activity_main);
        ImageView signoutButton = findViewById(R.id.signoutButton);
        Button startButton = findViewById(R.id.startGame), newFaceButton = findViewById(R.id.addNewFaces), faceLibrary = findViewById(R.id.myFaces);

        // Start game button
        startButton.setOnClickListener(v -> startGame());

        // Library button
        faceLibrary.setOnClickListener(v -> library());

        // Upload new Image (Face) button
        newFaceButton.setOnClickListener(v -> faceUpload());

        // Sign out button -> return to sign in view
        signoutButton.setOnClickListener(v -> {
            setContentView(R.layout.login_tab_fragment);
            didUserSignin = 0;
            signinAction();
        });
    }

    // Upload new Image function
    private void faceUpload(){
        setContentView(R.layout.new_face_upload);

        Button uploadButton = findViewById(R.id.uploadButton);
        EditText uploadNameField = findViewById(R.id.uploadNameField);
        RadioGroup uploadGenderRadio = findViewById(R.id.radioGroupGender);
        ImageView uploadBackButton = findViewById(R.id.uploadBack), imageUploadSelectImageButton = findViewById(R.id.imageUploadSelectImage);

        // Back button -> return to main menu
        uploadBackButton.setOnClickListener(v -> mainMenu());

        // Select Image to upload from gallery
        imageUploadSelectImageButton.setOnClickListener(v -> openGallery());

        // Upload Image
        uploadButton.setOnClickListener(v -> {
            String uploadGender, uploadName = String.valueOf(uploadNameField.getText());
            int checkedGender = uploadGenderRadio.getCheckedRadioButtonId();

            // Check if gender is selected
            if (checkedGender != -1) {
                RadioButton selectedRadioButton = uploadGenderRadio.findViewById(checkedGender);
                String selectedText = (String) selectedRadioButton.getText();

                // Check what kind of gender is selected
                if (selectedText.equals("M")){
                    uploadGender = "male";
                }
                else {
                    uploadGender = "female";
                }

                // Check if name of the Image is input
                if (!uploadName.equals("")) {
                    Context applicationContext = MainActivity.getContextOfApplication();
                    TinyDB tinydb = new TinyDB(applicationContext);
                    token = tinydb.getString("token");
                    String urlServiceImagesUpload = getResources().getString(R.string.URL_images_upload);

                    // Upload image via ImageUploadAPI
                    new AsyncTaskExecutor().execute(new ImageUploadAPI(token, selectedImageName, imageBase64, uploadGender, uploadName, urlServiceImagesUpload, this), (result) -> imageUpload(result));
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

    // Function that checks if image was uploaded successfully and resets the contentView to allow another upload
    private void imageUpload(String result) {
        String imageUploadedSuccessfully = getResources().getString(R.string.image_upload_successful);
        EditText uploadNameField = findViewById(R.id.uploadNameField);
        RadioGroup uploadGenderRadio = findViewById(R.id.radioGroupGender);

        if (result.equals(imageUploadedSuccessfully)){
            notificationToast(imageUploadedSuccessfully);
            uploadGenderRadio.clearCheck();
            uploadNameField.setText("");
            imageUploadPreview.setImageResource(0);
        }
        else {
            notificationToast(getResources().getString(R.string.image_upload_error));
        }
    }

    // Function that allows Image selection from phone gallery
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
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {

                    // Return selected Image data
                    imageUploadPreview = findViewById(R.id.imageUploadPreview);
                    imageUploadPreview.setImageURI(selectedImageUri);
                    selectedImageName = getRealNameFromURI(selectedImageUri);
                    Context applicationContext = getContextOfApplication();
                    try {
                        // Convert selected Image to base64 format to allow Image upload
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), selectedImageUri);
                        imageBase64 = ConvertBitmapToBase64Format(imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Function that converts Bitmap Image to Base64 format
    private String ConvertBitmapToBase64Format(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }

    // Function that gets images real name from URI
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

    // Library function which allows you to browse your uploaded Images
    private void library() {
        setContentView(R.layout.library_selection);

        Context applicationContext = MainActivity.getContextOfApplication();
        String urlServiceImages;
        urlServiceImages = getResources().getString(R.string.URL_images);
        ImageView libraryBackButton = findViewById(R.id.libraryBack), libraryNextPageButton = findViewById(R.id.libraryNextPage);
        pageNumberLibrary = 0;
        TinyDB tinydb = new TinyDB(applicationContext);
        token = tinydb.getString("token");

        // Retrieve Images from database via ImagesAPI
        new AsyncTaskExecutor().execute(new ImagesAPI(token, urlServiceImages, this), (result) -> loadLibrary(tinydb, pageNumberLibrary));

        // Back button -> return to Main menu
        libraryBackButton.setOnClickListener(v -> mainMenu());

        imagePath = tinydb.getListString("imagePath");
        Object[] imagePathArray = imagePath.toArray();

        double imageArrayLength = imagePathArray.length, imagesPerPage2 = imagesPerPage;

        // Calculate maximum number of pages (maximum Images per page is 10)
        double maximumNumberOfPages = Math.ceil(imageArrayLength / imagesPerPage2)-1;

        // Next page button
        libraryNextPageButton.setOnClickListener(v -> {
            // Check if this isn't the last page
            if (pageNumberLibrary < maximumNumberOfPages) {
                // Count up page number
                pageNumberLibrary = pageNumberLibrary + 1;
                // Load new page of Images
                loadLibrary(tinydb, pageNumberLibrary);
            }
        });

        // Previous page button
        ImageView libraryPreviousPageButton = findViewById(R.id.libraryPreviousPage);
        libraryPreviousPageButton.setOnClickListener(v -> {
            // Check if this isn't the last page
            if (pageNumberLibrary != 0) {
                // Count down page number
                pageNumberLibrary = pageNumberLibrary - 1;
                // Load new page of Images
                loadLibrary(tinydb, pageNumberLibrary);
            }
        });

    }

    // Function that prepares
    private void loadLibrary(TinyDB tinydb, int pageNumberLibrary) {
        // Get String lists from database
        imagePath = tinydb.getListString("imagePath");
        imageGender = tinydb.getListString("imageGender");
        imageName = tinydb.getListString("imageName");

        // String list to array
        Object[] imagePathArray = imagePath.toArray();
        Object[] imageNameArray = imageName.toArray();
        Object[] imageGenderArray = imageGender.toArray();

        // ImageView array init which allows selection of certain Images
        ImageView[] imageViews = new ImageView[imagesPerPage];

        // Save imageView values into ImageView array
        for (int j = 0; j < imagesPerPage; j++) {
            String viewImage = "libraryImage" + j;
            int resIDImage = getResources().getIdentifier(viewImage, "id", getPackageName());
            imageViews[j] = ((ImageView) findViewById(resIDImage));
        }

        // Load new page of Images
        newImagePageLibrary(imagePathArray, imageViews, pageNumberLibrary, imageNameArray, imageGenderArray);
    }

    // Function that loads new page of Images in library
    private void newImagePageLibrary(Object[] imagePathArray, ImageView[] imageViews, int pageNumberLibrary, Object[] imageNameArray, Object[] imageGenderArray) {
        // There could be less than 10 Images uploaded for this user or for certain page
        for (int k = (pageNumberLibrary * imagesPerPage); k < (imagesPerPage + pageNumberLibrary * imagesPerPage); k++) {
            if (k < imagePathArray.length){
                // Image data, saved in arrays imageNameArray, imagePathArray, imageGenderArray could be numbered higher than 10, while there are only 10 imageViews
                int j = k-(pageNumberLibrary * 10);

                // Glide image into imageView
                String urlImage = "http://10.0.2.2/application/" + imagePathArray[k];
                Glide.with(this).load(urlImage).centerCrop().into(imageViews[j]);

                // Get current Image data
                String currentImageName = (String) imageNameArray[k];
                String currentImageGender = (String) imageGenderArray[k];
                String currentImagePath = (String) imagePathArray[k];
                int num = k;

                // Set onClickListener for every imageView. This allows changing Image data
                imageViews[j].setOnClickListener(v -> {
                    // Change Image data
                    libraryImageSettings(currentImageName, currentImageGender, currentImagePath, num);
                });
            }
            else{
                // If there are <10 Images on this page -> clear imageView
                imageViews[(k-(pageNumberLibrary * imagesPerPage))].setImageResource(0);
            }
        }
    }

    // Function that allows changing of Image data (name, gender) or even deletion
    private void libraryImageSettings(String imageName, String imageGender, String currentImagePath, int k){
        setContentView(R.layout.image_settings_library);

        // Get selected Image data
        ImageView imageSettingsImageView = findViewById(R.id.imageSettingImage);
        RadioGroup imageSettingsGenderRadio = findViewById(R.id.imageSettingRadioGroupGender);
        EditText imageSettingName = findViewById(R.id.imageSettingName);

        // Set radio group to the correct value
        if (imageGender.equals("male")){
            imageSettingsGenderRadio.check(R.id.imageSettingRadioGroupMale);
        }
        else if (imageGender.equals("female")){
            imageSettingsGenderRadio.check(R.id.imageSettingRadioGroupFemale);
        }

        // Set selected Image name
        imageSettingName.setText((String) imageName);

        // Disable radio group selection if edit button not selected
        for (int i = 0; i < imageSettingsGenderRadio.getChildCount(); i++) {
            imageSettingsGenderRadio.getChildAt(i).setEnabled(false);
        }

        // Disable name EditText if edit button not selected
        imageSettingName.setEnabled(false);

        // Glide selected image into ImageView
        String urlImage = "http://10.0.2.2/application/" + currentImagePath;
        Glide.with(this).load(urlImage).centerCrop().into(imageSettingsImageView);

        // Back button returns to Library
        ImageView librarySettingsBackButton = findViewById(R.id.imageSettingBack);
        librarySettingsBackButton.setOnClickListener(v -> library());

        // Edit button allows Image data edit
        Button imageSettingEditButton = findViewById(R.id.imageSettingEditButton);
        imageSettingEditButton.setOnClickListener(v -> imageSettingsEdit(imageName, imageGender, currentImagePath, k));
    }

    // Function that allows user to change Image data
    private void imageSettingsEdit(String imageName, String imageGender, String currentImagePath, int k) {
        setContentView(R.layout.image_settings_library_edit);

        ImageView imageSettingsImageView = findViewById(R.id.imageSettingEditImage);
        RadioGroup imageSettingsGenderRadio = findViewById(R.id.imageSettingEditRadioGroupGender);
        EditText imageSettingName = findViewById(R.id.imageSettingEditName);
        Context applicationContext = MainActivity.getContextOfApplication();
        String urlServiceImages = getResources().getString(R.string.URL_images);
        TinyDB tinydb = new TinyDB(applicationContext);

        // Again preset the selected Image data
        if (imageGender.equals("male")){
            imageSettingsGenderRadio.check(R.id.imageSettingEditRadioGroupMale);
        }
        else if (imageGender.equals("female")){
            imageSettingsGenderRadio.check(R.id.imageSettingEditRadioGroupFemale);
        }
        imageSettingName.setText((String) imageName);

        // Glide selected Image into ImageView
        String urlImage = "http://10.0.2.2/application/" + currentImagePath;
        Glide.with(this).load(urlImage).centerCrop().into(imageSettingsImageView);

        // If the Upload button is selected -> upload input Image data
        Button librarySettingUploadButton = findViewById(R.id.imageSettingUploadButton);
        librarySettingUploadButton.setOnClickListener(v -> {
            // Get name value from EditText
            String imageSettingEditText = String.valueOf(imageSettingName.getText());
            int checkedGender = imageSettingsGenderRadio.getCheckedRadioButtonId();
            // Check if gender is selected
            if (checkedGender != -1) {
                // Get selected gender value
                RadioButton selectedRadioButton = imageSettingsGenderRadio.findViewById(checkedGender);
                String selectedText = (String) selectedRadioButton.getText(), imageSettingEditGender, urlServiceImagesUpdate = getResources().getString(R.string.URL_images_update);
                if (selectedText.equals("M")){
                    imageSettingEditGender = "male";
                }
                else {
                    imageSettingEditGender = "female";
                }
                // Check if name is not empty
                if (!imageSettingEditText.equals("")) {
                    token = tinydb.getString("token");

                    // Update image data via ImageUpadateAPI
                    new AsyncTaskExecutor().execute(new imageUpdateAPI(token, imageSettingEditText, imageSettingEditGender, currentImagePath, urlServiceImagesUpdate, this), (result) -> {
                        notificationToast(result);

                        // Get updated Image data via ImagesAPI
                        new AsyncTaskExecutor().execute(new ImagesAPI(token, urlServiceImages, this), (result2) -> {
                            List updatedImageList = getUpdatedImage(k);
                            String imageNameUpdatedString = (String) updatedImageList.get(0);
                            String imageGenderUpdatedString = (String) updatedImageList.get(1);
                            String imagePathUpdatedString = (String) updatedImageList.get(2);

                            // Return to locked Image settings view with updated data
                            libraryImageSettings(imageNameUpdatedString, imageGenderUpdatedString, imagePathUpdatedString, k);
                        });
                    });
                }
                // If gender/name is not selected return error
                else {
                    notificationToast(getResources().getString(R.string.pleaseSelectAllParameters));
                }
            }
            else{
                notificationToast(getResources().getString(R.string.pleaseSelectAllParameters));
            }
        });

        // Back button gets old image data via ImagesAPI and returns to locked Image settings view
        ImageView librarySettingsBackButton = findViewById(R.id.imageSettingEditBack);
        librarySettingsBackButton.setOnClickListener(v -> {
            token = tinydb.getString("token");
            new AsyncTaskExecutor().execute(new ImagesAPI(token, urlServiceImages, this), (result) -> {
                List updatedImageList = getUpdatedImage(k);
                String imageNameUpdatedString = (String) updatedImageList.get(0);
                String imageGenderUpdatedString = (String) updatedImageList.get(1);
                String imagePathUpdatedString = (String) updatedImageList.get(2);
                libraryImageSettings(imageNameUpdatedString, imageGenderUpdatedString, imagePathUpdatedString, k);
            });
        });

        // Delete button allows deletion of Images, when pressed -> user is asked for confirmation
        ImageView librarySettingsDeleteButton = findViewById(R.id.imageSettingEditDelete);
        librarySettingsDeleteButton.setOnClickListener(v -> {
            String urlServiceImagesUpdate = getResources().getString(R.string.URL_images_delete);
            confirmationAlert(token, currentImagePath, urlServiceImagesUpdate, this);
        });
    }

    // Function asks user for confirmation for Image deletion
    private void confirmationAlert(String token, String imagePath, String urlService, Activity callerActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.confirmation))
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                })
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If user confirms deletion -> delete image from database via imageDeleteAPI, give notification of deletion status and return to library
                        new AsyncTaskExecutor().execute(new imageDeleteAPI(token, imagePath, urlService, callerActivity), (result) -> {
                            notificationToast(result);
                            library();
                        });
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Return updated image list
    private static List<Object> getUpdatedImage(int k){
        Context applicationContext = getContextOfApplication();
        TinyDB tinydb = new TinyDB(applicationContext);
        List imageGenderUpdated = tinydb.getListString("imageGender");
        List imageNameUpdated = tinydb.getListString("imageName");
        List imagePathUpdated = tinydb.getListString("imagePath");

        Object[] imageNameArray = imageNameUpdated.toArray();
        Object[] imageGenderArray = imageGenderUpdated.toArray();
        Object[] imagePathArray = imagePathUpdated.toArray();

        String imageNameUpdatedString = (String) imageNameArray[k];
        String imageGenderUpdatedString = (String) imageGenderArray[k];
        String imagePathUpdatedString = (String) imagePathArray[k];

        return Arrays.asList(imageNameUpdatedString, imageGenderUpdatedString, imagePathUpdatedString);
    }

    // Function that is called when new game is started. It gets Image data via ImagesAPI
    private void startGame() {
        Context applicationContext = MainActivity.getContextOfApplication();
        TinyDB tinydb = new TinyDB(applicationContext);
        token = tinydb.getString("token");
        String urlServiceImages = getResources().getString(R.string.URL_images_random);
        // Get Image data and start new game
        new AsyncTaskExecutor().execute(new ImagesAPI(token, urlServiceImages, this), (result) -> game());
    }

    // Function is logic behind the actual game
    private void game(){
        // Reset values for new game to start
        pageNumber = 0;
        rightAnswerCounter = 0;
        wrongAnswerCounter = 0;
        // Game length is defaulted at 10
        gameLength = 10;

        // Get Image path list and convert it to array
        Context applicationContext = MainActivity.getContextOfApplication();
        TinyDB tinydb = new TinyDB(applicationContext);
        imagePath = tinydb.getListString("imagePath");
        Object[] imagePathArray = imagePath.toArray();

        // If there are no Images uploaded -> return error
        if (imagePathArray.length != 0) {
            setContentView(R.layout.game_main);

            // If user has <10 Images uploaded -> shorten the game
            if (imagePathArray.length < gameLength) {
                gameLength = imagePathArray.length;
            }

            // Calls function that loads new face
            loadNewFace(pageNumber, applicationContext);

            Button nameButton1 = findViewById(R.id.nameButton1), nameButton2 = findViewById(R.id.nameButton2), nameButton3 = findViewById(R.id.nameButton3), nameButton4 = findViewById(R.id.nameButton4);

            // Right answer is labeled with a number 1-4, if the button contains the right answer and is clicked -> call rightAnswer function
            // else -> call wrongAnswer function. After any button is clicked -> load new Face or end the game (afterGameSummary) if the Face was the last one
            nameButton1.setOnClickListener(v -> {
                if (rightAnswer == 1) {
                    rightAnswer();
                } else {
                    wrongAnswer();
                }
                imagePath = tinydb.getListString("imagePath");
                if (pageNumber < gameLength) {
                    loadNewFace(pageNumber, applicationContext);
                } else {
                    SystemClock.sleep(500);
                    afterGameSummary();
                }
            });

            nameButton2.setOnClickListener(v -> {
                if (rightAnswer == 2) {
                    rightAnswer();
                } else {
                    wrongAnswer();
                }
                if (pageNumber < gameLength) {
                    loadNewFace(pageNumber, applicationContext);
                } else {
                    SystemClock.sleep(500);
                    afterGameSummary();
                }
            });

            nameButton3.setOnClickListener(v -> {
                if (rightAnswer == 3) {
                    rightAnswer();
                } else {
                    wrongAnswer();
                }
                if (pageNumber < gameLength) {
                    loadNewFace(pageNumber, applicationContext);
                } else {
                    SystemClock.sleep(500);
                    afterGameSummary();
                }
            });

            nameButton4.setOnClickListener(v -> {
                if (rightAnswer == 4) {
                    rightAnswer();
                } else {
                    wrongAnswer();
                }
                if (pageNumber < gameLength) {
                    loadNewFace(pageNumber, applicationContext);
                } else {
                    SystemClock.sleep(500);
                    afterGameSummary();
                }
            });
        }
        else {
            notificationToast(getResources().getString(R.string.noFacesUploadedError));
            mainMenu();
        }
    }

    // Function displays After game summary, it sets the numbers to the number of correct and wrong answers
    @SuppressLint("SetTextI18n")
    private void afterGameSummary(){
        setContentView(R.layout.after_game_report);
        wrongAnswerCount = findViewById(R.id.wrongAnswerCountSummary);
        wrongAnswerCount.setText(Integer.toString(wrongAnswerCounter));
        rightAnswerCount = findViewById(R.id.rightAnswerCountSummary);
        rightAnswerCount.setText(Integer.toString(rightAnswerCounter));
        ImageView summaryBackButton = findViewById(R.id.summaryBack);

        // Back button -> return to Main menu
        summaryBackButton.setOnClickListener(v -> mainMenu());
    }

    // Function is called if the right answer was selected
    @SuppressLint("SetTextI18n")
    private void rightAnswer(){
        // Notify the user that the right answer was selected
        notificationToast(getResources().getString(R.string.rightAnswerNotification));
        // Upcount the page number
        pageNumber = pageNumber+1;
        // Upcount the right answer counter
        rightAnswerCounter = rightAnswerCounter+1;
        rightAnswerCount = findViewById(R.id.rightAnswerCount);
        // Update the right answer counter
        rightAnswerCount.setText(Integer.toString(rightAnswerCounter));
        SystemClock.sleep(500);
    }

    // Function is called if the wrong answer was selected
    @SuppressLint("SetTextI18n")
    private void wrongAnswer(){
        // Notify the user that the wrong answer was selected
        notificationToast(getResources().getString(R.string.wrongAnswerNotification));
        // Upcount the page number
        pageNumber = pageNumber+1;
        // Upcount the right wrong counter
        wrongAnswerCounter = wrongAnswerCounter+1;
        wrongAnswerCount = findViewById(R.id.wrongAnswerCount);
        // Update the wrong answer counter
        wrongAnswerCount.setText(Integer.toString(wrongAnswerCounter));
        SystemClock.sleep(500);
    }

    // Function loads new Image
    @SuppressLint("SetTextI18n")
    private void loadNewFace(int i, Context applicationContext){
        // Get Image data from database
        TinyDB tinydb = new TinyDB(applicationContext);
        imagePath = tinydb.getListString("imagePath");
        imageGender = tinydb.getListString("imageGender");
        imageName = tinydb.getListString("imageName");
        String urlNames = getResources().getString(R.string.URL_names);

        // Convert String list to array
        Object[] imagePathArray = imagePath.toArray();
        Object[] imageNameArray = imageName.toArray();
        Object[] imageGenderArray = imageGender.toArray();

        // Glide image into ImageView
        imageView = findViewById(R.id.downloadedImage);
        String urlImage = "http://10.0.2.2/application/"+imagePathArray[i];
        Glide.with(this).load(urlImage).centerCrop().into(imageView);

        // Get gender of the current Image
        String tempGender = (String) imageGenderArray[i];
        // Return 10 random names of current Image gender from randomnames database via NamesAPI
        new AsyncTaskExecutor().execute(new NamesAPI(tempGender, urlNames,this), (result) -> {
            ArrayList randomNames = tinydb.getListString("names");
            Object[] randomNamesArray = randomNames.toArray();

            Button nameButton1 = findViewById(R.id.nameButton1);
            Button nameButton2 = findViewById(R.id.nameButton2);
            Button nameButton3 = findViewById(R.id.nameButton3);
            Button nameButton4 = findViewById(R.id.nameButton4);

            faceNumber = findViewById(R.id.faceNumber);
            faceNumber.setText(Integer.toString(i+1));

            // Check if any of the names equal the correct answer, if so -> use another one in its place (we have 10 random names and we only use first 4)
            for (int k = 0; k<4; k++){
                if (randomNamesArray[k].equals(imageNameArray[i])){
                    randomNamesArray[k] = randomNamesArray[6];
                }
            }

            // AGenerate a random number between 1-4 and use that button to assign the right answer
            Random r = new Random();
            rightAnswer = r.nextInt(5 - 1) + 1;

            nameButton1.setText((String) randomNamesArray[0]);
            nameButton2.setText((String) randomNamesArray[1]);
            nameButton3.setText((String) randomNamesArray[2]);
            nameButton4.setText((String) randomNamesArray[3]);

            // Assign the right answer to one of the 4 random names
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

    // Function allows user to sign in
    private void signinAction() {
        EditText usernameField = findViewById(R.id.usernameField), passwordField = findViewById(R.id.passwordField);
        Button signinButton = findViewById(R.id.loginButton);
        String urlService = getResources().getString(R.string.URL_signin);

        // If Sign in button is pressed -> Check if the username and password fields are not empty -> try to login via LoginAPI
        signinButton.setOnClickListener(v -> {
            String username = String.valueOf(usernameField.getText()), password = String.valueOf(passwordField.getText());
            if(!username.equals("") && !password.equals("")) {
                new AsyncTaskExecutor().execute(new Signin(username, password, urlService, this), (result) ->
                        // Check if user signed in successfully
                        didUserSignin(result));
            }
        });

        // Sign up button allows user to sign up
        TextView createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(v -> {
            setContentView(R.layout.signup_tab_fragment);
            signupAction();
        });
    }

    // Function allows user to sign up via SignupAPI
    private void signupAction() {
        EditText usernameFieldSignup = findViewById(R.id.usernameFieldSignup), passwordFieldSignup = findViewById(R.id.passwordFieldSignup);
        Button signupButton = findViewById(R.id.signupButton);
        ImageView backButton = findViewById(R.id.signupBack);
        String urlService = getResources().getString(R.string.URL_signup);

        signupButton.setOnClickListener(v -> {
            String username = String.valueOf(usernameFieldSignup.getText()), password = String.valueOf(passwordFieldSignup.getText());
            if(!username.equals("") && !password.equals("")) {
                new AsyncTaskExecutor().execute(new SignupAPI(username, password, urlService, this), (result) ->
                        // Check if user signed up successfully
                        didUserSignup(result));
            }
        });

        // Back button -> return to sign in view
        backButton.setOnClickListener(v -> {
            setContentView(R.layout.login_tab_fragment);
            signinAction();
        });
    }

    // Function checks if user signed up successfully
    private void didUserSignup(String result) {
        if (result.equals(getResources().getString(R.string.signup_successfully))){
            // Return to sign in view
            setContentView(R.layout.login_tab_fragment);
            signinAction();
        }
        notificationToast(result);
    }

    // Function checks if user signed in successfully
    private void didUserSignin(String result) {
        if (didUserSignin == 1){
            mainMenu();
        }
        notificationToast(result);
    }

    // Function allows to show notification toast
    private void notificationToast(String notification){
        Context context = getApplicationContext();
        CharSequence text = notification;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // Function returns context of application
    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
}