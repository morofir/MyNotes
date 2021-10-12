package com.example.moveonotes.views;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moveonotes.NoteAdapter;
import com.example.moveonotes.NoteObject;
import com.example.moveonotes.R;
import com.example.moveonotes.viewmodel.NoteListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class FragmentNoteList extends Fragment implements View.OnClickListener {
    FloatingActionButton newNoteFab;
    RecyclerView recyclerView;
    TextView no_notes_tv;
    private NoteListViewModel noteListViewModel;
    NoteAdapter noteAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        noteListViewModel = new ViewModelProvider(this).get(NoteListViewModel.class);
        noteListViewModel.init();
        View view = inflater.inflate(R.layout.note_list_fragment, container, false);
        recyclerView = view.findViewById(R.id.recylerview_notes);

        no_notes_tv = view.findViewById(R.id.no_notes_yet_msg);

        //observe changes
        initObserver();
        initRecyclerView();


        return view;
    }


    private void initRecyclerView() {
        noteAdapter = new NoteAdapter(getContext(), noteListViewModel.getNoteList().getValue());
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }


    private void initObserver() {
        noteListViewModel.getNoteList().observe(getViewLifecycleOwner(), new Observer<List<NoteObject>>() {
            @Override
            public void onChanged(List<NoteObject> noteObjects) {
                noteAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        newNoteFab = getActivity().findViewById(R.id.newNoteFab);
        newNoteFab.setOnClickListener(this);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()) {
//
//            @Override
//            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
//                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getActivity()) {
//
//                    private static final float SPEED = 300f; //Change this value (default=25f)
//
//                    @Override
//                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//                        return SPEED / displayMetrics.densityDpi;
//                    }
//
//                };
//                smoothScroller.setTargetPosition(position);
//                startSmoothScroll(smoothScroller);
//            }
//
//        };
//
//
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setAdapter(noteAdapter);
        recyclerView.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();



    }

    @Override
    public void onStart() {
        super.onStart();
        if(noteListViewModel.getNoteList()!=null )
            no_notes_tv.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);

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
                if (newText != null) {
                    if (noteAdapter != null)
                        noteAdapter.getFilter().filter(newText);

                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onClick(View v) {  //todo in viewmodel not view
        /*
        viewModel.activityToStart.observe(this, Observer { value ->
        val intent = Intent(this, value.first.java)
        if(value.second != null)
            intent.putExtras(value.second)
        startActivity(intent)
        })*/
        switch (v.getId()) {
            case R.id.newNoteFab:
                Intent intent = new Intent(getActivity(), AddNoteActivity.class);
                startActivity(intent);
                break;
        }

    }
}
