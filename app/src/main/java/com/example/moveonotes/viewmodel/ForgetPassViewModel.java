package com.example.moveonotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.moveonotes.model.ForgetPassRepository;
import com.example.moveonotes.model.LoginAppRepository;
import com.example.moveonotes.views.ForgotPasswordActivity;

public class ForgetPassViewModel extends AndroidViewModel {
    ForgetPassRepository forgetPassRepository;
    Boolean sent;

    public ForgetPassViewModel(@NonNull Application application) {
        super(application);
        forgetPassRepository = new ForgetPassRepository(application);
    }

    public void resetPass(String email) {
        sent = forgetPassRepository.resetPass(email);
    }
    public boolean resultSent(){
        return sent;
    }
}
