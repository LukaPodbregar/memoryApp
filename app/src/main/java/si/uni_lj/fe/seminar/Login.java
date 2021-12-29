package si.uni_lj.fe.seminar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class Login extends AppCompatActivity {

    private String username, password, urlService, urlServiceImages, token, urlNames;
    private TextView createAccount;
    private Button signinButton, signupButton, startButton, nameButton1, nameButton2, nameButton3, nameButton4;
    private ImageView backButton, signoutButton, imageView;
    EditText usernameField, passwordField, usernameFieldSignup, passwordFieldSignup;
    static int didUserSignin, rightAnswer;
    public static Context contextOfApplication;
    TinyDB tinydb;
    ArrayList imagePath, imageGender, imageName, randomNames;


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

        signoutButton.setOnClickListener(v -> {
            setContentView(R.layout.login_tab_fragment);
            didUserSignin = 0;
            signinAction();
        });
        startButton.setOnClickListener(v -> {
            game();
        });
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
        int i = 0;
        Context applicationContext = Login.getContextOfApplication();
        loadNewFace(i, applicationContext);

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
        String urlTest = "http://10.0.2.2/application/"+imagePathArray[i];
        Glide.with(this).load(urlTest).into(imageView);

        String tempGender = (String) imageGenderArray[i];
        new AsyncTaskExecutor().execute(new NamesAPI(tempGender, urlNames,this), (result) -> {});
        randomNames = tinydb.getListString("names");
        Object[] randomNamesArray = randomNames.toArray();

        nameButton1 = findViewById(R.id.nameButton1);
        nameButton2 = findViewById(R.id.nameButton2);
        nameButton3 = findViewById(R.id.nameButton3);
        nameButton4 = findViewById(R.id.nameButton4);

        Random r = new Random();
        rightAnswer = r.nextInt(5 - 1) + 1;

        nameButton1.setText((String) randomNamesArray[1]);
        nameButton2.setText((String) randomNamesArray[2]);
        nameButton3.setText((String) randomNamesArray[3]);
        nameButton4.setText((String) randomNamesArray[4]);
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
        }
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