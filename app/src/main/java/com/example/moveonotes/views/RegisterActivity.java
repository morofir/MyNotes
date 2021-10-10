package com.example.moveonotes.views;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.moveonotes.R;
import com.example.moveonotes.viewmodel.LoginViewModel;
import com.example.moveonotes.viewmodel.RegisterViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    EditText emailEt,passEt,confirmEt;
    Button registerBtn,loginBtn;
    CheckBox checkBox;
    ProgressBar progressBar;
    FirebaseAuth auth;
    RegisterViewModel registerViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initiateView();
        auth = FirebaseAuth.getInstance();

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        //set up the observer
        registerViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    //todo
                    Toast.makeText(getApplicationContext(), "User Created", Toast.LENGTH_SHORT).show();
                    sendToMain();
                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    passEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
            }
        });
        //if Enter pressed will login (in confirm password only)
        confirmEt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    registerBtn.performClick();
                    return true;
                }
                return false;
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString();
                String pass = passEt.getText().toString();
                String confirm = confirmEt.getText().toString();

                registerViewModel.register(email,pass,confirm); //will create user node if adding new note

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void initiateView() {
        emailEt = findViewById(R.id.register_email_et);
        passEt = findViewById(R.id.register_pass_et);
        confirmEt = findViewById(R.id.confirm_pass_et);
        registerBtn = findViewById(R.id.register_btn);
        loginBtn = findViewById(R.id.signup_to_login);
        checkBox = findViewById(R.id.register_cb);
        progressBar = findViewById(R.id.progressbar_register);

    }

    private void sendToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            sendToMain();
        }
    }}