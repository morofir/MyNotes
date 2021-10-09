package com.example.moveonotes.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.moveonotes.ForgotPasswordActivity;
import com.example.moveonotes.R;
import com.example.moveonotes.viewmodel.LoginViewModel;
import com.example.moveonotes.viewmodel.RegisterViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText emailEt, passEt;
    Button loginButton, RegisterButton;
    TextView forgotPasswordTv;
    CheckBox showPassword;
    ProgressBar progressBar;
    FirebaseAuth auth;
    private LoginViewModel loginViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initiateView();
        auth = FirebaseAuth.getInstance();


        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);


        //set up the observers

        loginViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {

                if (firebaseUser != null) {
                    sendToMain();
                }
            }
        });



        showPassword.setChecked(false);
        passEt.setTransformationMethod(PasswordTransformationMethod.getInstance()); //first will show password as ***

        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            } else {
                passEt.setTransformationMethod(PasswordTransformationMethod.getInstance());


            }
        });

        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));

            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


        //if Enter pressed will login
        passEt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = emailEt.getText().toString();
                String pass = passEt.getText().toString();

                loginViewModel.loginUser(email,pass);

            }
        });
    }

    private void sendToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void initiateView() {
        showPassword = findViewById(R.id.showpass_cb);
        emailEt = findViewById(R.id.login_email_et);
        passEt = findViewById(R.id.login_pass_et);
        forgotPasswordTv= findViewById(R.id.forgot_tv);
        RegisterButton = findViewById(R.id.login_to_signup);
        loginButton = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progressbar_login);

    }

    private void sendToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}