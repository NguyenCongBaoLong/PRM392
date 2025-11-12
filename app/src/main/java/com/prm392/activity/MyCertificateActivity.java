package com.prm392.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.app.AlertDialog; // THÊM IMPORT

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // THÊM IMPORT
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.prm392.adapters.CertificateAdapter;
import com.prm392.R;
import com.prm392.model.Certificate;
import com.prm392.model.Tag; // THÊM IMPORT MODEL TAG

import java.util.ArrayList;
import java.util.HashSet; // THÊM IMPORT
import java.util.List;
import java.util.Objects;
import java.util.Set; // THÊM IMPORT

public class MyCertificateActivity extends AppCompatActivity {

    private static final String TAG = "MyCertificateActivity";
    private static final int EDIT_CERTIFICATE_REQUEST_CODE = 1;
    private static final String CERTIFICATES_COLLECTION = "certificates"; // Khai báo hằng số

    private RecyclerView recyclerView;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;
    private SearchView searchView;
    private Button btnFilter;
    private Button btnBack;
    private Button btnUploadNew;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // BIẾN MỚI: Danh sách các Tag duy nhất hiện có trong dữ liệu người dùng
    private Set<String> uniqueTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_certificate);

        // 1. Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Ánh xạ các View
        recyclerView = findViewById(R.id.recycler_view_certificates);
        searchView = findViewById(R.id.search_view_certificate);
        btnFilter = findViewById(R.id.btn_filter);
        btnBack = findViewById(R.id.btn_back);
        btnUploadNew = findViewById(R.id.btn_upload_new);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        certificateList = new ArrayList<>();
        uniqueTags = new HashSet<>(); // Khởi tạo Set Tag

        certificateAdapter = new CertificateAdapter(certificateList, certificate -> {
            Intent intent = new Intent(MyCertificateActivity.this, CertificateDetailActivity.class);
            // SỬA: Đảm bảo truyền đúng ID
            intent.putExtra("CERTIFICATE_ID", certificate.getId());
            startActivityForResult(intent, EDIT_CERTIFICATE_REQUEST_CODE);
        });
        recyclerView.setAdapter(certificateAdapter);

        // 4. Tải dữ liệu lần đầu
        loadCertificateList(null); // Tải toàn bộ ban đầu

        // 5. Setup các sự kiện
        setupSearchView();

        // XỬ LÝ NÚT LỌC
        btnFilter.setOnClickListener(v -> showFilterDialog());

        btnBack.setOnClickListener(v -> finish());
        btnUploadNew.setOnClickListener(v -> {
            Intent intent = new Intent(MyCertificateActivity.this, UploadCertificateActivity.class);
            startActivity(intent);
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (certificateAdapter != null) {
                    certificateAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (certificateAdapter != null) {
                    certificateAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    /**
     * Tải danh sách chứng chỉ từ Firestore, có thể lọc theo Tag.
     * @param selectedTag Tag để lọc (null để tải tất cả).
     */
    private void loadCertificateList(String selectedTag) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem chứng chỉ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        certificateList.clear();
        uniqueTags.clear(); // Xóa tags cũ

        Query query = db.collection(CERTIFICATES_COLLECTION)
                .whereEqualTo("userId", userId);

        // THÊM ĐIỀU KIỆN LỌC THEO TAG
        if (selectedTag != null && !selectedTag.equalsIgnoreCase("Tất cả")) {

        }


        // 2. Truy vấn Firestore
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Certificate certificate = document.toObject(Certificate.class);
                                certificate.setId(document.getId());

                                // Nếu có tag được chọn, lọc thủ công sau khi tải
                                if (selectedTag != null && !selectedTag.equalsIgnoreCase("Tất cả")) {
                                    if (certificate.getTags() != null) {
                                        for(Tag tag : certificate.getTags()) {
                                            if (tag.getName().equalsIgnoreCase(selectedTag)) {
                                                certificateList.add(certificate);
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    // Không lọc (tải tất cả)
                                    certificateList.add(certificate);
                                }

                                // Trích xuất và lưu trữ các tag duy nhất cho dialog lọc
                                extractUniqueTags(certificate);

                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi chuyển đổi Document sang Certificate: " + e.getMessage());
                            }
                        }

                        if (certificateAdapter != null) {
                            certificateAdapter.notifyDataSetChanged();
                            certificateAdapter.getFilter().filter(searchView.getQuery().toString());
                        }
                    } else {
                        Log.w(TAG, "Lỗi khi lấy dữ liệu Firestore: ", task.getException());
                        Toast.makeText(MyCertificateActivity.this, "Không thể tải chứng chỉ.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Trích xuất các tag duy nhất từ chứng chỉ và thêm vào uniqueTags Set.
     */
    private void extractUniqueTags(Certificate certificate) {
        if (certificate.getTags() != null) {
            for (Tag tag : certificate.getTags()) {
                uniqueTags.add(tag.getName());
            }
        }
    }

    /**
     * Hiển thị Dialog cho phép người dùng chọn Tag để lọc.
     */
    private void showFilterDialog() {
        if (uniqueTags.isEmpty()) {
            Toast.makeText(this, "Không có Tag nào để lọc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển Set sang Array để hiển thị trong Dialog
        List<String> tagList = new ArrayList<>(uniqueTags);
        // Thêm tùy chọn "Tất cả" lên đầu
        tagList.add(0, "Tất cả");

        final String[] tagsArray = tagList.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lọc theo Tag");
        builder.setItems(tagsArray, (dialog, which) -> {
            String selectedTag = tagsArray[which];
            // Tải lại danh sách với Tag đã chọn
            loadCertificateList(selectedTag);

            // Cập nhật nút lọc để hiển thị tag đã chọn
            if (!selectedTag.equalsIgnoreCase("Tất cả")) {
                btnFilter.setText("Lọc: " + selectedTag);
            } else {
                btnFilter.setText("Lọc");
            }

            // Xóa nội dung tìm kiếm
            searchView.setQuery("", false);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CERTIFICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Danh sách đã được cập nhật.", Toast.LENGTH_SHORT).show();
            // Đặt lại lọc về null để tải lại toàn bộ danh sách mới
            loadCertificateList(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}