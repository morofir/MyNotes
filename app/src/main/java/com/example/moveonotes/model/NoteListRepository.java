package com.example.moveonotes.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.moveonotes.NoteAdapter;
import com.example.moveonotes.NoteObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.okhttp.Callback;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class NoteListRepository {
    private static NoteListRepository instance;

    private ArrayList<NoteObject> dataSet = new ArrayList<>();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference;
    MutableLiveData<List<NoteObject>> data = new MutableLiveData<>();



    //singleton pattern
    public static NoteListRepository getInstance(){

        if (instance == null){
            instance = new NoteListRepository();
        }
        return instance;
    }

    public MutableLiveData<List<NoteObject>> getNotesList(){
        setNotesList(); //get data from firebase

        Log.e("this size should be dif then 0", String.valueOf(dataSet.size()));


        data.setValue(dataSet);

        return data;
    }
    private void setNotesList(){
        //adding to data base from server (firebase)

        try {
            String uid = user.getUid();


            FirebaseDatabase database = FirebaseDatabase.getInstance("https://moveonotes-default-rtdb.europe-west1.firebasedatabase.app/");// europe server require link

            databaseReference = database.getReference("notes");
            //this function loads notes per user from firebase:
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<NoteObject> noteList = new ArrayList<>();
//                    dataSet.clear();
//                    data.setValue(null);

                    for (DataSnapshot dataSnapshot : snapshot.child(uid).getChildren()) { //fetch every user

                        String title = (String) dataSnapshot.child("title").getValue();
                        String body = (String) dataSnapshot.child("textBody").getValue();
                        String time = (String) dataSnapshot.child("currentTime").getValue();
                        String date = (String) dataSnapshot.child("currentDate").getValue();
                        String latitude = (String) dataSnapshot.child("latitude").getValue();
                        String longitude = (String) dataSnapshot.child("longitude").getValue();
                        String photo = (String) dataSnapshot.child("photo").getValue();
                        NoteObject noteObject = new NoteObject(title, body, date, time,latitude,longitude,photo);
                        dataSet.add(noteObject); //adding to list
                    }

                    data.postValue(dataSet); //todo

                    Log.e("size2", String.valueOf(dataSet.size()));

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
}
