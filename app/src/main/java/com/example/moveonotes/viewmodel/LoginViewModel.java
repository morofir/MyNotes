package com.example.moveonotes.viewmodel;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.moveonotes.model.AppRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel {
    private AppRepository appRepository;
    private MutableLiveData<FirebaseUser> userMutableLiveData;



/*Google suggests that you use 1 ViewModel per View (i.e., Activity or Fragment)
 (see https://youtu.be/Ts-uxYiBEQ8?t=8m40s)*/

    public LoginViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        userMutableLiveData = appRepository.getUserMutableLiveData();

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void loginUser(String email, String password){
        appRepository.loginUser(email,password);

    }



    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }
}