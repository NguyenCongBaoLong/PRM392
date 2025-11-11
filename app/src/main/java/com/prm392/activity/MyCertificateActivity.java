package com.prm392.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.prm392.adapters.CertificateAdapter;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MyCertificateActivity extends AppCompatActivity {

    private static final int EDIT_CERTIFICATE_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;
    private SearchView searchView;
    private Button btnFilter;
    private Button btnBack;
    private Button btnUploadNew; // thêm nút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_certificate);

        recyclerView = findViewById(R.id.recycler_view_certificates);
        searchView = findViewById(R.id.search_view_certificate);
        btnFilter = findViewById(R.id.btn_filter);
        btnBack = findViewById(R.id.btn_back);
        btnUploadNew = findViewById(R.id.btn_upload_new); // ánh xạ

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        certificateList = new ArrayList<>();
        loadCertificateList();

        certificateAdapter = new CertificateAdapter(certificateList, new CertificateAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Certificate certificate) {
                Intent intent = new Intent(MyCertificateActivity.this, CertificateDetailActivity.class);
                intent.putExtra("SELECTED_CERTIFICATE", certificate);
                startActivityForResult(intent, EDIT_CERTIFICATE_REQUEST_CODE);
            }
        });
        recyclerView.setAdapter(certificateAdapter);

        setupSearchView();

        btnFilter.setOnClickListener(v -> {
            Toast.makeText(MyCertificateActivity.this, "Chức năng Lọc đang được phát triển...", Toast.LENGTH_SHORT).show();
        });
        btnBack.setOnClickListener(v -> finish());

        // Sự kiện chuyển sang Upload
        btnUploadNew.setOnClickListener(v -> {
            Intent intent = new Intent(MyCertificateActivity.this, UploadCertificateActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // Hàm thiết lập logic cho SearchView
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Thực hiện tìm kiếm khi người dùng nhấn Enter
                certificateAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Thực hiện tìm kiếm ngay lập tức khi text thay đổi
                if (certificateAdapter != null) {
                    certificateAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    // Hàm Tải/Làm mới danh sách (Gọi từ API/DB)
    private void loadCertificateList() {
        // *** THAY THẾ BẰNG LOGIC LOAD DATA THỰC TẾ ***
        certificateList.clear();
        certificateList.addAll(createMockData());
        // -----------------------------------------------------

        if(certificateAdapter != null) {
            certificateAdapter.notifyDataSetChanged();
        }
    }

    // Cập nhật danh sách nếu Edit thành công
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CERTIFICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Danh sách đã được cập nhật.", Toast.LENGTH_SHORT).show();
            loadCertificateList(); // Load lại data từ nguồn
        }
    }

    // MOCK DATA (Giữ nguyên)
    private List<Certificate> createMockData() {
        List<Certificate> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Date issueDate = new Date();


        cal.add(Calendar.DAY_OF_YEAR, 10);
        list.add(new Certificate("1", "Chứng chỉ Sắp Hạn", "FPT Software", "CRD123", issueDate, cal.getTime(),
                "url_a", "file_a.pdf", "user_1"));


        cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        list.add(new Certificate("2", "Chứng chỉ Đã Hết", "Alphabet (Google)", "CRD456", issueDate, cal.getTime(),
                "url_b", "file_b.pdf", "user_1"));


        list.add(new Certificate("3", "Vĩnh Viễn Certificate", "Microsoft", "CRD789", issueDate, null,
                "url_c", "file_c.pdf", "user_1"));


        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 100);
        list.add(new Certificate("4", "Chứng chỉ Hợp Lệ Dài Hạn", "Amazon", "CRD100", issueDate, cal.getTime(),
                "url_d", "file_d.pdf", "user_1"));

        return list;
    }
}