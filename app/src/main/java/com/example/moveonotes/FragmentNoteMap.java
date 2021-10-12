package com.example.moveonotes;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FragmentNoteMap  extends Fragment {
    DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    List<NoteObject> noteList2 = new ArrayList<>();
    private ClusterManager<MyItem> clusterManager;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { //TODO: MVVM FRAGMENT NOTE MAP
        super.onCreate(savedInstanceState);
        String uid = user.getUid();

        String apiUrl = Helper.getConfigValue(getActivity(), "server_url");
        FirebaseDatabase database = FirebaseDatabase.getInstance(apiUrl);// europe server require link

        databaseReference = database.getReference("notes");
        //this function loads favorites note per user:
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<NoteObject> noteList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.child(uid).getChildren()) { //fetch every user

                    String title = (String) dataSnapshot.child("title").getValue();
                    String body = (String) dataSnapshot.child("textBody").getValue();
                    String time = (String) dataSnapshot.child("currentTime").getValue();
                    String date = (String) dataSnapshot.child("currentDate").getValue();
                    String latitude = (String) dataSnapshot.child("latitude").getValue();
                    String longitude = (String) dataSnapshot.child("longitude").getValue();
                    String photo = (String) dataSnapshot.child("photo").getValue();
                    NoteObject noteObject = new NoteObject(title, body, date, time,latitude,longitude,photo);

                    noteList2.add(noteObject);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //initialize view

         View view = inflater.inflate(R.layout.note_map_fragment, container , false);


         SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        //async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.clear();

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(31.9117, 35.0516), 6 ));
                clusterManager = new ClusterManager<>(getContext(), googleMap);
                // Point the map's listeners at the listeners implemented by the cluster
                // manager.
                googleMap.setOnCameraIdleListener(clusterManager);
                googleMap.setOnMarkerClickListener(clusterManager);
                googleMap.setOnMarkerClickListener(clusterManager.getMarkerManager());
                MarkerManager.Collection normalMarkersCollection = clusterManager.getMarkerManager().newCollection();


                //map is loaded:

                HashMap<String, String> hashMapTime = new HashMap<>(); //marker is final class, i cant add a new field so i put the field in hashmap
                HashMap<String, String> hashMapPhoto = new HashMap<>(); //marker is final class, i cant add a new field so i put the field in hashmap

                for (NoteObject note : noteList2) {
                        Double lat = Double.parseDouble(note.getLatitude());
                        Double lon = Double.parseDouble(note.getLongitude());

                        //generate double number between 0 and 1, wont affect much, except remove duplicates markers
                        MarkerOptions marker = new MarkerOptions().position(new LatLng(lat+Math.random()*0.001, lon+Math.random()*0.001));

                        marker.title(note.getTitle());
                        marker.snippet(note.getTextBody());
                        hashMapTime.put(note.getTitle(),note.getCurrentTime());
                        hashMapPhoto.put(note.getTitle(),note.getPhoto());

                        normalMarkersCollection.addMarker(marker);//adding marker of all notes from firebase by location written in

                        //add items to cluster
                        MyItem offsetItem = new MyItem(lat, lon, note.getTitle(), note.getTextBody(),note.getCurrentTime(),note.getCurrentDate());
                        clusterManager.addItem(offsetItem);

                    }

                clusterManager.setAnimation(false);
                    normalMarkersCollection.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Bundle bundle = new Bundle();
                        bundle.putString("title",marker.getTitle());
                        bundle.putString("body",marker.getSnippet());
                        bundle.putString("time",hashMapTime.get(marker.getTitle())); //(title:time)
                        bundle.putString("photo",hashMapPhoto.get(marker.getTitle())); //(title:photo)

                        ShowNoteFragment showNoteFragment = new ShowNoteFragment();
                        showNoteFragment.setArguments(bundle);

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),16),3000,null); //zoom in 3 seconds


                        clusterManager.cluster();
                        clusterManager.clearItems();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,showNoteFragment).commit();

                            }
                        }, 5000); //open note after 5 second (2 seconds after zooming)


                        return true;
                    }
                });







                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {


                        float zoom = googleMap.getCameraPosition().zoom;

                        if(zoom>7){
                            clusterManager.clearItems();
                            clusterManager.cluster();
                        }


                    }
                });
            }
        });

        return view;
    }




}
