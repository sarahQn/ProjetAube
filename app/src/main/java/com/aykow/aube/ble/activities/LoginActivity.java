package com.aykow.aube.ble.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aykow.aube.ble.Callback.GetUserCallback;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.ServerRequests;
import com.aykow.aube.ble.User;
import com.aykow.aube.ble.UserLocalStore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvRegister, tvForget;
    Button btnLogin;
    EditText etEmail, etPassword;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvRegister = (TextView) findViewById(R.id.tvRegister);
        tvForget = (TextView) findViewById(R.id.tvForgot);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForget.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);

    }

    public void onClick(View v){
        switch (v.getId()){

            case R.id.btnLogin:
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if(!validateEmail(email) &&  !validatePassword(password)){
                    etEmail.setError(this.getString(R.string.error_email));
                    etPassword.setError(this.getString(R.string.error_password));
                    etEmail.requestFocus();
                }
                else if (!validateEmail(email)){
                    etEmail.setError(this.getString(R.string.error_email));
                    etEmail.requestFocus();
                }else if(!validatePassword(password)){
                    etPassword.setError(this.getString(R.string.error_password));
                    etPassword.requestFocus();
                }
                else{
                    Log.d("Part1", "clicked on send btn and everything is oki");
                    User user = new User(email,password);
                    authenticate(user);
                }

                break;

            case R.id.tvRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                break;

            case R.id.tvForgot:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
        }
    }

    private boolean validateEmail(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                +"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private boolean validatePassword(String password) {
        if(password!=null && password.length()> 7){
            return true;
        }
        else{
            return false;
        }

    }

    private void authenticate(User user){

        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null) {
                    showErrorMessage();
                } else {
                    logUserIn(returnedUser);
                }
            }

            @Override
            public void doneString(String result) {

            }
        });
    }

    private void logUserIn(User returnedUser) {

            userLocalStore.storeUserData(returnedUser);
            userLocalStore.setIsUserLoggedIn(true);
            startActivity(new Intent(this, InterMainActivity.class));
    }

    private void showErrorMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        dialogBuilder.setMessage(R.string.incorrect_user_details);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.show();
    }




}