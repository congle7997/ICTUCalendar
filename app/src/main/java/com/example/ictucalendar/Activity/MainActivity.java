package com.example.ictucalendar.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.ictucalendar.Adapter.AdapterShowEvents;
import com.example.ictucalendar.Decorator.SubjectDecorator;
import com.example.ictucalendar.Interface.OnDatePickerListener;
import com.example.ictucalendar.Interface.OnListennerReadExcelStudent;
import com.example.ictucalendar.Interface.ReturnListLecturerName;
import com.example.ictucalendar.MultiThread.AsyncTaskGetListLecturer;
import com.example.ictucalendar.MultiThread.AsyncTaskReadExcelStudent;
import com.example.ictucalendar.Object.Event;
import com.example.ictucalendar.Object.Student;
import com.example.ictucalendar.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnDateSelectedListener, ReturnListLecturerName, OnListennerReadExcelStudent {


    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;
    MaterialCalendarView materialCalendarView;
    RecyclerView rcShowEvents;
    AdapterShowEvents customAdapterShowEvents;
    TextView txtName, txtUnit;
    ProgressDialog pdGetListLecturerName;
    ProgressDialog pdReadingDataLecturer;
    ProgressDialog pgReadingDataStudent;

    static final String TAG = MainActivity.class.getSimpleName();
    static final int REQUEST_CODE_STUDENT = 1;
    static final int REQUEST_CODE_LECTURER = 2;
    final String arrStringDay[] = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    final int arrIntDay[] = {DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY, DateTimeConstants.WEDNESDAY,
            DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY, DateTimeConstants.SUNDAY};
    String arrStartTimeSummer[] = {"06:30", "07:25", "08:25", "09:25", "10:20", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
    String arrEndTimeSummer[] = {"07:20", "08:15", "09:15", "10:15", "11:10", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};
    String arrStartTimeWinter[] = {"06:45", "07:40", "08:40", "09:40", "10:35", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
    String arrEndTimeWinter[] = {"07:35", "08:30", "09:30", "10:30", "11:25", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};
    String strDateSelected;
    String pathExcelLecturer;
    List<Event> listEventSelected;
    CalendarDay calDateSelected;
    String SHARED_PREFERENCE = "shared_preference";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

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
        showEventDot();
        showEventDetail(convertCalendarDayToString(CalendarDay.today()));
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

            String lecturer = "Lecturer";
            String student = "Student";
            if (Locale.getDefault().getDisplayLanguage().equals("Tiếng Việt")) {
                lecturer = "Giảng viên";
                student = "Sinh viên";
            }
            final String arrPerson[] = {lecturer, student};
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setIcon(R.drawable.ic_people_info);
            builderSingle.setTitle(R.string.you_are);
            final String finalLecturer = lecturer;
            final String finalStudent = student;
            builderSingle.setItems(arrPerson, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    String strPerson = arrPerson[i];
                    if (strPerson.equals(finalLecturer)) {
                        startActivityForResult(intent, REQUEST_CODE_LECTURER);
                    } else if (strPerson.equals(finalStudent)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(R.string.day_format);
                        builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivityForResult(intent, REQUEST_CODE_STUDENT);
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_ictu));


                    }
                }
            });
            builderSingle.show();
        } else if (id == R.id.nav_qr_code) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_extracurricular_point) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_STUDENT && resultCode == Activity.RESULT_OK) {
            String pathExcelFile = data.getStringExtra(SelectFileActivity.PATH);


            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);

            AsyncTaskReadExcelStudent asyncTaskReadExcelStudent = new AsyncTaskReadExcelStudent(this, sharedPreferences);
            asyncTaskReadExcelStudent.execute(pathExcelFile);

            pgReadingDataStudent = new ProgressDialog(MainActivity.this);
            String message = "Saving data ...";
            if (Locale.getDefault().getDisplayLanguage().equals("Tiếng Việt")) {
                message = "Đang lưu dữ liệu ...";
            }
            pgReadingDataStudent.setMessage(message);
            pgReadingDataStudent.setCanceledOnTouchOutside(false);
            pgReadingDataStudent.show();

        } else if (requestCode == REQUEST_CODE_LECTURER && resultCode == Activity.RESULT_OK) {
            pathExcelLecturer = data.getStringExtra(SelectFileActivity.PATH);

            AsyncTaskGetListLecturer asyncTaskGetListLecturer = new AsyncTaskGetListLecturer(this);
            asyncTaskGetListLecturer.execute(pathExcelLecturer);

            pdGetListLecturerName = new ProgressDialog(MainActivity.this);
            String message = "Getting list of lecturers ...";
            if (Locale.getDefault().getDisplayLanguage().equals("Tiếng Việt")) {
                message = "Đang lấy danh sách giảng viên ...";
            }
            pdGetListLecturerName.setMessage(message);
            pdGetListLecturerName.setCanceledOnTouchOutside(false);
            pdGetListLecturerName.show();
        } else if (requestCode == 7997) {
            //Toast.makeText(this, R.string.sent_developer, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setOnListennerReadExcelStudent() {
        materialCalendarView.removeDecorators();
        showEventDot();
        showEventDetail(strDateSelected);
        addDecoratorToDay();
        showInfoProfile();

        pgReadingDataStudent.dismiss();

        Toast.makeText(this, R.string.save_data_successfully, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void setReturnListLecturerName(List<String> listLecturerName) {
        selectLecturer(pathExcelLecturer, listLecturerName);
    }

    public void selectLecturer(final String pathExcelFile, final List<String> listLecturerName) {
        final String arrString[] = new String[listLecturerName.size()];
        for (int i = 0; i < listLecturerName.size(); i++) {
            arrString[i] = listLecturerName.get(i);
        }
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_people_info);
        builderSingle.setTitle(R.string.choose_your_name);
        builderSingle.setItems(arrString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int i) {
                pdGetListLecturerName.dismiss();

                pdReadingDataLecturer = new ProgressDialog(MainActivity.this);
                String message = "Saving data for " + listLecturerName.get(i);
                if (Locale.getDefault().getDisplayLanguage().equals("Tiếng Việt")) {
                    message = "Đang lưu dữ liệu của " + listLecturerName.get(i);
                }
                pdReadingDataLecturer.setMessage(message);
                pdReadingDataLecturer.setCanceledOnTouchOutside(false);
                pdReadingDataLecturer.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        readExcelLecturer(pathExcelFile, i);

                        materialCalendarView.removeDecorators();
                        showEventDot();
                        showEventDetail(strDateSelected);
                        addDecoratorToDay();

                        pdReadingDataLecturer.dismiss();

                        Toast.makeText(MainActivity.this, R.string.save_data_successfully, Toast.LENGTH_SHORT).show();
                    }
                }, 100);
            }
        });
        builderSingle.show();

    }

    public String getFirstWeek(Iterator<Row> rowIterator) {
        int rowWeek = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (row.getCell(1).getStringCellValue().contains("TUẦN")) {
                rowWeek++;
            }

            if (rowWeek == 3) {
                return row.getCell(1).getStringCellValue().substring(0, 8);
            }
        }

        return "str";
    }

    public void readExcelLecturer(String pathExcelFile, int i) {
        new Delete().from(Event.class).where("type = ?", "Lecturer").execute();
        new Delete().from(Event.class).where("type = ?", "Student").execute();

        int rowIndex = 0;
        int rowWeek = 0;
        List<String> listData = null;

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            FileInputStream excelFile = new FileInputStream(new File(pathExcelFile));
            HSSFWorkbook workbook = new HSSFWorkbook(excelFile);
            HSSFSheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = sheet.iterator();
            String currentWeek = getFirstWeek(sheet.iterator());

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowIndex++;

                if (rowIndex == 6) {
                    editor.putString("name", row.getCell(2).getStringCellValue());
                } else if (rowIndex == 7) {
                    editor.putString("unit", row.getCell(2).getStringCellValue());
                    editor.apply();

                    showInfoProfile();
                }

                if (rowIndex < 11) {
                    continue;
                }

                if (row.getCell(1).getStringCellValue().contains("TUẦN")) {
                    rowWeek++;
                }

                if (rowWeek <= 2) {
                    if (row.getCell(1).getStringCellValue().contains("TUẦN")) {
                        listData = new ArrayList<>();
                    }
                    if (row.getCell(1).getCellTypeEnum() == CellType.BLANK) {
                        saveData(listData);
                    }

                    Iterator<Cell> cellIterator = row.iterator();
                    String rowData = "";

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        if (cell.getCellTypeEnum() == CellType.STRING) {
                            rowData += cell.getStringCellValue();
                            rowData += "---";
                        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                            rowData += String.valueOf((int) cell.getNumericCellValue());
                            rowData += "---";
                        }
                    }

                    listData.add(rowData);
                }

                if (rowWeek >= 3) {
                    if (!row.getCell(1).getStringCellValue().substring(0, 8).equals(currentWeek)
                            && row.getCell(1).getStringCellValue().contains("TUẦN")) {
                        saveData(listData);
                    }
                    if (row.getCell(1).getStringCellValue().contains("TUẦN")) {
                        listData = new ArrayList<>();
                    }

                    Iterator<Cell> cellIterator = row.iterator();
                    String rowData = "";

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        if (cell.getCellTypeEnum() == CellType.STRING) {
                            rowData += cell.getStringCellValue();
                            rowData += "---";
                        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                            rowData += String.valueOf((int) cell.getNumericCellValue());
                            rowData += "---";
                        }
                    }

                    listData.add(rowData);
                }

                /* dữ liệu của tuần cuối đã được đưa vào listData
                nên chỉ cần gọi hàm saveData() khi nó là dòng cuối cùng */
                if (!rowIterator.hasNext()) {
                    saveData(listData);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveData(List<String> listData) {
        try {
            int posStart = listData.get(0).indexOf("(") + 1;
            int posEnd = listData.get(0).indexOf("đến") - 1;
            String strStartDate = listData.get(0).substring(posStart, posEnd);
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            DateTime startDate = dateTimeFormatter.parseDateTime(strStartDate);
            for (String rowData1 : listData) {
                // không lấy dữ liệu dòng đầu tiên mỗi listData
                if (rowData1.contains(")")) {
                    String rowDataSplit[] = rowData1.split("---");
                    Event event = new Event();
                    SimpleDateFormat formatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    SimpleDateFormat formatOutput = new SimpleDateFormat("dd/MM/yyyy");
                    String day = rowDataSplit[3];
                    Date date = formatInput.parse(String.valueOf(startDate.plusDays(Integer.parseInt(day) - 2)));
                    String dateFormatted = formatOutput.format(date);
                    event.setDate(dateFormatted);

                    String subjectName = rowDataSplit[1].substring(0, rowDataSplit[1].indexOf("-"));
                    event.setSubjectName(subjectName);

                    String time = rowDataSplit[4].replace(",", ", ");
                    String arrTime[] = rowDataSplit[4].split(",");
                    int startTime = Integer.parseInt(arrTime[0]);
                    int endTime = Integer.parseInt(arrTime[arrTime.length - 1]);
                    if (isSummer(dateFormatted)) {
                        event.setTime(time + " (" + arrStartTimeSummer[startTime - 1] + " - " + arrEndTimeSummer[endTime - 1] + ")");

                    } else {
                        event.setTime(time + " (" + arrStartTimeWinter[startTime - 1] + " - " + arrEndTimeWinter[endTime - 1] + ")");
                    }

                    event.setPlace(rowDataSplit[5]);

                    event.setType("Lecturer");

                    event.save();

                    //Log.d(TAG, "saveData: " + event.getDate() + " - " + event.getSubjectName());
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private boolean isDay(String strCellValue) {
        for (String day : arrStringDay) {
            if (strCellValue.equals(day)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSummer(String strDate) {
        String strDateSplit[] = strDate.split("/");
        int day = Integer.parseInt(strDateSplit[0]);
        int month = Integer.parseInt(strDateSplit[1]);

        if (month >= 4 && month <= 10) {
            if (month == 4 && day < 15) {
                return false;
            } else if (month == 10 && day > 15) {
                return false;
            }
            // Log.d(TAG, "isSummer: " + month + " " + day);
            return true;
        }

        return false;
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
                displayDialogNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayDialogNote() {
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
                event.setType("Note");
                event.setContentNote(strNote);
                event.setDate(btnDatePicked.getText().toString());
                // khi load dữ liệu ta lấy dữ liệu đã được sắp xếp theo time
                event.setTime("9999");
                event.save();


                materialCalendarView.removeDecorators();
                showEventDot();
                addDecoratorToDay();
                Toast.makeText(MainActivity.this, R.string.save_note_successfully, Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_decline));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_accept));
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
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_decline));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_accept));
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

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        txtName.setText(sharedPreferences.getString("name", "?"));
        //txtStudentCode.setText(sharedPreferences.getString("student_code", "?"));
        txtUnit.setText(sharedPreferences.getString("unit", "?"));
    }

    public void showEventDetail(String strDate) {
        listEventSelected = getListEventSelected(strDate);
        customAdapterShowEvents = new AdapterShowEvents(MainActivity.this, listEventSelected);
        rcShowEvents.setAdapter(customAdapterShowEvents);
        customAdapterShowEvents.notifyDataSetChanged();
    }

    public void showEventDot() {
        List<Event> listEvent = new Select().from(Event.class).execute();
        List<CalendarDay> listCalendarDay = new ArrayList<>();

        String type = "";
        for (Event event : listEvent) {
            if (event.getType().equals("Lecturer")) {
                type = "Lecturer";
                break;
            } else if (event.getType().equals("Student")) {
                type = "Student";
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

        int countSubject;
        int start;
        int arrColor[];
        for (CalendarDay calendarDay : listCalendarDay) {
            countSubject = countDate(calendarDay, type);
            start = 0;

            if (countSubject > 4) {
                countSubject = 4;
            }

            if (countNote(calendarDay)) {
                countSubject++;
                start++;
            }

            arrColor = new int[countSubject];

            if (countNote(calendarDay)) {
                arrColor[0] = colorNote;
            }

            for (int i = start; i < countSubject; i++) {
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
                .where("type = ?", "Note")
                .where("date = ?", strDate)
                .execute();

        return listEvent.size() > 0;
    }
}
