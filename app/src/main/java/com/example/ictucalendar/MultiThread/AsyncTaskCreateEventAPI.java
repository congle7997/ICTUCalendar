package com.example.ictucalendar.MultiThread;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.ictucalendar.Activity.MainActivity;
import com.example.ictucalendar.Object.Event;
import com.example.ictucalendar.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AsyncTaskCreateEventAPI extends AsyncTask<Void, Void, Void> {
    com.google.api.services.calendar.Calendar service;
    String calendarID;
    List<Event> listEvent;
    int timeAlarm;
    ProgressDialog progressDialog;
    Context context;

    String TAG = "CreateEventAPI";
    boolean isCancel = false;
    int i = 0;
    int max;
    String reason;

    public AsyncTaskCreateEventAPI(com.google.api.services.calendar.Calendar service,
                                   String calendarID, List<Event> listEvent, int timeAlarm,
                                   ProgressDialog progressDialog, Context context) {
        this.service = service;
        this.calendarID = calendarID;
        this.listEvent = listEvent;
        this.timeAlarm = timeAlarm;
        this.progressDialog = progressDialog;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        max = listEvent.size();
        progressDialog.setMax(listEvent.size());

        for (final Event myEvent : listEvent) {
            com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
            String strDate = myEvent.getDate();
            String strTime = myEvent.getTime().substring(myEvent.getTime().indexOf("(") + 1, myEvent.getTime().indexOf(")"));

            if (myEvent.getType().equals("note")) {
                event.setSummary(myEvent.getContentNote());

                String strStart = strDate.substring(6, strDate.length()) + "-" + strDate.substring(3, 5) + "-" + strDate.substring(0, 2);
                String strEnd = strDate.substring(6, strDate.length()) + "-" + strDate.substring(3, 5) + "-" + strDate.substring(0, 2);
                com.google.api.client.util.DateTime startDateTime = new com.google.api.client.util.DateTime(strStart);
                EventDateTime start = new EventDateTime()
                        .setDate(DateTime.parseRfc3339(strStart));
                event.setStart(start);
                com.google.api.client.util.DateTime endDateTime = new com.google.api.client.util.DateTime(strEnd);
                EventDateTime end = new EventDateTime()
                        .setDate(DateTime.parseRfc3339(strEnd));
                event.setEnd(end);
                event.setTransparency("transparent");
            } else {
                event.setSummary(myEvent.getSubjectName());
                event.setLocation(myEvent.getPlace());
                event.setDescription(myEvent.getLecturer());

                String strStart = strDate.substring(6, strDate.length()) + "-" + strDate.substring(3, 5) + "-" + strDate.substring(0, 2)
                        + "T" + strTime.substring(0, 5) + ":00+07:00";
                String strEnd = strDate.substring(6, strDate.length()) + "-" + strDate.substring(3, 5) + "-" + strDate.substring(0, 2)
                        + "T" + strTime.substring(8, 13) + ":00+07:00";
                com.google.api.client.util.DateTime startDateTime = new com.google.api.client.util.DateTime(strStart);
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime);
                event.setStart(start);
                com.google.api.client.util.DateTime endDateTime = new com.google.api.client.util.DateTime(strEnd);
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime);
                event.setEnd(end);

                EventReminder[] reminderOverrides;
                if (timeAlarm == -1) {
                    reminderOverrides = new EventReminder[]{};
                    com.google.api.services.calendar.model.Event.Reminders reminders = new com.google.api.services.calendar.model.Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(Arrays.asList(reminderOverrides));
                    event.setReminders(reminders);
                } else {
                    reminderOverrides = new EventReminder[]{
                            new EventReminder().setMethod("popup").setMinutes(timeAlarm)};
                    com.google.api.services.calendar.model.Event.Reminders reminders = new com.google.api.services.calendar.model.Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(Arrays.asList(reminderOverrides));
                    event.setReminders(reminders);
                }
            }

            try {
                service.events().insert(calendarID, event).execute();
            } catch (UserRecoverableAuthIOException e) {
                // startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
                e.printStackTrace();
            }

            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK && !keyEvent.isCanceled()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.notification);
                        builder.setMessage(R.string.cancel_syn);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                isCancel = true;
                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }
                    return true;
                }
            });

            progressDialog.setProgress(i++);

            if (isCancel == true) {
                reason = context.getResources().getString(R.string.canceled_sync);
                break;
            }

            if (!checkNetwork()) {
                reason = context.getResources().getString(R.string.internet_problem);
                break;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        progressDialog.dismiss();

        if (i == max) {
            Toast.makeText(context, R.string.sync_successful, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, reason + "\n" + context.getResources().getString(R.string.synced) + ": " + i +
                    "/" + max + " " + context.getResources().getString(R.string.event), Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
