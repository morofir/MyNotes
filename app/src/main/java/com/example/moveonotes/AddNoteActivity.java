package com.example.moveonotes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;


import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddNoteActivity extends AppCompatActivity {
    Button saveButton;
    EditText title,bodyText;
    TextView latitudeTextView, longitTextView,date_now,time_now;
    ImageButton galleryBtn,cameraButton;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    final int WRITE_PERMISSION_REQUEST = 1;
    final int PICK_FROM_GALLERY = 2;
    final int CAMERA_REQUEST = 3;
    String PhotoPath,Path;
    ImageView noteImageView;
    Boolean isPhoto = false;
    File noteFile;
    int picCounter;
    SharedPreferences sharedPreferences;
    Uri imageUri;
    String apiUrl;



    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        picCounter = sharedPreferences.getInt("pic_number",0); //getting the number of the pic

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);


        saveButton = findViewById(R.id.savenote_btn);
        title = findViewById(R.id.title_input);
        bodyText = findViewById(R.id.body_input);
        latitudeTextView = findViewById(R.id.lat_user);
        longitTextView = findViewById(R.id.lon_user);
        date_now = findViewById(R.id.date_now);
        time_now = findViewById(R.id.time_now);
        noteImageView = findViewById(R.id.image_view_note);
        galleryBtn = findViewById(R.id.img_gallery);
        cameraButton = findViewById(R.id.take_pic_btn);


        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        apiUrl = Helper.getConfigValue(this, "server_url");



        sharedPreferences = getSharedPreferences("pic_number",MODE_PRIVATE);
        picCounter = sharedPreferences.getInt("pic_number",0);



// use the shared preferences and editor as you normally would
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


        date_now.setText(currentDate);
        time_now.setText(currentTime);
        requestPermissions();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoteObject noteObject = new NoteObject();

                noteObject.setTitle(title.getText().toString());
                noteObject.setTextBody(bodyText.getText().toString());
                Date currentTime = Calendar.getInstance().getTime();

                noteObject.setCurrentDate(currentTime.toInstant().toString().substring(0,10)); //date format xxxx-xx-xx
                noteObject.setCurrentTime(String.valueOf(currentTime).substring(11,19)); //time format xx:xx:xx

                // check if permissions are given
                if (checkPermissions()) {
                    getLastLocation();
                }
                else {
                    // if permissions aren't available,
                    // request for permissions
                    requestPermissions();
//
                }

                noteObject.setLatitude(latitudeTextView.getText().toString());
                noteObject.setLongitude(longitTextView.getText().toString());

                if(PhotoPath!=null)
                    noteObject.setPhoto(PhotoPath); //if theres a picture, will save the paths in order to present it later
                else
                    noteObject.setPhoto(null);
                if(!checkPermissions()){
                    new AlertDialog.Builder(AddNoteActivity.this).setTitle("Required Location Permissions")
                            .setMessage("This app may not work correctly without the requested permissions. " +
                                    "Open the app setting screen to modify app permission.").setPositiveButton("OK",null)
                            .show();
                    requestPermissions();
                }
                else
                    uploadNote(noteObject);


            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                }
                else
                {
                    takePic();
                }//todo

            }
        });

    }

    private void uploadImage() {
        int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWritePermission == PackageManager.PERMISSION_GRANTED) {
            choosePic();
        }
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
    }


    @SuppressLint("IntentReset")
    private void choosePic() {
        Intent openGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGallery.setType("image/*");
        openGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(openGallery, "Select Picture"), PICK_FROM_GALLERY);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("pic_number",(picCounter+1)); //add shares preference
        editor.apply();
    }

    private void uploadNote(NoteObject noteObject) {
        String title1 = title.getText().toString();
        String body1 = bodyText.getText().toString();

        if(!TextUtils.isEmpty(title1) && !TextUtils.isEmpty(body1)){ //not empty

            FirebaseDatabase database = FirebaseDatabase.getInstance(apiUrl);// europe server require link
            DatabaseReference myRef = database.getReference("notes").child(user.getUid());
            myRef.push().setValue(noteObject); //inserting the note object to firebase

            Toast.makeText(getApplicationContext(),"Note Added",Toast.LENGTH_SHORT).show();
            finish();


        }else{
            Toast.makeText(getApplicationContext(),"Must submit title and note body!",Toast.LENGTH_SHORT).show();
        }


    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
            // check if location is enabled
            if (isLocationEnabled()) {

                // getting lasT location from FusedLocationClient
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitudeTextView.setText(location.getLatitude() + "");
                            longitTextView.setText(location.getLongitude() + "");
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }


    @SuppressLint("MissingPermission")
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions(location)
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri = null;

        if(requestCode == PICK_FROM_GALLERY && resultCode == AppCompatActivity.RESULT_OK) //gallery option
        {
            if (data != null) {
                selectedImageUri = data.getData();
                PhotoPath = selectedImageUri.toString();
                Path = PhotoPath;
                Glide.with(this).load(PhotoPath).into(noteImageView);
                noteImageView.setVisibility(View.VISIBLE);

                isPhoto = true;
            }
        }

        else {
            if (requestCode == CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK) // picture option
            {
                PhotoPath = noteFile.getAbsolutePath();
                Path = PhotoPath;
                Glide.with(this).load(PhotoPath).into(noteImageView);
                noteImageView.setVisibility(View.VISIBLE);

                isPhoto = true;
            }
        }
    }

    private void takePic() {
        noteFile = new File(getExternalFilesDir(null),"pic_number"+picCounter+".jpg");

        imageUri = FileProvider.getUriForFile(
                AddNoteActivity.this,
                "com.example.MoveoNotes.provider", noteFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,CAMERA_REQUEST);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("pic_number",(picCounter+1));
        editor.apply();
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
        else if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No Permissions", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}