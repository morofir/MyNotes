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

public class AddNoteRepository {

    private static AddNoteRepository instance;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //singleton pattern
    public static AddNoteRepository getInstance(){

        if (instance == null){
            instance = new AddNoteRepository();
        }
        return instance;
    }


    public void uploadNote(NoteObject note,String apiUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(apiUrl);// europe server require link
        DatabaseReference myRef = database.getReference("notes").child(user.getUid());
        myRef.push().setValue(note); //inserting the note object to firebase

    }
}
