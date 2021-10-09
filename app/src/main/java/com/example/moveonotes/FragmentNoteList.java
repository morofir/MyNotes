package com.example.moveonotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moveonotes.views.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentNoteList extends Fragment implements View.OnClickListener {
    FloatingActionButton newNoteFab;
    RecyclerView recyclerView;
    TextView no_notes_tv;
    List<NoteObject> noteList2 = new ArrayList<>();

    DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    NoteAdapter noteAdapter;


    @Override
    public void onStop() {
        super.onStop();


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.note_list_fragment, container, false);
        recyclerView = view.findViewById(R.id.recylerview_notes);
        no_notes_tv = view.findViewById(R.id.no_notes_yet_msg);
        setHasOptionsMenu(true);

        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        newNoteFab = getActivity().findViewById(R.id.newNoteFab);
        newNoteFab.setOnClickListener(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()) {

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getActivity()) {

                    private static final float SPEED = 300f; //Change this value (default=25f)

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return SPEED / displayMetrics.densityDpi;
                    }

                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }

        };


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            String uid = user.getUid();

            String apiUrl = Helper.getConfigValue(this.getContext(),"server_url");
            FirebaseDatabase database = FirebaseDatabase.getInstance(apiUrl);// europe server require link

            databaseReference = database.getReference("notes");
            //this function loads notes per user from firebase:
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
                        noteList.add(noteObject); //adding to list
                        noteAdapter = new NoteAdapter(getContext(), noteList); //note list initialized from firebase
                        noteList2.add(noteObject);

                    }
                    recyclerView.setAdapter(noteAdapter);
                    if(noteList.size()>0)
                        no_notes_tv.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception e){
            Log.e("e",e.getMessage());
        }


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_bar,menu);
        MenuItem item = menu.findItem( R.id.search_bar1);
        SearchView searchView = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());

        searchView.setQueryHint("Search Note...");
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setActionView(item, searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText==null){
                    return false;
                }
                else {
                    if(noteAdapter!=null)
                        noteAdapter.getFilter().filter(newText);

                    return false;
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newNoteFab:
                Intent intent = new Intent(getActivity(), AddNoteActivity.class);
                startActivity(intent);
                break;
        }

    }
}
