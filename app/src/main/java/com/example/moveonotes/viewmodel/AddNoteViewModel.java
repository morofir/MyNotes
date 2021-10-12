package com.example.moveonotes.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.moveonotes.Helper;
import com.example.moveonotes.NoteObject;
import com.example.moveonotes.model.AddNoteRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class AddNoteViewModel extends AndroidViewModel {

    private MutableLiveData<List<NoteObject>> NoteList; //live data cant be directly changed
    private AddNoteRepository repository;
    FusedLocationProviderClient mFusedLocationClient;
    private static String lon = "0";
    private static String lat = "0";
    private LocationCallback mLocationCallback;



    public AddNoteViewModel(@NonNull Application application) {
        super(application);
        repository = new AddNoteRepository();
    }

    public void init() {
        if (NoteList != null) {
            return;
        }
        repository = repository.getInstance();
    }

    public void saveNote(Context context, String title, String body, String date, String time, String lat, String lon, String photoPath) {
        NoteObject noteObject = new NoteObject(title, body, date, time, lat, lon, photoPath); //creating note object

        String apiUrl = Helper.getConfigValue(context, "server_url");

        repository.uploadNote(noteObject, apiUrl);

    }

    @SuppressLint("MissingPermission")
    public String[] getLocation(FusedLocationProviderClient location) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication());

        String[] res = new String[2]; //(lat,lon)

        // getting lasT location from FusedLocationClient

        location.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location == null) {
                    requestNewLocationData();
                } else {
                    lon = String.valueOf(location.getLongitude());
                    lat = String.valueOf(location.getLatitude());
                }
            }
        });
            res[0] = String.valueOf(lat);//lat
            res[1] = String.valueOf(lon);//lon

        return res;
    }




    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication());
    }


    public void initiatePhoto(String path, ImageView imageView) {

    }
}
