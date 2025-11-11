package com.prm392.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // Thêm import này
import android.view.View; // Thêm import này
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Thêm import này
import android.widget.Toast;
import androidx.annotation.NonNull; // Thêm import này
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.R;
import com.prm392.adapters.CertificateSelectionAdapter;
import com.prm392.model.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PublicProfileSetupActivity extends AppCompatActivity {

    private RecyclerView rvCertSelection;
    private EditText etCustomSlug;
    private Button btnGeneratePublicLink;
    private TextView tvEmptyState; // Khai báo TextView mới
    private CertificateSelectionAdapter adapter;

    // Khai báo Firebase Instances
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile_setup);

        // Kích hoạt nút Back (Up button) trên ActionBar
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("Cấu hình Hồ sơ"); // Đặt tiêu đề cho dễ nhìn

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvCertSelection = findViewById(R.id.rv_cert_selection);
        etCustomSlug = findViewById(R.id.et_custom_slug);
        btnGeneratePublicLink = findViewById(R.id.btn_generate_public_link);
        tvEmptyState = findViewById(R.id.tv_empty_state); // Ánh xạ TextView mới

        // Tải dữ liệu chứng chỉ từ Firestore
        loadCertificatesFromFirestore();

        btnGeneratePublicLink.setOnClickListener(v -> generatePublicLink());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Xử lý sự kiện khi nhấn nút Back trên ActionBar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Quay lại Activity trước đó
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCertificatesFromFirestore() {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập.", Toast.LENGTH_LONG).show();
            // Có thể chuyển hướng về LoginActivity ở đây
             startActivity(new Intent(this, LoginActivity.class));
             finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Truy vấn collection "certificates"
        db.collection("certificates")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Certificate> certList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Chuyển đổi DocumentSnapshot sang đối tượng Certificate
                                Certificate cert = document.toObject(Certificate.class);
                                // Gán ID của document vào đối tượng Certificate nếu cần để dùng trong adapter
                                // cert.setId(document.getId());
                                certList.add(cert);
                            } catch (Exception e) {
                                Log.e("Firestore", "Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                            }
                        }

                        // Kiểm tra danh sách rỗng để hiển thị thông báo
                        if (certList.isEmpty()) {
                            rvCertSelection.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            rvCertSelection.setVisibility(View.VISIBLE);
                            tvEmptyState.setVisibility(View.GONE);

                            // Cập nhật RecyclerView với dữ liệu mới
                            adapter = new CertificateSelectionAdapter(certList);
                            rvCertSelection.setLayoutManager(new LinearLayoutManager(this));
                            rvCertSelection.setAdapter(adapter);
                        }

                    } else {
                        Log.w("Firestore", "Lỗi khi lấy documents: ", task.getException());
                        Toast.makeText(this, "Không thể tải chứng chỉ.", Toast.LENGTH_LONG).show();
                        // Hiển thị thông báo rỗng nếu lỗi
                        rvCertSelection.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Đã xảy ra lỗi khi tải dữ liệu.");
                    }
                });
    }

    private void generatePublicLink() {
        // ... (Giữ nguyên logic tạo link)
        List<String> selectedIds = adapter.getSelectedCertificateIds();
        // ... (phần còn lại của hàm)
        String customSlug = etCustomSlug.getText().toString().trim();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một chứng chỉ.", Toast.LENGTH_SHORT).show();
            return;
        }

        String profileIdentifier = customSlug.isEmpty() ? "user-" + (System.currentTimeMillis() / 1000) : customSlug;
        String generatedUrl = "https://prm392.com/profile/" + profileIdentifier;

        Toast.makeText(this, "Hồ sơ đã được tạo thành công!", Toast.LENGTH_LONG).show();
        Log.i("PublicProfile", "Liên kết công khai: " + generatedUrl);

        shareLink(generatedUrl);
    }

    private void shareLink(String url) {
        // ... (Giữ nguyên logic chia sẻ)
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hồ sơ Chứng chỉ Công khai của tôi");
        String shareBody = "Đây là hồ sơ chứng chỉ công khai của tôi. Bạn có thể xem các bằng cấp của tôi tại đây:\n" + url;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ hồ sơ qua..."));
    }
}