package si.uni_lj.fe.seminar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.net.URL;


public class Login extends AppCompatActivity {

    private String username;
    private String password;
    private String urlService;
    private Button signinButtonLogin;
    private Button signupButtonLogin;
    private Button loginButton;
    private Button signupButton;
    EditText usernameField, passwordField, usernameFieldSignup, passwordFieldSignup, cityFieldSignup, countryFieldSignup;
    Login login;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signinButtonLogin = findViewById(R.id.signinButtonLogin);
        signupButtonLogin = findViewById(R.id.signupButtonLogin);

        signinButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.login_tab_fragment);
                usernameField = findViewById(R.id.usernameField);
                passwordField = findViewById(R.id.passwordField);
                signInAction();
            }


        });

        signupButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.signup_tab_fragment);
            }
        });
    }

    private void signInAction() {
        loginButton = findViewById(R.id.loginButton);
        urlService = getResources().getString(R.string.URL_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username, password;
                username = String.valueOf(usernameField.getText());
                password = String.valueOf(passwordField.getText());
                if(!username.equals("") && !password.equals("")) {
                   new AsyncTaskExecutor().execute(new LoginAPI(username, password, urlService, login), (result) -> notificationToast(result));
                }
            }
        });
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