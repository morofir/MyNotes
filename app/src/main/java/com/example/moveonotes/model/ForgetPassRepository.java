package com.example.moveonotes.model;

import android.app.Application;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPassRepository {
    private FirebaseAuth auth;
    private boolean sent = false;


    public ForgetPassRepository(Application application) {
        auth = FirebaseAuth.getInstance();
    }


    public Boolean resetPass(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {


            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sent = task.isSuccessful();
            }
        });
        return sent;
    }
}
