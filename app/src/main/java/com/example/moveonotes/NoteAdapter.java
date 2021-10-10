package com.example.moveonotes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> implements Filterable {
    Context context;
    List<NoteObject> noteList;
    List<NoteObject> notesAll;



    public NoteAdapter(Context context, List<NoteObject> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.notesAll = new ArrayList<>(noteList);
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //sort notes by date
        Collections.sort(noteList, new Comparator<NoteObject>() {
            @Override
            public int compare(NoteObject o1, NoteObject o2) {
                return o2.getCurrentDate().compareTo(o1.getCurrentDate());
            }
        });

        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        NoteObject noteObject = noteList.get(position);
        holder.timeOutput.setText(noteObject.getCurrentDate()+" " +noteObject.getCurrentTime());
        holder.titleOutput.setText(noteObject.getTitle());
        holder.bodyOutput.setText(noteObject.getTextBody());
        String picUrl = Helper.getConfigValue(holder.bodyOutput.getContext(), "no_image_note");

        if(noteObject.getPhoto()==null)
            Glide.with(context).load(picUrl).into(holder.note_img); // as center crop(xml)
        else{
            Glide.with(context).load(noteObject.getPhoto()).into(holder.note_img);
        }


        //when user clicks on item, note, will attach new fragment, showing this notes
        //and passing the object with bundle
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("photo",noteObject.getPhoto());
                bundle.putString("title",noteObject.getTitle());
                bundle.putString("body",noteObject.getTextBody());
                bundle.putString("time",noteObject.getCurrentTime());
                bundle.putString("date",noteObject.getCurrentDate());

                ShowNoteFragment showNoteFragment = new ShowNoteFragment();
                showNoteFragment.setArguments(bundle);

                ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout,showNoteFragment).addToBackStack("f5").commit();
            }
        });


    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    //background thread
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<NoteObject> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(notesAll);
            } else {
                for (NoteObject note1 : notesAll) { //check if the item is on the recyclerview
                    String title = note1.getTitle().toLowerCase();
                    String text = note1.getTextBody().toLowerCase();
                    String item = constraint.toString().toLowerCase();
                    if (title.contains(item) || text.contains(item)) {
                        filteredList.add(note1);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            noteList.clear();
            noteList.addAll((Collection<? extends NoteObject>) results.values);
            notifyDataSetChanged();

        }
    };

    public class MyViewHolder  extends RecyclerView.ViewHolder{
        TextView titleOutput,bodyOutput,timeOutput;
        ImageView note_img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleOutput = itemView.findViewById(R.id.note_title_item);
            bodyOutput = itemView.findViewById(R.id.note_body_item);
            timeOutput = itemView.findViewById(R.id.note_time_item);
            note_img = itemView.findViewById(R.id.note_imgview);

        }
    }

}
