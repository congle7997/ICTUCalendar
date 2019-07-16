package com.example.ictucalendar.MultiThread;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.activeandroid.query.Delete;
import com.example.ictucalendar.Activity.MainActivity;
import com.example.ictucalendar.Interface.OnListennerReadExcelStudent;
import com.example.ictucalendar.Object.Event;
import com.example.ictucalendar.Object.Student;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AsyncTaskReadExcelStudent extends AsyncTask<String, Void, Void> {

    String arrStringDay[] = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    int arrIntDay[] = {DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY, DateTimeConstants.WEDNESDAY,
            DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY, DateTimeConstants.SUNDAY};
    String arrStartTimeSummer[] = {"06:30", "07:25", "08:25", "09:25", "10:20", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
    String arrEndTimeSummer[] = {"07:20", "08:15", "09:15", "10:15", "11:10", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};
    String arrStartTimeWinter[] = {"06:45", "07:40", "08:40", "09:40", "10:35", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
    String arrEndTimeWinter[] = {"07:35", "08:30", "09:30", "10:30", "11:25", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};

    OnListennerReadExcelStudent onListennerReadExcelStudent;
    SharedPreferences sharedPreferences;

    public AsyncTaskReadExcelStudent(OnListennerReadExcelStudent onListennerReadExcelStudent, SharedPreferences sharedPreferences) {
        this.onListennerReadExcelStudent = onListennerReadExcelStudent;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... strings) {
        new Delete().from(Event.class).where("type = ?", "Lecturer").execute();
        new Delete().from(Event.class).where("type = ?", "Student").execute();

        Student student = new Student();

        int rowIndex = 0;
        int indexArrDay = 0;
        String strDay = "";

        try {
            String pathExcelFile = strings[0];

            FileInputStream excelFile = new FileInputStream(new File(pathExcelFile));
            HSSFWorkbook workbook = new HSSFWorkbook(excelFile);
            HSSFSheet sheet = workbook.getSheetAt(0);
            // Tạo và gán 1 đối tượng Iterator để lặp các hàng từ đầu tới cuối
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                // Tạo và gán 1 đối tượng Row (1 dòng)
                Row row = rowIterator.next();
                // Lấy dữ liệu bắt đầu từ hàng thứ 7
                rowIndex++;
                //Log.d(TAG, "rowIndex: " + rowIndex);
                if (rowIndex == 4) {
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

                    //SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", rowStudentInfoSplit[1]);
                    editor.putString("unit", rowStudentInfoSplit[5]);
                    editor.putString("student_code", rowStudentInfoSplit[3]);
                    editor.apply();

                    //showInfoProfile();
                }

                if (rowIndex < 7) {
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
                    event.setType("Student");

                    event.save();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        onListennerReadExcelStudent.setOnListennerReadExcelStudent();
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
}
