package com.aykow.aube.ble.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.aykow.aube.ble.Callback.GetUserCallback;
import com.aykow.aube.ble.PickerDialogs;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.ServerRequests;
import com.aykow.aube.ble.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etEmail, etFirstName, etLastName, etPassword, etPasswordConfirm, etAddress;
    EditText etBirthDate;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = (EditText) findViewById(R.id.etEmailRegister);
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etPassword = (EditText) findViewById(R.id.etPasswordRegister);
        etPasswordConfirm = (EditText) findViewById(R.id.etPasswordVerifyRegister);
        etAddress = (EditText) findViewById(R.id.etAddress);

        etBirthDate = (EditText) findViewById(R.id.etBirthDate);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(this);

        etBirthDate.setOnClickListener(this);
        etBirthDate.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    hideKeypad();
                    PickerDialogs pickerDialogs = new PickerDialogs(v);
                    pickerDialogs.show(getSupportFragmentManager(),"date_picker");
                }
            }
        });


    }

    private void hideKeypad() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etBirthDate.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.etBirthDate:
                hideKeypad();
                PickerDialogs pickerDialogs = new PickerDialogs(etBirthDate);
                pickerDialogs.show(getSupportFragmentManager(),"date_picker");
                break;

            case R.id.btnRegister:
                String email = etEmail.getText().toString();
                String firstName = etFirstName.getText().toString();
                String lastName = etLastName.getText().toString();
                String password = etPassword.getText().toString();
                String passwordConfirm = etPasswordConfirm.getText().toString();
                String address = etAddress.getText().toString();
                String birthDate = etBirthDate.getText().toString();

                if(firstName.equals("") && lastName.equals("") && !validateEmail(email ) && ( !validatePasswords(password,passwordConfirm)|| (password.length()< 7 || password== null) )){
                    etFirstName.setError(this.getString(R.string.empty_field));
                    etLastName.setError(this.getString(R.string.empty_field));
                    etEmail.setError(this.getString(R.string.error_email));
                    if(!validatePasswords(password,passwordConfirm)){
                        etPassword.setError(this.getString(R.string.passwords_no_match));
                        etPasswordConfirm.setError(this.getString(R.string.passwords_no_match));
                    }
                    else if (password.length()< 7 || password== null){
                        etPassword.setError(this.getString(R.string.password_char_error));
                        etPasswordConfirm.setError(this.getString(R.string.password_char_error));
                    }
                    etFirstName.requestFocus();
                }

                else if(lastName.equals("") && !validateEmail(email ) && ( !validatePasswords(password,passwordConfirm)|| (password.length()< 7 || password== null) )){
                    etLastName.setError(this.getString(R.string.empty_field));
                    etEmail.setError(this.getString(R.string.error_email));
                    if(!validatePasswords(password,passwordConfirm)){
                        etPassword.setError(this.getString(R.string.passwords_no_match));
                        etPasswordConfirm.setError(this.getString(R.string.passwords_no_match));
                    }
                    else if (password.length()< 7 || password== null){
                        etPassword.setError(this.getString(R.string.password_char_error));
                        etPasswordConfirm.setError(this.getString(R.string.password_char_error));
                    }
                    etLastName.requestFocus();
                }
                else if(!validateEmail(email ) && ( !validatePasswords(password,passwordConfirm)|| (password.length()< 7 || password== null) )){
                    etEmail.setError(this.getString(R.string.error_email));
                    if(!validatePasswords(password,passwordConfirm)){
                        etPassword.setError(this.getString(R.string.passwords_no_match));
                        etPasswordConfirm.setError(this.getString(R.string.passwords_no_match));
                    }
                    else if (password.length()< 7 || password== null){
                        etPassword.setError(this.getString(R.string.password_char_error));
                        etPasswordConfirm.setError(this.getString(R.string.password_char_error));
                    }
                    etEmail.requestFocus();
                }

                else if(firstName.equals("")){
                    etFirstName.setError(this.getString(R.string.empty_field));
                    etFirstName.requestFocus();
                    etEmail.setError(this.getString(R.string.error_email));
                }
                else if (lastName.equals("")) {
                    etLastName.setError(this.getString(R.string.empty_field));
                    etLastName.requestFocus();
            }
                else if(!validateEmail(email)) {
                    etEmail.setError(this.getString(R.string.error_email));
                    etEmail.requestFocus();
                }

                else if (password.length()< 7 || password== null){
                    etPassword.setError(this.getString(R.string.password_char_error));
                    etPasswordConfirm.setError(this.getString(R.string.password_char_error));
                    etPassword.requestFocus();
                }
                else if (!validatePasswords(password,passwordConfirm)){
                    etPassword.setError(this.getString(R.string.passwords_no_match));
                    etPasswordConfirm.setError(this.getString(R.string.passwords_no_match));
                    etPassword.requestFocus();
                    etPasswordConfirm.requestFocus();
                }



                else{
                    User user = new User(firstName, lastName, email, password, birthDate, address);
                    registerUser(user);
                }
                break;
        }
    }

    private boolean validateEmail(String email) {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                +"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private boolean validatePasswords(String password, String passwordConfirm) {
        if( password.equals(passwordConfirm))
            return true;
        else
            return false;
    }

    private void registerUser(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeUserDataInBackground(user, new GetUserCallback(){

            @Override
            public void done(User returnedUser) {
            }

            @Override
            public void doneString(String result) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
                if(result!=null)
                {
                    if (result.equals("email already exists"))
                    {
                        etEmail.setText("");
                        etEmail.requestFocus();
                        dialogBuilder.setMessage(R.string.email_exist);
                        dialogBuilder.setPositiveButton(R.string.ok, null);
                        dialogBuilder.show();
                    }
                    else if ( result.equals("UnSuccessful") || result.equals("Error registering"))
                    {
                        dialogBuilder.setMessage(R.string.register_error);
                        dialogBuilder.setPositiveButton(R.string.ok, null);
                        dialogBuilder.show();
                    }
                    else if ( result.equals("successful"))
                    {
                        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                    }
                }
                Log.d("result", result);
            }

        });
    }

}
