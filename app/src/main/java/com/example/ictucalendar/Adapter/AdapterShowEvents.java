package com.example.ictucalendar.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Update;
import com.example.ictucalendar.Activity.MainActivity;
import com.example.ictucalendar.Object.Event;
import com.example.ictucalendar.R;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.List;

public class AdapterShowEvents extends RecyclerView.Adapter<AdapterShowEvents.ViewHolder> {

    public final String TAG = AdapterShowEvents.class.getSimpleName();

    Context context;
    List<Event> listEventSelected;
    MainActivity mainActivity;
    MaterialCalendarView materialCalendarView;

    public AdapterShowEvents(Context context, List<Event> listEventSelected,
                             MainActivity mainActivity, MaterialCalendarView materialCalendarView) {
        this.context = context;
        this.listEventSelected = listEventSelected;
        this.mainActivity = mainActivity;
        this.materialCalendarView = materialCalendarView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubjectName, txtTime, txtPlace, txtLecturer, txtClassID;
        LinearLayout lnSubject, lnTime, lnPlace, lnLecturer, lnClassID;
        ImageView imgSubjectName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubjectName = itemView.findViewById(R.id.txt_subject_name);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtPlace = itemView.findViewById(R.id.txt_place);
            txtLecturer = itemView.findViewById(R.id.txt_lecturer);
            txtClassID = itemView.findViewById(R.id.txt_class_id);
            lnSubject = itemView.findViewById(R.id.ln_subject);
            lnTime = itemView.findViewById(R.id.ln_time);
            lnPlace = itemView.findViewById(R.id.ln_place);
            lnLecturer = itemView.findViewById(R.id.ln_lecturer);
            lnClassID = itemView.findViewById(R.id.ln_class_id);
            imgSubjectName = itemView.findViewById(R.id.img_subject_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_event, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return listEventSelected.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Event event = listEventSelected.get(i);
        if (event.getType().equals("lecturer")) {
            viewHolder.lnLecturer.setVisibility(View.GONE);
            viewHolder.txtSubjectName.setText(event.getSubjectName());
            viewHolder.txtTime.setText(event.getTime());
            viewHolder.txtClassID.setText(event.getClassID());
            viewHolder.txtPlace.setText(event.getPlace());
            viewHolder.lnSubject.setBackgroundColor(Color.rgb(153, 187, 255));
        } else if (event.getType().equals("student")) {
            //Log.d(TAG, "onBindViewHolder: " + "Event");
            viewHolder.lnClassID.setVisibility(View.GONE);
            viewHolder.txtSubjectName.setText(event.getSubjectName());
            viewHolder.txtTime.setText(event.getTime());
            viewHolder.txtPlace.setText(event.getPlace());
            viewHolder.txtLecturer.setText(event.getLecturer());
            viewHolder.lnSubject.setBackgroundColor(Color.rgb(153, 187, 255));
        } else {
            viewHolder.imgSubjectName.setImageResource(R.drawable.ic_note);
            viewHolder.txtSubjectName.setText(event.getContentNote());
            viewHolder.lnTime.setVisibility(View.GONE);
            viewHolder.lnPlace.setVisibility(View.GONE);
            viewHolder.lnLecturer.setVisibility(View.GONE);
            viewHolder.lnClassID.setVisibility(View.GONE);
            viewHolder.lnSubject.setBackgroundColor(Color.rgb(221, 153, 255));
        }

        viewHolder.lnSubject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String arrAction[] = {"Edit", "Delete"};
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
                builderSingle.setItems(arrAction, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int j) {
                        String strAction = arrAction[j];
                        //final Event event = lis;
                        if (strAction.equals("Edit")) {
                            if (!event.getType().equals("note"))  {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                builder2.setTitle(R.string.edit_schedule);
                                View view = mainActivity.getLayoutInflater().inflate(R.layout.layout_dialog_edit, null);
                                final EditText edtTime = view.findViewById(R.id.edt_time);
                                final EditText edtPlace = view.findViewById(R.id.edt_place);
                                builder2.setView(view);
                                builder2.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int k) {
                                        new Update(Event.class)
                                                .set("time = ?, place = ?",
                                                        edtTime.getText().toString(),
                                                        edtPlace.getText().toString())
                                                .where("Id = ?", event.getId())
                                                .execute();
                                        mainActivity.showEventDetail(event.getDate());
                                    }
                                });
                                builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                AlertDialog dialog2 = builder2.create();
                                dialog2.show();
                            } else {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                builder2.setTitle(R.string.edit_note);
                                View view = mainActivity.getLayoutInflater().inflate(R.layout.layout_dialog_edit_note, null);
                                final EditText edtNote = view.findViewById(R.id.edt_note);
                                builder2.setView(view);
                                builder2.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int k) {
                                        new Update(Event.class)
                                                .set("content_note = ?", edtNote.getText().toString())
                                                .where("Id = ?", event.getId())
                                                .execute();
                                        mainActivity.showEventDetail(event.getDate());
                                    }
                                });
                                builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                AlertDialog dialog2 = builder2.create();
                                dialog2.show();
                            }
                        } else if (strAction.equals("Delete")) {
                            new Delete().from(Event.class).where("Id = ?", event.getId()).execute();
                            materialCalendarView.removeDecorators();
                            mainActivity.showEventDetail(event.getDate());
                            mainActivity.showEventDot();
                        }
                    }
                });
                builderSingle.show();
                return false;
            }
        });
    }
}