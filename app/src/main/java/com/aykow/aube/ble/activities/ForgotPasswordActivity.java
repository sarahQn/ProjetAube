package com.aykow.aube.ble.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aykow.aube.ble.Callback.GetSendEmailCallback;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.ServerRequests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSend;
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        btnSend = (Button) findViewById(R.id.btnSend);
        etEmail = (EditText) findViewById(R.id.etEmailForgot);

        btnSend.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnSend:
                String email = etEmail.getText().toString();

                if (!validateEmail(email)) {
                    etEmail.setError(this.getString(R.string.error_email));
                    etEmail.requestFocus();
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                    dialogBuilder.setMessage(R.string.msg_send);
                    dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        }
                    });
                    dialogBuilder.setTitle("PASSWORD");
                    dialogBuilder.show();

                    sendResetPassword(email);
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

    private void sendResetPassword(String email){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.sendEmailInBackground(email, new GetSendEmailCallback() {

            @Override
            public void doneResultString(String result) {


            }
        });
    }
}
