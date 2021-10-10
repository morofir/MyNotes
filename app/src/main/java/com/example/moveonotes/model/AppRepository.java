package com.example.moveonotes.model;

import android.app.Application;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.example.moveonotes.Helper;
import com.example.moveonotes.NoteAdapter;
import com.example.moveonotes.NoteObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AppRepository {
    private Application application;
    private FirebaseAuth firebaseAuth;
    private MutableLiveData<FirebaseUser> userMutableLiveData;
    private static AppRepository instance;
    private ArrayList<NoteObject> dataset = new ArrayList<>();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference;



    public AppRepository() {

    }

    //singleton pattern
    public static AppRepository getInstance(){
        if (instance == null){
            instance = new AppRepository();
        }
        return instance;
    }

    public MutableLiveData<List<NoteObject>> getNotesList(){
        setNotesList();//get data from firebase
        MutableLiveData<List<NoteObject>> data = new MutableLiveData<>();
        data.setValue(dataset);

        return data;
    }
    private void setNotesList(){
        //adding to data base if it were from server (firebase)
        try {
            String uid = user.getUid();

            FirebaseDatabase database = FirebaseDatabase.getInstance("https://moveonotes-default-rtdb.europe-west1.firebasedatabase.app/");// europe server require link

            databaseReference = database.getReference("notes");
            //this function loads notes per user from firebase:
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    for (DataSnapshot dataSnapshot : snapshot.child(uid).getChildren()) { //fetch every user

                        String title = (String) dataSnapshot.child("title").getValue();
                        String body = (String) dataSnapshot.child("textBody").getValue();
                        String time = (String) dataSnapshot.child("currentTime").getValue();
                        String date = (String) dataSnapshot.child("currentDate").getValue();
                        String latitude = (String) dataSnapshot.child("latitude").getValue();
                        String longitude = (String) dataSnapshot.child("longitude").getValue();
                        String photo = (String) dataSnapshot.child("photo").getValue();
                        NoteObject noteObject = new NoteObject(title, body, date, time,latitude,longitude,photo);
                        dataset.add(noteObject); //adding to list
                        Log.e("gfsd",noteObject.getTitle());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("e",error.getMessage());

                }
            });
        }catch (Exception e){
            Log.e("e",e.getMessage());
        }

    }

    public MutableLiveData<Boolean> getLoggedOutuserMutableLiveData() {
        return loggedOutuserMutableLiveData;
    }

    private MutableLiveData<Boolean> loggedOutuserMutableLiveData;


    //here lays all the data, firebase ....
    //should know nothing about view and how it presents info to user
    //apiService in model also (if there is)

    public AppRepository(Application application){
        this.application = application;
        firebaseAuth = firebaseAuth.getInstance();
        userMutableLiveData = new MutableLiveData<>();
        loggedOutuserMutableLiveData = new MutableLiveData<>();

        if(firebaseAuth.getCurrentUser() != null){
            userMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            loggedOutuserMutableLiveData.postValue(false);
        }


    }
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void register(String email, String password,String confirmPassword) {

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
            if (password.equals(confirmPassword)) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(application.getMainExecutor(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    userMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                                } else {
                                    Toast.makeText(application, "registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            else
                Toast.makeText(application, "Passwords don't match", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(application, "Please Fill all fields", Toast.LENGTH_SHORT).show();


    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void loginUser(String email,String password) {

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(application.getMainExecutor(), new OnCompleteListener<AuthResult>() { //get message is enum
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        userMutableLiveData.postValue(firebaseAuth.getCurrentUser());

                    } else {
                        Toast.makeText(application, "login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(application, "Invalid Email/Password", Toast.LENGTH_SHORT).show();
        }
    }

    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public void logOut(){
        firebaseAuth.signOut();
        loggedOutuserMutableLiveData.postValue(true);
    }
}