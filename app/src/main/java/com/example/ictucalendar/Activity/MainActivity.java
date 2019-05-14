package com.example.ictucalendar.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.ictucalendar.Adapter.CustomAdapterShowEvents;
import com.example.ictucalendar.Decorator.SubjectDecorator;
import com.example.ictucalendar.Interface.OnDatePickerListener;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;
    MaterialCalendarView materialCalendarView;
    RecyclerView rcShowEvents;
    CustomAdapterShowEvents customAdapterShowEvents;
    TextView txtStudentName, txtStudentCode, txtClass;


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1;
    private final String arrStringDay[] = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private final int arrIntDay[] = {DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY, DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY, DateTimeConstants.SUNDAY};
    String strDateSelected;
    List<Event> listEventSelected;
    CalendarDay calDateSelected;
    String SHARED_PREFES_STUDENT = "shared_prefes_student";


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
                Toast.makeText(MainActivity.this, R.string.comming_soon, Toast.LENGTH_LONG).show();
            }
        });


        materialCalendarView.setSelectionColor(Color.rgb(0, 115, 186));
        showEventDot();
        showEventDetail(convertCalendarDayToString(CalendarDay.today()));
        addDecoratorToDay();
        showInfoProfile();

        navigationView.setNavigationItemSelectedListener(this);
        materialCalendarView.setOnDateChangedListener(this);

        Toast.makeText(this, "test 123", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_import_excel_lecturers) {
            Toast.makeText(this, R.string.comming_soon, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_import_excel_student) {
            Intent intent = new Intent(MainActivity.this, SelectFileActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        } else if (id == R.id.nav_qr_code) {
            Toast.makeText(this, R.string.comming_soon, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_setting) {
            Toast.makeText(this, R.string.comming_soon, Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, R.string.comming_soon, Toast.LENGTH_LONG).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            new Delete().from(Event.class).where("type =?", "Subject").execute();

            String pathExcelFile = data.getStringExtra(SelectFileActivity.PATH);
            readExcel(pathExcelFile);

            materialCalendarView.removeDecorators();
            showEventDot();

            calDateSelected = new CalendarDay(materialCalendarView.getSelectedDate().getYear(),
                    materialCalendarView.getSelectedDate().getMonth(),
                    materialCalendarView.getSelectedDate().getDay());
            showEventDetail(convertCalendarDayToString(calDateSelected));

            addDecoratorToDay();
        } else if (requestCode == 7997) {
            //Toast.makeText(this, R.string.sent_developer, Toast.LENGTH_LONG).show();
        }
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
        txtStudentName = view.findViewById(R.id.txt_student_name);
        //txtStudentCode = view.findViewById(R.id.txt_student_code);
        txtClass = view.findViewById(R.id.txt_class);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFES_STUDENT, Context.MODE_PRIVATE);
        txtStudentName.setText(sharedPreferences.getString("student_name", "?"));
        //txtStudentCode.setText(sharedPreferences.getString("student_code", "?"));
        txtClass.setText(sharedPreferences.getString("class", "?"));
    }


    public void readExcel(String pathExcelFile) {
        Student student = new Student();

        int rowExcelFile = 0;
        int indexArrDay = 0;
        String strDay = "";
        List<Event> listEvent = new ArrayList<>();

        try {
            FileInputStream excelFile = new FileInputStream(new File(pathExcelFile));
            HSSFWorkbook workbook = new HSSFWorkbook(excelFile);
            HSSFSheet sheet = workbook.getSheetAt(0);
            // Tạo và gán 1 đối tượng Iterator để lặp các hàng từ đầu tới cuối
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                // Tạo và gán 1 đối tượng Row (1 dòng)
                Row row = rowIterator.next();
                // Lấy dữ liệu bắt đầu từ hàng thứ 7
                rowExcelFile++;
                //Log.d(TAG, "rowExcelFile: " + rowExcelFile);
                if (rowExcelFile == 4) {
                    Iterator<Cell> cellIterator = row.iterator();
                    Cell cell;
                    String rowStudentInfo = "";
                    while (cellIterator.hasNext()) {
                        cell = cellIterator.next();

                        if (cell.getCellTypeEnum() == CellType.STRING) {
                            rowStudentInfo += cell.getStringCellValue();
                            rowStudentInfo += "---";
                        }
                    }
                    String rowStudentInfoSplit[] = rowStudentInfo.split("---");
                    student.setStudentName(rowStudentInfoSplit[1]);
                    student.setStudenCode(rowStudentInfoSplit[3]);
                    student.setStudentClass(rowStudentInfoSplit[5]);

                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFES_STUDENT, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("student_name", rowStudentInfoSplit[1]);
                    editor.putString("student_code", rowStudentInfoSplit[3]);
                    editor.putString("class", rowStudentInfoSplit[5]);
                    editor.apply();

                    showInfoProfile();
                }

                if (rowExcelFile < 7) {
                    continue;
                }
                // Tạo và gán 1 đối tượng Iterator để lặp các cột từ đầu tới cuối
                Iterator<Cell> cellIterator = row.iterator();
                // Tạo và gán 1 đối tượng Cell (1 ô)
                Cell cell = cellIterator.next();
                String stringCellValue = cell.getStringCellValue();
                if (isDay(stringCellValue)) {
                    strDay = stringCellValue;

                    if (cell.getStringCellValue().equals("T2")) {
                        indexArrDay = 0;
                    } else if (cell.getStringCellValue().equals("T3")) {
                        indexArrDay = 1;
                    } else if (cell.getStringCellValue().equals("T4")) {
                        indexArrDay = 2;
                    } else if (cell.getStringCellValue().equals("T5")) {
                        indexArrDay = 3;
                    } else if (cell.getStringCellValue().equals("T6")) {
                        indexArrDay = 4;
                    } else if (cell.getStringCellValue().equals("T7")) {
                        indexArrDay = 5;
                    } else if (cell.getStringCellValue().equals("CN")) {
                        indexArrDay = 6;
                    }
                }

                // đọc dữ liệu 1 hàng (từng cột 1 từ trái qua phải)
                String rowData = "";
                while (cellIterator.hasNext()) {
                    cell = cellIterator.next();

                    if (cell.getCellTypeEnum() == CellType.STRING) {
                        rowData += cell.getStringCellValue();
                        rowData += "---";
                    }
                }

                //Log.d(TAG, "rowData: " + rowData);
                String rowDataSplit[] = rowData.split("---");
                //Log.d(TAG, "Range time: " + rowDataSplit[1]);
                String rangeTime[] = rowDataSplit[1].split("->");

                String strStartDate = rangeTime[0];
                String strEndDate = rangeTime[1];

                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
                DateTime startDate = dateTimeFormatter.parseDateTime(strStartDate);
                DateTime endDate = dateTimeFormatter.parseDateTime(strEndDate);

                // danh sách các ngày trong khoảng thời gian của thứ cần tìm
                List<String> listDate = new ArrayList<>();

                if (strDay.equals(arrStringDay[indexArrDay])) {
                    startDate = startDate.plusDays(indexArrDay);
                    while (startDate.isBefore(endDate.plusDays(1))) {
                        if (startDate.getDayOfWeek() == arrIntDay[indexArrDay]) {
                            SimpleDateFormat formatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            SimpleDateFormat formatOutput = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = null;
                            try {
                                date = formatInput.parse(String.valueOf(startDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String formattedDate = formatOutput.format(date);

                            listDate.add(formattedDate);
                        }
                        startDate = startDate.plusWeeks(1);
                    }
                }

                // sau khi tính được ngày học cụ thể, ứng với mỗi ngày ta sẽ tạo ra được 1 đối tượng
                String subjectName;
                String timeSplit[];
                int startTime;
                int endTime;
                String arrStartTimeSummer[] = {"06:30", "07:25", "08:25", "09:25", "10:20", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
                String arrEndTimeSummer[] = {"07:20", "08:15", "09:15", "10:15", "11:10", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};
                String arrStartTimeWinter[] = {"06:45", "07:40", "08:40", "09:40", "10:35", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
                String arrEndTimeWinter[] = {"07:35", "08:30", "09:30", "10:30", "11:25", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};
                for (String date : listDate) {
                    Event event = new Event();
                    if (student.getStudenCode().contains("DTC")) {
                        timeSplit = rowDataSplit[0].split("->");
                        startTime = Integer.parseInt(timeSplit[0]);
                        endTime = Integer.parseInt(timeSplit[1]);
                        String cutTime = "";
                        for (int i = startTime; i <= endTime; i++) {
                            cutTime += i + ", ";
                        }
                        cutTime = cutTime.substring(0, cutTime.length() - 2);
                        if (isSummer(date)) {
                            event.setTime(cutTime + " (" + arrStartTimeSummer[startTime - 1] + " - " + arrEndTimeSummer[endTime - 1] + ")");
                        } else {
                            event.setTime(cutTime + " (" + arrStartTimeWinter[startTime - 1] + " - " + arrEndTimeWinter[endTime - 1] + ")");
                        }
                    } else {
                        event.setTime(rowDataSplit[0]);
                    }
                    event.setDate(date);
                    subjectName = rowDataSplit[2].substring(0, rowDataSplit[2].indexOf("-"));
                    event.setSubjectName(subjectName);
                    event.setPlace(rowDataSplit[4]);
                    event.setLecturer(rowDataSplit[5]);
                    // để sau này khi custom Adapter biết đâu là lịch học và đâu là ghi chú để hiển thị
                    event.setType("Subject");

                    listEvent.add(event);
                }
            }

            // thêm dữ liệu từ listEvent vào database
            for (Event event : listEvent) {
                event.save();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
            Log.d(TAG, "isSummer: " + month + " " + day);
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
                    public void onDatePickerListener(String strDatePicked) {
                        btnDatePicked.setText(strDatePicked);
                    }
                });
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ghi chú");
        builder.setView(view);
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton("Chấp nhận", new DialogInterface.OnClickListener() {
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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialCalendarView.removeDecorators();
                        showEventDot();

                        addDecoratorToDay();
                    }
                }, 1000);

                calDateSelected = new CalendarDay(materialCalendarView.getSelectedDate().getYear(),
                        materialCalendarView.getSelectedDate().getMonth(),
                        materialCalendarView.getSelectedDate().getDay());
                showEventDetail(convertCalendarDayToString(calDateSelected));
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
        builder.setNegativeButton("Quay lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
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
                onDatePickerListener.onDatePickerListener(strDatePicked);
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

    public void showEventDetail(String strDate) {
        Log.d(TAG, "showEventDetail: " + strDate);
        listEventSelected = getListEventSelected(strDate);
        customAdapterShowEvents = new CustomAdapterShowEvents(MainActivity.this, listEventSelected);
        rcShowEvents.setAdapter(customAdapterShowEvents);
        customAdapterShowEvents.notifyDataSetChanged();
    }

    public void showEventDot() {
        List<Event> listEvent = new Select().from(Event.class).execute();
        List<CalendarDay> listCalendarDay = new ArrayList<>();


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
            countSubject = countDate(calendarDay);
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

    public int countDate(CalendarDay calendarDay) {
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
                .where("type = ?", "Subject")
                .where("date = ?", strDate)
                .execute();
        int count = listEvent.size();

        return count;
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
        int count = listEvent.size();

        return count > 0;
    }
}
