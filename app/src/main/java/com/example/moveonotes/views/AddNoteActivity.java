package com.example.moveonotes.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.example.moveonotes.Helper;
import com.example.moveonotes.R;
import com.example.moveonotes.viewmodel.AddNoteViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddNoteActivity extends AppCompatActivity {
    Button saveButton;
    EditText title,bodyText;
    TextView latitudeTextView, longitTextView,date_now,time_now;
    ImageButton galleryBtn,cameraButton;

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
    AddNoteViewModel addNoteViewModel;


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        picCounter = sharedPreferences.getInt("pic_number",0); //getting the number of the pic

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        initiateViews();
        requestPermissions();

        addNoteViewModel = new ViewModelProvider(this).get(AddNoteViewModel.class);

        initObserver();


//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        apiUrl = Helper.getConfigValue(this, "server_url");


        sharedPreferences = getSharedPreferences("pic_number",MODE_PRIVATE);
        picCounter = sharedPreferences.getInt("pic_number",0);

        // use the shared preferences and editor as you normally would
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


        date_now.setText(currentDate);
        time_now.setText(currentTime);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkpermissions here
                //if !checkPermissions -> show alert
                requestPermissions();
                if (!checkPermissions()) {
                    new AlertDialog.Builder(AddNoteActivity.this).setTitle("Required Location Permissions")
                            .setMessage("This app may not work correctly without the requested permissions. " +
                                    "Open the app setting screen to modify app permission.").setPositiveButton("OK", null)
                            .show();
                    if (!checkPermissions())
                        requestPermissions();

                } else { //got location permission
                    Date currentTime = Calendar.getInstance().getTime();

                    String noteTitle = title.getText().toString();
                    String noteBody = bodyText.getText().toString();
                    String noteDate = currentTime.toInstant().toString().substring(0, 10); //date format xxxx-xx-xx
                    String noteTime = String.valueOf(currentTime).substring(11, 19); //time format xx:xx:xx
                    String noteLat = latitudeTextView.getText().toString();
                    String noteLon = longitTextView.getText().toString();

                    if (!TextUtils.isEmpty(noteTitle) && !TextUtils.isEmpty(noteBody)) { //not empty
                        addNoteViewModel.saveNote(getApplicationContext(), noteTitle, noteBody, noteDate, noteTime, noteLat, noteLon, PhotoPath);  //photo path can be null
                        Toast.makeText(getApplicationContext(), "Note Added", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Must submit title and note body!", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWritePermission == PackageManager.PERMISSION_GRANTED) {
                    choosePicFromGallery();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            }
        });


        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWritePermission == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);

            }
        });

    }

    private void initObserver() {
//        addNoteViewModel.getNoteList().observe(this, new Observer<List<NoteObject>>() {
//            @Override
//            public void onChanged(List<NoteObject> noteObjects) {
//                Log.e("obs","addnoteobserver");
//
//            }
//        });
    }

    private void initiateViews() {
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
    }



    @SuppressLint("IntentReset")
    private void choosePicFromGallery() { //https://stackoverflow.com/questions/59470837/how-to-select-photo-from-gallery-in-viewmodel data binding
        //or leave like this https://stackoverflow.com/questions/62964662/startactivityforresult-to-get-images-from-gallery-in-mvvm-architecture
        Intent openGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//TODO check if its ok mvvm wise
        openGallery.setType("image/*");
        openGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(openGallery, "Select Picture"), PICK_FROM_GALLERY);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("pic_number",(picCounter+1)); //add shares preference
        editor.apply();
    }



    @SuppressLint("MissingPermission")
    private void getLastLocation() {
            // check if location is enabled
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != 1
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != 1)) {
                String[] res = addNoteViewModel.getLocation(mFusedLocationClient);

                latitudeTextView.setText(res[0]);
                longitTextView.setText(res[1]);
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }




    private LocationCallback mLocationCallback = new LocationCallback() {

        @SuppressLint("SetTextI18n")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    // method to check for permissions
    private boolean checkPermissions() { //viewmodel
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions(location and files)
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri;

        if(requestCode == PICK_FROM_GALLERY && resultCode == AppCompatActivity.RESULT_OK) //gallery option
        {
            if (data != null) {
                selectedImageUri = data.getData();
                PhotoPath = selectedImageUri.toString();
                Path = PhotoPath;
                Glide.with(this).load(PhotoPath).into(noteImageView); //data binding?
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

    private void openCamera() {
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
                Toast.makeText(this, "No Camera/Gallery Permissions", Toast.LENGTH_SHORT).show();
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