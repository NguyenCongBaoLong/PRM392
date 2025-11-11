package com.prm392.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.prm392.R;
import com.prm392.adapters.CertificateDisplayAdapter; // Cần tạo Adapter mới
import com.prm392.model.Certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PublicProfileViewerActivity extends AppCompatActivity {

    private TextView tvProfileName, tvLoadingStatus;
    private RecyclerView rvCertificates;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile_viewer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xem Hồ sơ Công khai");
        }

        db = FirebaseFirestore.getInstance();
        initViews();
        handleIncomingLink();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        tvProfileName = findViewById(R.id.tv_viewer_profile_name);
        tvLoadingStatus = findViewById(R.id.tv_viewer_loading_status);
        rvCertificates = findViewById(R.id.rv_viewer_certificates);
        progressBar = findViewById(R.id.pb_viewer_loading);

        rvCertificates.setLayoutManager(new LinearLayoutManager(this));
        tvLoadingStatus.setVisibility(View.GONE);
    }

    private void handleIncomingLink() {
        // Kiểm tra xem Activity có được khởi động bằng Deep Link không
        if (getIntent().getAction() == null || !getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            // Nếu không phải Deep Link, thoát hoặc xử lý khác
            Toast.makeText(this, "Truy cập trực tiếp không hợp lệ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Uri data = getIntent().getData();
        if (data != null) {
            // Trích xuất Slug từ đường dẫn (ví dụ: /profile/my-slug -> my-slug)
            String slug = data.getLastPathSegment();

            if (slug != null && !slug.isEmpty()) {
                tvProfileName.setText("Đang tải hồ sơ: " + slug);
                fetchProfileData(slug);
            } else {
                tvProfileName.setText("Hồ sơ không hợp lệ.");
                Toast.makeText(this, "Lỗi: Không tìm thấy định danh hồ sơ.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchProfileData(String slug) {
        progressBar.setVisibility(View.VISIBLE);
        tvLoadingStatus.setVisibility(View.GONE);

        db.collection("public_profiles").document(slug)
                .get()
                .addOnSuccessListener(profileDoc -> {
                    if (profileDoc.exists()) {
                        List<String> certIds = (List<String>) profileDoc.get("certificateIds");

                        if (certIds != null && !certIds.isEmpty()) {
                            fetchCertificatesDetails(certIds);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            tvLoadingStatus.setText("Hồ sơ này chưa có chứng chỉ nào được công khai.");
                            tvLoadingStatus.setVisibility(View.VISIBLE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        tvProfileName.setText("Hồ sơ không tồn tại.");
                        tvLoadingStatus.setText("Vui lòng kiểm tra lại đường dẫn.");
                        tvLoadingStatus.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvProfileName.setText("Lỗi tải hồ sơ.");
                    Toast.makeText(this, "Lỗi tải hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchCertificatesDetails(List<String> certIds) {
        // Firestore chỉ cho phép truy vấn 'in' tối đa 10 phần tử
        if (certIds.size() > 10) {
            Toast.makeText(this, "Chỉ hiển thị 10 chứng chỉ đầu tiên do giới hạn truy vấn.", Toast.LENGTH_LONG).show();
            certIds = certIds.subList(0, 10);
        }

        db.collection("certificates")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), certIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<Certificate> certificates = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Certificate cert = document.toObject(Certificate.class);
                        if (cert != null) {
                            cert.setId(document.getId());
                            certificates.add(cert);
                        }
                    }

                    // Hiển thị danh sách chứng chỉ
                    rvCertificates.setAdapter(new CertificateDisplayAdapter(certificates));
                    if (certificates.isEmpty()) {
                        tvLoadingStatus.setText("Không tìm thấy dữ liệu chi tiết chứng chỉ nào.");
                        tvLoadingStatus.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvLoadingStatus.setText("Lỗi khi tải chi tiết chứng chỉ.");
                    tvLoadingStatus.setVisibility(View.VISIBLE);
                    Log.e("Viewer", "Lỗi tải chi tiết chứng chỉ", e);
                });
    }
}