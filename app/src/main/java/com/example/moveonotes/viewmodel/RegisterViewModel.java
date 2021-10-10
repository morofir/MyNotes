package com.example.moveonotes.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.moveonotes.model.AppRepository;
import com.example.moveonotes.views.LoginActivity;
import com.example.moveonotes.views.MainActivity;
import com.google.firebase.auth.FirebaseUser;

public class RegisterViewModel extends AndroidViewModel {
    private AppRepository appRepository;
    private MutableLiveData<FirebaseUser> userMutableLiveData;


/*Google suggests that you use 1 ViewModel per View (i.e., Activity or Fragment)
 (see https://youtu.be/Ts-uxYiBEQ8?t=8m40s)*/

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        userMutableLiveData = appRepository.getUserMutableLiveData();

    }
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void register(String email, String password,String confirmPassword){
        appRepository.register(email,password,confirmPassword);
    }



    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }
}