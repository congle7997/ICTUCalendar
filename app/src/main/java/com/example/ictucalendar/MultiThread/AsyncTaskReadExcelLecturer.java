package com.example.ictucalendar.MultiThread;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.activeandroid.query.Delete;
import com.example.ictucalendar.Interface.OnListennerReadExcelLecturer;
import com.example.ictucalendar.Object.Event;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;
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

public class AsyncTaskReadExcelLecturer extends AsyncTask<Integer, Void, Void> {

    String arrStartTimeSummer[] = {"06:30", "07:25", "08:25", "09:25", "10:20", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
    String arrEndTimeSummer[] = {"07:20", "08:15", "09:15", "10:15", "11:10", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};
    String arrStartTimeWinter[] = {"06:45", "07:40", "08:40", "09:40", "10:35", "13:00", "13:55", "14:55", "15:55", "16:50", "18:15", "19:10"};
    String arrEndTimeWinter[] = {"07:35", "08:30", "09:30", "10:30", "11:25", "13:50", "14:45", "15:45", "16:45", "17:40", "19:05", "20:00"};

    String pathExcelLecturer;
    OnListennerReadExcelLecturer onListennerReadExcelLecturer;
    SharedPreferences sharedPreferences;

    public AsyncTaskReadExcelLecturer(String pathExcelFile, OnListennerReadExcelLecturer onListennerReadExcelLecturer, SharedPreferences sharedPreferences) {
        this.pathExcelLecturer = pathExcelFile;
        this.onListennerReadExcelLecturer = onListennerReadExcelLecturer;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        new Delete().from(Event.class).where("type = ?", "lecturer").execute();
        new Delete().from(Event.class).where("type = ?", "student").execute();

        int rowIndex = 0;
        int rowWeek = 0;
        List<String> listData = null;

        //SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            int posLectucturer = integers[0];

            FileInputStream excelFile = new FileInputStream(new File(pathExcelLecturer));
            HSSFWorkbook workbook = new HSSFWorkbook(excelFile);
            HSSFSheet sheet = workbook.getSheetAt(posLectucturer);
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

                    // showInfoProfile();
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

                    StringBuilder sb = new StringBuilder();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        if (cell.getCellTypeEnum() == CellType.STRING) {
                            sb.append(cell.getStringCellValue());
                            sb.append("---");
                        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                            sb.append((int) cell.getNumericCellValue());
                            sb.append("---");
                        }
                    }
                    String data = sb.toString();
                    listData.add(data);
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

                    StringBuilder sb = new StringBuilder();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        if (cell.getCellTypeEnum() == CellType.STRING) {
                            sb.append(cell.getStringCellValue());
                            sb.append("---");
                        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                            sb.append((int) cell.getNumericCellValue());
                            sb.append("---");
                        }
                    }
                    String data = sb.toString();
                    listData.add(data);
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
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        onListennerReadExcelLecturer.setOnListennerReadExcelLecturer();
    }

    public void saveData(List<String> listData) {
        try {
            int posStart = listData.get(0).indexOf("(") + 1;
            int posEnd = listData.get(0).indexOf("đến") - 1;
            String strStartDate = listData.get(0).substring(posStart, posEnd);
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy");
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

                    event.setType("lecturer");

                    event.save();

                    //Log.d(TAG, "saveData: " + event.getDate() + " - " + event.getSubjectName());
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

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

}
