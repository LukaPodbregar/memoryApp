package si.uni_lj.fe.seminar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private String username, password, urlService;
    private Button signinButtonLogin, signupButtonLogin, signinButton, signupButton;
    EditText usernameField, passwordField, usernameFieldSignup, passwordFieldSignup;
    Login login;
    static int didUserSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.login = this;

        if (didUserSignin == 0) {
            setContentView(R.layout.activity_login);
            signinButtonLogin = findViewById(R.id.signinButtonLogin);
            signupButtonLogin = findViewById(R.id.signupButtonLogin);

            signinButtonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setContentView(R.layout.login_tab_fragment);
                    signinAction();
                }
            });

            signupButtonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setContentView(R.layout.signup_tab_fragment);
                    signupAction();
                }
            });
        }
        else {
            setContentView(R.layout.activity_main);
        }

    }


    // Functions

    private void signinAction() {
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        signinButton = findViewById(R.id.loginButton);
        urlService = getResources().getString(R.string.URL_signin);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = String.valueOf(usernameField.getText());
                password = String.valueOf(passwordField.getText());
                if(!username.equals("") && !password.equals("")) {
                   new AsyncTaskExecutor().execute(new LoginAPI(username, password, urlService, login), (result) -> didUserSignin(result));
                }
            }
        });
    }

    private void signupAction() {
        usernameFieldSignup = findViewById(R.id.usernameFieldSignup);
        passwordFieldSignup = findViewById(R.id.passwordFieldSignup);
        signupButton = findViewById(R.id.signupButton);
        urlService = getResources().getString(R.string.URL_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = String.valueOf(usernameFieldSignup.getText());
                password = String.valueOf(passwordFieldSignup.getText());
                if(!username.equals("") && !password.equals("")) {
                    new AsyncTaskExecutor().execute(new SignupAPI(username, password, urlService, login), (result) -> didUserSignup(result));
                }
            }
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
            setContentView(R.layout.activity_main);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(login);
        builder.setMessage(notification)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.napis_OK),
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}