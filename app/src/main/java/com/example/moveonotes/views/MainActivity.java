package com.example.moveonotes.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.moveonotes.FragmentNoteList;
import com.example.moveonotes.FragmentNoteMap;
import com.example.moveonotes.R;
import com.example.moveonotes.viewmodel.LogoutViewModel;
import com.example.moveonotes.viewmodel.RegisterViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity  {

    FirebaseAuth auth;
    TextView welcomeMsg;
    ImageView logOutButton,searchBtn;
    private LogoutViewModel logoutViewModel;


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        initiateViews();



        logoutViewModel = new ViewModelProvider(this).get(LogoutViewModel.class);


        //view handle
        logoutViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null && auth.getCurrentUser() != null) {

                    String username = firebaseUser.getEmail();
                    int index = username.indexOf('@');
                    username = username.substring(0, index);
                    welcomeMsg.setText("Hello " + username + "!");
                }
            }
        });

        logoutViewModel.getLoggedOutLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedOut) {
                if(loggedOut){
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
            }
        });



        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAlert(v);
            }
        });
        getSupportActionBar().hide();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getSupportActionBar().isShowing()){
                    getSupportActionBar().show();
                }else{
                    getSupportActionBar().hide();
                }

            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNav);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new FragmentNoteList()).addToBackStack("f1").commit();

    }

    private void initiateViews() {
        welcomeMsg = findViewById(R.id.msg_top);
        logOutButton = findViewById(R.id.logout_btn);
        searchBtn = findViewById(R.id.search_btn);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNav = item -> {
        Fragment selected = null;

        switch (item.getItemId()){
            case R.id.note_list:
                selected = new FragmentNoteList();
                welcomeMsg.setText("Notes List");
                break;

            case R.id.note_map:
                selected = new FragmentNoteMap();
                welcomeMsg.setText("Notes Map");


                break;

        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,selected).addToBackStack("f0").commit();
        return true;
    };



    public void logoutAlert(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Logout").setMessage("Are you sure you want to log out?").setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutViewModel.logOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //nothing
            }
        });
        builder.create().show();

    }
}