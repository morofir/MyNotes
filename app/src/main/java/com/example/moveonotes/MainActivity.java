package com.example.moveonotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity  {

    FirebaseAuth auth;
    TextView welcomeMsg;
    ImageView logOutButton,searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeMsg = findViewById(R.id.msg_top);
        logOutButton = findViewById(R.id.logout_btn);
        searchBtn = findViewById(R.id.search_btn);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
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

        auth = FirebaseAuth.getInstance();


        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNav);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new FragmentNoteList()).addToBackStack("f1").commit();



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

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            String username = user.getEmail();
            int index = username.indexOf('@');
            username = username.substring(0,index);
            welcomeMsg.setText("Hello "+username+"!");
        }

    }

    public void logout(View view){

        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Logout").setMessage("Are you sure you want to log out?").setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();

    }
}