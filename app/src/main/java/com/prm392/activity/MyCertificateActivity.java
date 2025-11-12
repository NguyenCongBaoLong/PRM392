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

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.prm392.adapters.CertificateAdapter;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyCertificateActivity extends AppCompatActivity {

    private static final String TAG = "MyCertificateActivity";
    private static final int EDIT_CERTIFICATE_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;
    private SearchView searchView;
    private Button btnFilter;
    private Button btnBack;
    private Button btnUploadNew;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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


        certificateAdapter = new CertificateAdapter(certificateList, certificate -> {
            Intent intent = new Intent(MyCertificateActivity.this, CertificateDetailActivity.class);
            intent.putExtra("SELECTED_CERTIFICATE", certificate);
            startActivityForResult(intent, EDIT_CERTIFICATE_REQUEST_CODE);
        });
        recyclerView.setAdapter(certificateAdapter);

        // 4. Tải dữ liệu lần đầu
        loadCertificateList();

        // 5. Setup các sự kiện
        setupSearchView();

        btnFilter.setOnClickListener(v -> {
            Toast.makeText(MyCertificateActivity.this, "Chức năng Lọc đang được phát triển...", Toast.LENGTH_SHORT).show();
        });
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
                certificateAdapter.getFilter().filter(query);
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

    private void loadCertificateList() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem chứng chỉ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        certificateList.clear();
        db.collection("certificates")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {

                                Certificate certificate = document.toObject(Certificate.class);

                                certificate.setId(document.getId());

                                certificateList.add(certificate);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi chuyển đổi Document sang Certificate: " + e.getMessage());
                            }
                        }

                        if (certificateAdapter != null) {
                            certificateAdapter.notifyDataSetChanged();

                            certificateAdapter.getFilter().filter("");
                        }
                    } else {
                        Log.w(TAG, "Lỗi khi lấy dữ liệu Firestore: ", task.getException());
                        Toast.makeText(MyCertificateActivity.this, "Không thể tải chứng chỉ.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Load lại danh sách nếu có sự thay đổi sau khi chỉnh sửa
        if (requestCode == EDIT_CERTIFICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Danh sách đã được cập nhật.", Toast.LENGTH_SHORT).show();
            loadCertificateList(); // Load lại data từ Firestore
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