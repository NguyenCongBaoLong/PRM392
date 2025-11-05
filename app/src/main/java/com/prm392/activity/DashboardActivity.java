package com.prm392.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import com.prm392.CertificateAdapter;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CertificateAdapter adapter;
    private List<Certificate> originalCertificateList;
    private EditText etSearch;
    private Button btnFilter;

    // Interface để xử lý sự kiện click từ Adapter
    public interface OnItemClickListener {
        void onItemClick(Certificate certificate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Ánh xạ View
        recyclerView = findViewById(R.id.recycler_view_certificates);
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);

        // 2. Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Giả lập Gọi API và Gán Data (Sử dụng Mock Data)
        fetchCertificates("USER_ID_X");

        // 4. Xử lý sự kiện Search/Filter
        setupSearchAndFilter();
    }

    private void fetchCertificates(String userID) {
        // --- Giả lập API call (Sử dụng Mock Data) ---
        originalCertificateList = createMockData();

        // Khởi tạo Adapter và thiết lập sự kiện Click
        adapter = new CertificateAdapter(originalCertificateList, new OnItemClickListener() {
            @Override
            public void onItemClick(Certificate certificate) {
                // Xử lý luồng: click vào item → sang CertificateDetailActivity

                Intent intent = new Intent(DashboardActivity.this, CertificateDetailActivity.class);

                // TRUYỀN ĐỐI TƯỢNG CERTIFICATE (Phải implements Serializable)
                intent.putExtra(CertificateDetailActivity.EXTRA_CERTIFICATE, certificate);

                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private List<Certificate> createMockData() {
        // Tạo dữ liệu giả để kiểm tra logic hết hạn
        List<Certificate> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // 1. Chứng chỉ Sắp hết hạn (Còn 30 ngày)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        list.add(new Certificate("Chứng chỉ Sắp Hạn", "Testing Org", "111", new Date(), cal.getTime(), "url", "file", "userX"));

        // 2. Chứng chỉ Đã hết hạn (Hết hạn cách đây 10 ngày)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -10);
        list.add(new Certificate("Chứng chỉ Đã Hạn", "Expired Org", "222", new Date(), cal.getTime(), "url", "file", "userX"));

        // 3. Chứng chỉ Còn lâu mới hết hạn (Còn 300 ngày)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 300);
        list.add(new Certificate("Chứng chỉ Còn Lâu", "Long Term Org", "333", new Date(), cal.getTime(), "url", "file", "userX"));

        // 4. Chứng chỉ Vĩnh viễn (ExpirationDate = null)
        list.add(new Certificate("Chứng chỉ Vĩnh Viễn", "Permanent Org", "444", new Date(), null, "url", "file", "userX"));

        return list;
    }

    private void setupSearchAndFilter() {
        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Mở dialog/menu lọc chứng chỉ...", Toast.LENGTH_SHORT).show();
        });

        // KẾT NỐI SEARCH BAR VỚI ADAPTER
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}