package com.example.ictucalendar.Activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.example.ictucalendar.Adapter.AdapterShowEvents;
import com.example.ictucalendar.Decorator.SubjectDecorator;
import com.example.ictucalendar.Interface.OnDatePickerListener;
import com.example.ictucalendar.Interface.OnListennerReadExcelLecturer;
import com.example.ictucalendar.Interface.OnListennerReadExcelStudent;
import com.example.ictucalendar.Interface.ReturnListLecturer;
import com.example.ictucalendar.MultiThread.AsyncTaskCreateEventAPI;
import com.example.ictucalendar.MultiThread.AsyncTaskGetListLecturer;
import com.example.ictucalendar.MultiThread.AsyncTaskReadExcelLecturer;
import com.example.ictucalendar.MultiThread.AsyncTaskReadExcelStudent;
import com.example.ictucalendar.Object.Event;
import com.example.ictucalendar.R;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnDateSelectedListener, ReturnListLecturer, OnListennerReadExcelStudent, OnListennerReadExcelLecturer {


    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;
    MaterialCalendarView materialCalendarView;
    RecyclerView rcShowEvents;
    AdapterShowEvents customAdapterShowEvents;
    TextView txtName, txtUnit;
    ProgressDialog pdGetListLecturer;
    ProgressDialog pdReadingDataLecturer;
    ProgressDialog pdReadingDataStudent;

    static final String TAG = "my_MainActivity";
    static final int REQUEST_CODE_IMPORT = 1;
    String strDateSelected;
    String pathExcelLecturer;
    List<Event> listEventSelected;
    CalendarDay calDateSelected;
    SharedPreferences sharedPreferences;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    GoogleAccountCredential credential;
    com.google.api.services.calendar.Calendar service;
    String calendarID;
    boolean isAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("shared_preference", MODE_PRIVATE);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS}, 1);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        materialCalendarView = findViewById(R.id.material_calendar_view);
        rcShowEvents = findViewById(R.id.rc_show_events);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcShowEvents.setLayoutManager(layoutManager);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setMinimumDate(CalendarDay.from(2015, 1, 1))
                .setMaximumDate(CalendarDay.from(2025, 1, 1))
                .commit();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, R.string.not_available, Toast.LENGTH_LONG).show();
            }
        });


        materialCalendarView.setSelectionColor(Color.rgb(0, 115, 186));
        showEventDetail(convertCalendarDayToString(CalendarDay.today()));
        showEventDot();
        addDecoratorToDay();
        showInfoProfile();

        calDateSelected = new CalendarDay(materialCalendarView.getSelectedDate().getYear(),
                materialCalendarView.getSelectedDate().getMonth(),
                materialCalendarView.getSelectedDate().getDay());
        strDateSelected = convertCalendarDayToString(calDateSelected);

        navigationView.setNavigationItemSelectedListener(this);
        materialCalendarView.setOnDateChangedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_import_excel) {
            final Intent intent = new Intent(MainActivity.this, SelectFileActivity.class);
            startActivityForResult(intent, REQUEST_CODE_IMPORT);
        } else if (id == R.id.nav_sync) {
            if (checkNetwork()) {
                HttpTransport transport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                String[] SCOPES = {CalendarScopes.CALENDAR};
                credential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES));
                service = new com.google.api.services.calendar.Calendar.Builder(
                        transport, jsonFactory, credential).build();

                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.require_internet);
                builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } else if (id == R.id.nav_qr_code) {

        } else if (id == R.id.nav_extracurricular_point) {

        } else if (id == R.id.nav_setting) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_opinion_feedback) {
            boolean check = true;
            try {
                PackageManager packageManager = getPackageManager();
                packageManager.getPackageInfo("com.google.android.gm", 0);
            } catch (PackageManager.NameNotFoundException e) {
                check = false;
            }

            if (check) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "congle7997@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "ICTU Calendar");
                startActivityForResult(Intent.createChooser(intent, "Send email!"), 7997);
            } else {
                Toast.makeText(this, R.string.not_installed_gmail, Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_information) {
            Intent intent = new Intent(MainActivity.this, InformationActivity.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        String accountName = "";
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode != RESULT_CANCELED) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            credential.setSelectedAccountName(accountName);

            isAuth = false;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        CalendarList calendarList = service.calendarList().list().setPageToken(null).execute();
                        isAuth = true;
                    } catch (UserRecoverableAuthIOException userRecoverableException) {
                        Log.d(TAG, "UserRecoverableAuthIOException: ");
                        // nếu chưa được xác thực sẽ nhảy catch này
                        startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    showAlertUploadNote();
                    super.onPostExecute(aVoid);
                }
            }.execute();

        } else if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == RESULT_CANCELED) {
                isAuth = false;
                Toast.makeText(MainActivity.this, R.string.no_access, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK && data.getExtras() != null) {
                isAuth = true;
                showAlertUploadNote();
            }
        } else if (requestCode == REQUEST_CODE_IMPORT && resultCode == RESULT_OK) {
            pathExcelLecturer = data.getStringExtra(SelectFileActivity.PATH);
            int rowIndex = 0;
            try {
                String pathExcelFile = pathExcelLecturer;

                FileInputStream excelFile = new FileInputStream(new File(pathExcelFile));
                HSSFWorkbook workbook = new HSSFWorkbook(excelFile);
                HSSFSheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    rowIndex++;
                    if (rowIndex == 4) {
                        Iterator<Cell> cellIterator = row.iterator();
                        Cell cell = cellIterator.next();
                        String rowData = cell.getStringCellValue();

                        if (rowData.equals("Sinh viên:")) {
                            Row row2 = rowIterator.next();
                            if (row2.getCell(0).getStringCellValue().equals("")) {
                                showAlertFormat();
                            } else {
                                pdReadingDataStudent = new ProgressDialog(MainActivity.this);
                                pdReadingDataStudent.setMessage(getResources().getString(R.string.saving_data));
                                pdReadingDataStudent.setCanceledOnTouchOutside(false);
                                pdReadingDataStudent.show();

                                new AsyncTaskReadExcelStudent(pathExcelFile, this, sharedPreferences).execute();
                            }
                        } else if (rowData.equals("LỊCH GIẢNG DẠY GIẢNG VIÊN")) {
                            pdGetListLecturer = new ProgressDialog(MainActivity.this);
                            pdGetListLecturer.setMessage(getResources().getString(R.string.getting_lecturers));
                            pdGetListLecturer.setCanceledOnTouchOutside(false);
                            pdGetListLecturer.show();

                            new AsyncTaskGetListLecturer(pathExcelLecturer, this).execute();
                        } else {
                            showAlertFormat();
                        }

                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showAlertUploadNote() {
        if (isAuth) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.notification);
            builder.setMessage(R.string.upload_note);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showAlertPickTimeAlarm(true);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showAlertPickTimeAlarm(false);
                }
            });
            AlertDialog alertDialog2 = builder.create();
            alertDialog2.show();
        }
    }

    public void showAlertFormat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.day_format);
        builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_ictu));
    }


    @Override
    public void setReturnListLecturer(final List<String> listLecturer) {
        final String arrString[] = new String[listLecturer.size()];
        for (int i = 0; i < listLecturer.size(); i++) {
            arrString[i] = listLecturer.get(i);
        }
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_people_info);
        builderSingle.setTitle(R.string.choose_lecturer_name);
        builderSingle.setItems(arrString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int i) {
                pdGetListLecturer.dismiss();

                pdReadingDataLecturer = new ProgressDialog(MainActivity.this);
                pdReadingDataLecturer.setMessage(getResources().getString(R.string.saving_data) + ": " + listLecturer.get(i));
                pdReadingDataLecturer.setCanceledOnTouchOutside(false);
                pdReadingDataLecturer.show();

                new AsyncTaskReadExcelLecturer(pathExcelLecturer, MainActivity.this, sharedPreferences).execute(i);
            }
        });
        builderSingle.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                pdGetListLecturer.dismiss();
            }
        });
        builderSingle.show();
    }

    @Override
    public void setOnListennerReadExcelLecturer() {
        materialCalendarView.removeDecorators();
        showEventDot();
        showEventDetail(strDateSelected);
        addDecoratorToDay();
        showInfoProfile();

        pdReadingDataLecturer.dismiss();

        Toast.makeText(this, R.string.save_data_successfully, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setOnListennerReadExcelStudent() {
        materialCalendarView.removeDecorators();
        showEventDetail(strDateSelected);
        showEventDot();
        addDecoratorToDay();
        showInfoProfile();

        pdReadingDataStudent.dismiss();

        Toast.makeText(this, R.string.save_data_successfully, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
        strDateSelected = convertCalendarDayToString(calendarDay);
        showEventDetail(strDateSelected);
    }

    public static List<Event> getListEventSelected(String formattedDate) {
        return new Select()
                .from(Event.class)
                .where("date = ?", formattedDate)
                .orderBy("type DESC")
                .orderBy("time ASC")
                .execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_note:
                showDialogNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialogNote() {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.layout_dialog_note, null);

        final EditText edtInputNote = view.findViewById(R.id.edt_input_note);
        final Button btnDatePicked = view.findViewById(R.id.btn_date_picker);

        calDateSelected = new CalendarDay(materialCalendarView.getSelectedDate().getYear(),
                materialCalendarView.getSelectedDate().getMonth(),
                materialCalendarView.getSelectedDate().getDay());
        btnDatePicked.setText(convertCalendarDayToString(calDateSelected));

        btnDatePicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(new OnDatePickerListener() {
                    @Override
                    public void setOnDatePickerListener(String strDatePicked) {
                        btnDatePicked.setText(strDatePicked);
                    }
                });
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.note);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strNote = edtInputNote.getText().toString();
                Event event = new Event();
                event.setType("note");
                event.setContentNote(strNote);
                event.setDate(btnDatePicked.getText().toString());
                // khi load dữ liệu ta lấy dữ liệu đã được sắp xếp theo time
                event.setTime("9999()");
                event.save();


                materialCalendarView.removeDecorators();
                showEventDot();
                addDecoratorToDay();
                showEventDetail(convertCalendarDayToString(calDateSelected));
                Toast.makeText(MainActivity.this, getString(R.string.save_note_successfully) +
                        "\n" + btnDatePicked.getText().toString(), Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void pickDate(final OnDatePickerListener onDatePickerListener) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.layout_dialog_date_picker, null);

        final DatePicker datePicker = view.findViewById(R.id.date_picker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            datePicker.setFirstDayOfWeek(Calendar.MONDAY);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int day = datePicker.getDayOfMonth();
                String strDay = String.valueOf(day);
                int month = datePicker.getMonth() + 1;
                String strMonth = String.valueOf(month);
                if (day < 10) {
                    strDay = "0" + day;
                }
                if (month < 10) {
                    strMonth = "0" + month;
                }
                int year = datePicker.getYear();
                String strDatePicked = strDay + "/" + strMonth + "/" + year;
                // gọi callback trả dữ liệu ngày về displayDialogNote()
                onDatePickerListener.setOnDatePickerListener(strDatePicked);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public String convertCalendarDayToString(CalendarDay calendarDay) {
        int day = calendarDay.getDay();
        String strDay = String.valueOf(day);
        int month = calendarDay.getMonth() + 1;
        String strMonth = String.valueOf(month);
        if (day < 10) {
            strDay = "0" + day;
        }
        if (month < 10) {
            strMonth = "0" + month;
        }
        int year = calendarDay.getYear();

        String strDate = strDay + "/" + strMonth + "/" + year;

        return strDate;
    }

    public void addDecoratorToDay() {
        // vòng tròn của ngày hiện tại chỉ xuất hiện khi ngày hiện tại được chọn
        materialCalendarView.setDateSelected(CalendarDay.today(), true);

        materialCalendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                if (CalendarDay.today().equals(day)) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_current_date));
            }
        });
    }

    public void showInfoProfile() {
        View view = navigationView.getHeaderView(0);
        txtName = view.findViewById(R.id.txt_name);
        //txtStudentCode = view.fcindViewById(R.id.txt_student_code);
        txtUnit = view.findViewById(R.id.txt_unit);

        //SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        txtName.setText(sharedPreferences.getString("name", "?"));
        //txtStudentCode.setText(sharedPreferences.getString("student_code", "?"));
        txtUnit.setText(sharedPreferences.getString("unit", "?"));
    }

    public void showEventDetail(String strDate) {
        listEventSelected = getListEventSelected(strDate);
        customAdapterShowEvents = new AdapterShowEvents(this, listEventSelected, this, materialCalendarView);
        rcShowEvents.setAdapter(customAdapterShowEvents);
        customAdapterShowEvents.notifyDataSetChanged();
    }

    public void showEventDot() {
        List<Event> listEvent = new Select().from(Event.class).execute();
        List<CalendarDay> listCalendarDay = new ArrayList<>();

        String type = "";
        for (Event event : listEvent) {
            if (event.getType().equals("lecturer")) {
                type = "lecturer";
                break;
            } else if (event.getType().equals("student")) {
                type = "student";
                break;
            }
        }

        for (Event event : listEvent) {
            String eventSplit[] = event.getDate().split("/");
            CalendarDay calendarDay = new CalendarDay(Integer.parseInt(eventSplit[2]), Integer.parseInt(eventSplit[1]), Integer.parseInt(eventSplit[0]));
            listCalendarDay.add(calendarDay);
        }

        int colorSubject = Color.rgb(102, 153, 255);
        int colorNote = Color.rgb(204, 102, 255);

        int countSchedule;
        int start;
        int arrColor[];
        for (CalendarDay calendarDay : listCalendarDay) {
            countSchedule = countDate(calendarDay, type);
            start = 0;

            if (countSchedule > 4) {
                countSchedule = 4;
            }

            if (countNote(calendarDay)) {
                countSchedule++;
                start++;
            }

            arrColor = new int[countSchedule];

            if (countNote(calendarDay)) {
                arrColor[0] = colorNote;
            }

            for (int i = start; i < countSchedule; i++) {
                arrColor[i] = colorSubject;
            }

            materialCalendarView.addDecorator(new SubjectDecorator(calendarDay, arrColor, MainActivity.this));
        }
    }

    public int countDate(CalendarDay calendarDay, String type) {
        String day = String.valueOf(calendarDay.getDay());
        if (calendarDay.getDay() < 10) {
            day = "0" + calendarDay.getDay();
        }
        String month = String.valueOf(calendarDay.getMonth());
        if (calendarDay.getMonth() < 10) {
            month = "0" + calendarDay.getMonth();
        }
        String year = String.valueOf(calendarDay.getYear());

        String strDate = day + "/" + month + "/" + year;

        List<Event> listEvent = new Select().from(Event.class)
                .where("type = ?", type)
                .where("date = ?", strDate)
                .execute();

        return listEvent.size();
    }

    public boolean countNote(CalendarDay calendarDay) {
        String day = String.valueOf(calendarDay.getDay());
        if (calendarDay.getDay() < 10) {
            day = "0" + calendarDay.getDay();
        }
        String month = String.valueOf(calendarDay.getMonth());
        if (calendarDay.getMonth() < 10) {
            month = "0" + calendarDay.getMonth();
        }
        String year = String.valueOf(calendarDay.getYear());

        String strDate = day + "/" + month + "/" + year;

        List<Event> listEvent = new Select().from(Event.class)
                .where("type = ?", "note")
                .where("date = ?", strDate)
                .execute();

        return listEvent.size() > 0;
    }

    public void upload(final int timeAlarm, boolean hasNote) {
        final String type;
        if (isLecturer()) {
            type = "lecturer";
        } else {
            type = "student";
        }
        final List<Event> listEvent = new Select()
                .from(Event.class)
                .where("type = ?", type)
                .execute();

        if (hasNote) {
            List<Event> listNote = new Select()
                    .from(Event.class)
                    .where("type = ?", "note")
                    .execute();
            for (Event note : listNote) {
                listEvent.add(note);
            }
        }


        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.uploading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(listEvent.size());
        progressDialog.show();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Xoá lịch cũ
                CalendarList calendarList = null;
                try {
                    calendarList = service.calendarList().list().setPageToken(null).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<CalendarListEntry> items = calendarList.getItems();
                for (CalendarListEntry calendarListEntry : items) {
                    if (calendarListEntry.getSummary().equals("ICTU Calendar")) {
                        try {
                            service.calendarList().delete(calendarListEntry.getId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Tạo lịch mới
                com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                calendar.setSummary("ICTU Calendar");
                com.google.api.services.calendar.model.Calendar createdCalendar = null;
                try {
                    createdCalendar = service.calendars().insert(calendar).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                calendarID = createdCalendar.getId();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                new AsyncTaskCreateEventAPI(service, calendarID, listEvent, timeAlarm, progressDialog, MainActivity.this).execute();
            }
        }.execute();
    }

    public boolean isLecturer() {
        List<Event> listEvent = new Select()
                .from(Event.class)
                .orderBy("type ASC")
                .limit(1)
                .execute();

        if (listEvent.size() > 0 && listEvent.get(0).getType().equals("lecturer")) {
            return true;
        } else {
            return false;
        }
    }

    public void showAlertPickTimeAlarm(final boolean hasNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.time_notification);
        builder.setCancelable(false);
        final View view = getLayoutInflater().inflate(R.layout.layout_dialog_time_alarm, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        RadioGroup rgTimeAlarm = view.findViewById(R.id.rg_time_alarm);

        rgTimeAlarm.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb1:
                        upload(-1, hasNote);
                        dialog.dismiss();
                        break;
                    case R.id.rb2:
                        upload(0, hasNote);
                        dialog.dismiss();
                        break;
                    case R.id.rb3:
                        upload(15, hasNote);
                        dialog.dismiss();
                        break;
                    case R.id.rb4:
                        upload(30, hasNote);
                        dialog.dismiss();
                        break;
                    case R.id.rb5:
                        upload(45, hasNote);
                        dialog.dismiss();
                        break;
                    case R.id.rb6:
                        dialog.dismiss();
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                        builder2.setTitle(R.string.input_time);
                        View view = getLayoutInflater().inflate(R.layout.layout_dialog_time_alarm_2, null);
                        builder2.setView(view);

                        final EditText edtTimeAlarm = view.findViewById(R.id.edt_time_alarm);
                        builder2.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                upload(Integer.parseInt(edtTimeAlarm.getText().toString()), hasNote);
                            }
                        });

                        AlertDialog dialog2 = builder2.create();
                        dialog2.show();
                        break;
                }
            }
        });
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}