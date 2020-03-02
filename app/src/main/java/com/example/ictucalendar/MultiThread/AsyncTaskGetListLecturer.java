package com.example.ictucalendar.MultiThread;

import android.os.AsyncTask;

import com.example.ictucalendar.Interface.ReturnListLecturer;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AsyncTaskGetListLecturer extends AsyncTask<String, Void, List<String>> {

    String TAG = "AsyncTaskGetListLecturer";

    String pathExcelLecturer;
    ReturnListLecturer returnListLecturer;

    public AsyncTaskGetListLecturer(String pathExcelLecturer, ReturnListLecturer returnListLecturer) {
        this.pathExcelLecturer = pathExcelLecturer;
        this.returnListLecturer = returnListLecturer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<String> doInBackground(String... strings) {
        List<String> listLecturerName = new ArrayList<>();

        try {
           // String pathExcelLecturer = strings[0];
            FileInputStream excelFile = new FileInputStream(new File(pathExcelLecturer));
            HSSFWorkbook workbook = new HSSFWorkbook(excelFile);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                HSSFSheet sheet = workbook.getSheetAt(i);
                Iterator<Row> rowIterator = sheet.iterator();

                int rowExcelFile = 1;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if (rowExcelFile == 6) {
                        Iterator<Cell> cellIterator = row.iterator();
                        Cell cell;


                        int colOfRow = 1;
                        while (cellIterator.hasNext()) {
                            cell = cellIterator.next();
                            if (colOfRow == 3) {
                                if (cell.getCellTypeEnum() == CellType.STRING) {
                                    String lecturerName = cell.getStringCellValue();
                                    listLecturerName.add(lecturerName);
                                }
                            }
                            colOfRow++;
                        }
                    }
                    rowExcelFile++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listLecturerName;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(List<String> list) {
        super.onPostExecute(list);

        returnListLecturer.setReturnListLecturer(list);
    }
}
