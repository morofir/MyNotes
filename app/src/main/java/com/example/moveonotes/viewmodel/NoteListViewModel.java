package com.example.moveonotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.moveonotes.NoteObject;
import com.example.moveonotes.model.AppRepository;

import java.util.List;

public class NoteListViewModel extends AndroidViewModel {
    private MutableLiveData<List<NoteObject>> NoteList; //live data cant be directly changed
    private AppRepository repository;

    public NoteListViewModel(@NonNull Application application) {
        super(application);
    }


    public void init(){
        if (NoteList != null){
            return;
        }
        repository = AppRepository.getInstance();
        NoteList = repository.getNotesList(); //retrieve list from repo
    }

    public LiveData<List<NoteObject>> getNoteList(){ //mutable live data is sub class of live data
        return NoteList;
    }
}
