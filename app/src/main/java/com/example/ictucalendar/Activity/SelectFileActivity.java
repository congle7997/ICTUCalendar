package com.example.ictucalendar.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ictucalendar.Adapter.CustomAdapterSelectFiles;
import com.example.ictucalendar.Interface.OnClickedListener;
import com.example.ictucalendar.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectFileActivity extends AppCompatActivity implements OnClickedListener {
    RecyclerView rcSelectFile;
    RelativeLayout rlBackFolder, rlBackMain;

    private final String TAG = SelectFileActivity.class.getSimpleName();
    public static final String PATH = "path";
    private final String root = "/storage/";
    private String path = root;
    private List<File> listFile = new ArrayList<>();
    private CustomAdapterSelectFiles customAdapterSelectFiles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);

        rcSelectFile = findViewById(R.id.rc_select_file);
        rlBackFolder = findViewById(R.id.rl_back_folder);
        rlBackMain = findViewById(R.id.rl_back_main);
        //txtBackFolder = findViewById(R.id.txt_back_folder);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcSelectFile.setLayoutManager(layoutManager);
        
        initView();
    }

    private void initView() {
        // di chuyển đến thư mục gốc chứa emulated(bộ nhớ máy) và sdcard0(thẻ nhớ)
        File file = new File(root);
        // addAll(): add 1 ArrayList vào 1 ArrayList
        // asList(): convert Array to ArrayList
        listFile.addAll(Arrays.asList(file.listFiles()));

        loadRootFolder();

        customAdapterSelectFiles = new CustomAdapterSelectFiles(this, this, listFile);
        rcSelectFile.setAdapter(customAdapterSelectFiles);

        rlBackFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Nếu phần tử cuối là dấu '/' thì cắt đi
                để đoạn code lấy link back về thư mục trước phía dưới
                chỉ cần dùng phương thức substring lấy từ đầu đến phần tử '/' cuối cùng
                VD: /storage/emulated/0/Download/ --> /storage/emulated/0/ */
                if (path.charAt(path.length() - 1) == '/') {
                    path = path.substring(0, path.length() - 1);
                }

                if (path.length() > root.length()) {
                    path = path.substring(0, path.lastIndexOf("/"));
                }

                /* Nếu đường dẫn là /storage/emulated thì sẽ bị crash app nên phải để là /storage/
                   chắc tại /storage/ là gốc
                   nên chỉ có /storage/emulated/0 là bộ nhớ máy và /storage/sdcard0 là thẻ nhớ */
                if (path.equalsIgnoreCase("/storage/emulated")) {
                    path = "/storage/";
                   // rlBackFolder.setVisibility(View.GONE);
                }

                 /* Kiểm tra nếu đường dẫn rỗng hoặc không có '/' ở cuối thì thêm '/' vào cuối
                  để thuật tiện cho việc nối chuỗi khi click vào một thư mục bất kỳ */
                if (path.equalsIgnoreCase("/storage") || path.isEmpty()) {
                    path = "/storage/";

                }

                File file = new File(path);
                listFile.clear();
                listFile.addAll(Arrays.asList(file.listFiles()));

                // Nếu back ra ngoài là đường dẫn gốc thì ta sẽ load thư mục gốc
                if (file.getPath().equalsIgnoreCase("/storage")) {
                    loadRootFolder();
                }

                customAdapterSelectFiles.notifyDataSetChanged();
            }
        });
    }

    private void loadRootFolder() {
        /* Ở trong thư mục gốc /storage (máy thật) có tới 4 thư mục
          1 là của bộ nhớ trong (emulated), 2 là của thẻ nhớ, 3 là enc_emulated và 4 là self
          cái 3 và 4 là thư mục rỗng nên xóa nó đi */
        for (int i = 0; i < listFile.size(); i++) {
            if (listFile.get(i).getName().equalsIgnoreCase("enc_emulated")
                    || listFile.get(i).getName().equalsIgnoreCase("self")) {
                listFile.remove(i);
                i--;
            }
        }

        // Kiểm tra nếu đang ở thư mục root thì ta sửa lại link "emulated" thành "emulated/0"
        if (listFile.size() <= 2) {
            for (int i = 0; i < listFile.size(); i++) {
                if (listFile.get(i).getName().equalsIgnoreCase("emulated")) {
                    File file2 = new File(listFile.get(i).getPath() + "/0");
                    listFile.remove(i);
                    listFile.add(file2);
                }
            }
        }

        // Xóa file không phải là file Excel
        /*for (int i = 0; i < listFile.size(); i++) {
            if (listFile.get(i).isFile() && !listFile.get(i).getPath().endsWith(".xls")) {
                listFile.remove(i);
                i--;
            }
        }*/

        rlBackFolder.setVisibility(View.GONE);
        rlBackMain.setVisibility(View.VISIBLE);
        rlBackMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onFolderClicked(String folderName) {
        // Kiểm tra nếu cuối đường không có dấu '/' thì thêm
        if (path.charAt(path.length() - 1) != '/') {
            path += '/';
        }

        path += folderName + "/";

        rlBackMain.setVisibility(View.GONE);
        rlBackFolder.setVisibility(View.VISIBLE);

        Log.d(TAG, "onFolderClicked: ");
        customAdapterSelectFiles.notifyDataSetChanged();
    }

    @Override
    public void onExcelFileClicked(String pathExcelFile) {
        Intent intent = new Intent();
        intent.putExtra(PATH, pathExcelFile);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
