package com.example.moveonotes.views;


import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.moveonotes.R;
import com.example.moveonotes.viewmodel.ForgetPassViewModel;
import com.example.moveonotes.viewmodel.LoginViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText email_et;
    private Button resetPass;
    private ProgressBar progressBar;
    private ForgetPassViewModel forgotPassViewModel;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email_et = findViewById(R.id.pass_et);
        resetPass = findViewById(R.id.btn_pass);
        progressBar = findViewById(R.id.progress_bar_pass);
        forgotPassViewModel = new ViewModelProvider(this).get(ForgetPassViewModel.class);


        auth = FirebaseAuth.getInstance();
        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_et.getText().toString().trim();
                //UI
                if(email.isEmpty()){
                    email_et.setText("");
                    Toast.makeText(getApplicationContext(),"Email Required",Toast.LENGTH_LONG).show();
                    email_et.requestFocus();
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    email_et.setText("");
                    Toast.makeText(getApplicationContext(),"Not a Valid Email",Toast.LENGTH_LONG).show();
                    email_et.requestFocus();
                }
                progressBar.setVisibility(View.VISIBLE);

                //viewmodel

                forgotPassViewModel.resetPass(email);

                if(forgotPassViewModel.resultSent())
                {
                    Toast.makeText(getApplicationContext(),"Email sent!",Toast.LENGTH_LONG).show();
                    email_et.setText("");
                }else{
                    Toast.makeText(getApplicationContext(),"No such User",Toast.LENGTH_LONG).show();


                }
                progressBar.setVisibility(View.GONE);


            }
        });

    }


}