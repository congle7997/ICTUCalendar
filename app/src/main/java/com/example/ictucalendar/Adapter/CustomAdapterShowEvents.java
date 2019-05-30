package com.example.ictucalendar.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ictucalendar.Object.Event;
import com.example.ictucalendar.R;

import java.util.List;

public class CustomAdapterShowEvents extends RecyclerView.Adapter<CustomAdapterShowEvents.RecyclerViewHolder> {

    public final String TAG = CustomAdapterShowEvents.class.getSimpleName();

    private Context context;
    private List<Event> listEventSelected;

    public CustomAdapterShowEvents(Context context, List<Event> listEventSelected) {
        this.context = context;
        this.listEventSelected = listEventSelected;
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubjectName, txtTime, txtPlace, txtLecturer;
        LinearLayout lnSubject, lnTime, lnPlace, lnLecturer;
        ImageView imgSubjectName;


        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubjectName = itemView.findViewById(R.id.txt_subject_name);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtPlace = itemView.findViewById(R.id.txt_place);
            txtLecturer = itemView.findViewById(R.id.txt_lecturer);
            lnSubject = itemView.findViewById(R.id.ln_subject);
            lnTime = itemView.findViewById(R.id.ln_time);
            lnPlace = itemView.findViewById(R.id.ln_place);
            lnLecturer = itemView.findViewById(R.id.ln_lecturer);
            imgSubjectName = itemView.findViewById(R.id.img_subject_name);
            
        }
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_event, viewGroup, false);
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return listEventSelected.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder recyclerViewHolder, int i) {
        if (listEventSelected.get(i).getType().equals("Lecturer")) {
            recyclerViewHolder.txtSubjectName.setText(listEventSelected.get(i).getSubjectName());
            recyclerViewHolder.txtTime.setText(listEventSelected.get(i).getTime());
            recyclerViewHolder.txtPlace.setText(listEventSelected.get(i).getPlace());
            recyclerViewHolder.lnLecturer.setVisibility(View.GONE);
            recyclerViewHolder.lnSubject.setBackgroundColor(Color.rgb(153, 187, 255));
        } else if (listEventSelected.get(i).getType().equals("Student")) {
            //Log.d(TAG, "onBindViewHolder: " + "Event");
            recyclerViewHolder.txtSubjectName.setText(listEventSelected.get(i).getSubjectName());
            recyclerViewHolder.txtTime.setText(listEventSelected.get(i).getTime());
            recyclerViewHolder.txtPlace.setText(listEventSelected.get(i).getPlace());
            recyclerViewHolder.txtLecturer.setText(listEventSelected.get(i).getLecturer());
            recyclerViewHolder.lnSubject.setBackgroundColor(Color.rgb(153, 187, 255));
        } else {
            recyclerViewHolder.imgSubjectName.setImageResource(R.drawable.ic_note);
            recyclerViewHolder.txtSubjectName.setText(listEventSelected.get(i).getContentNote());
            recyclerViewHolder.lnTime.setVisibility(View.GONE);
            recyclerViewHolder.lnPlace.setVisibility(View.GONE);
            recyclerViewHolder.lnLecturer.setVisibility(View.GONE);
            recyclerViewHolder.lnSubject.setBackgroundColor(Color.rgb(221, 153, 255));
        }

    }
}