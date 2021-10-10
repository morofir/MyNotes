package com.example.moveonotes.viewmodel;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.moveonotes.model.LoginAppRepository;
import com.google.firebase.auth.FirebaseUser;

public class RegisterViewModel extends AndroidViewModel {
    private LoginAppRepository loginAppRepository;
    private MutableLiveData<FirebaseUser> userMutableLiveData;


/*Google suggests that you use 1 ViewModel per View (i.e., Activity or Fragment)
 (see https://youtu.be/Ts-uxYiBEQ8?t=8m40s)*/

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        loginAppRepository = new LoginAppRepository(application);
        userMutableLiveData = loginAppRepository.getUserMutableLiveData();

    }
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void register(String email, String password,String confirmPassword){
        loginAppRepository.register(email,password,confirmPassword);
    }



    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }
}