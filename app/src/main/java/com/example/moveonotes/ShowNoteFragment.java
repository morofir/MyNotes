package com.example.moveonotes;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.moveonotes.views.FragmentNoteList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ShowNoteFragment extends Fragment implements View.OnClickListener {
    EditText noteTitle, noteText;
    TextView noteTime;
    View view;
    Button updateNote, deleteNote;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_note, container, false);
        noteTitle = view.findViewById(R.id.titleitem_shownote);
        noteText = view.findViewById(R.id.body_showinput);
        noteTime = view.findViewById(R.id.time_shownow);
        deleteNote = view.findViewById(R.id.delete_note);
        updateNote = view.findViewById(R.id.update_note);
        imageView = view.findViewById(R.id.imageview_shownote);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            noteTitle.setText(bundle.getString("title"));
            noteText.setText(bundle.getString("body"));
            if(bundle.getString("date") == null)
                noteTime.setText("Note Originally created in \n    " +
                        bundle.getString("time"));
            else
                noteTime.setText("Note Originally created in \n    " +
                    bundle.getString("time") + " " + bundle.getString("date"));
            Glide.with(getActivity()).load(bundle.getString("photo")).into(imageView);


            if(bundle.getString("photo")!=null){
                Glide.with(getActivity()).load(bundle.getString("photo")).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                Log.e("p",bundle.getString("photo"));
            }
        }
        deleteNote.setOnClickListener(this);
        updateNote.setOnClickListener(this);

        return view;

    }

    @Override
    public void onClick(View v) {
        String apiUrl = Helper.getConfigValue(getContext(),"server_url");
        switch (v.getId()) {
            case R.id.delete_note:
                try {
                    Bundle bundle = this.getArguments();

                    DatabaseReference dR = FirebaseDatabase.getInstance(apiUrl)
                            .getReference("notes").child(user.getUid());
                    Query query = dR.orderByChild("currentTime")
                            .equalTo(bundle.getString("time"));
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                snap.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    Toast.makeText(getContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new FragmentNoteList()).commit();
                }catch (Exception e){Log.e("e",e.getMessage());}

                break;


            case R.id.update_note:
                try{
                    Bundle bundle = this.getArguments();
                    DatabaseReference dR = FirebaseDatabase.getInstance(apiUrl)
                            .getReference("notes").child(user.getUid());
                    Query query = dR.orderByChild("currentTime")
                            .equalTo(bundle.getString("time")); //finding the right note by time
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                if(!noteText.getText().toString().isEmpty() && !noteTitle.getText().toString().isEmpty()) {
                                    snap.getRef().child("textBody").setValue(noteText.getText().toString()); // updating the node in firebase
                                    snap.getRef().child("title").setValue(noteTitle.getText().toString());
                                    Toast.makeText(getContext(), "Note Updated/Saved", Toast.LENGTH_SHORT).show();

                                }
                                else
                                    Toast.makeText(getContext(), "NOT UPDATED: Must submit Note Title and Body", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new FragmentNoteList()).commit();


                }catch (Exception e){Log.e("e",e.getMessage());}

                break;


        }
    }

}