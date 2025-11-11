package com.prm392.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.R;
import com.prm392.adapters.CertificateSelectionAdapter;
import com.prm392.model.Certificate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PublicProfileSetupActivity extends AppCompatActivity {

    private RecyclerView rvCertSelection;
    private EditText etCustomSlug;
    private Button btnGeneratePublicLink;
    private Button btnViewPublicLink; // Nút mới để xem link
    private TextView tvEmptyState;
    private CertificateSelectionAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String lastGeneratedUrl = null; // Lưu URL cuối cùng đã tạo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile_setup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cấu hình Hồ sơ Công khai");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadCertificatesFromFirestore();
        setupListeners();
    }

    private void initViews() {
        rvCertSelection = findViewById(R.id.rv_cert_selection);
        etCustomSlug = findViewById(R.id.et_custom_slug);
        btnGeneratePublicLink = findViewById(R.id.btn_generate_public_link);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnViewPublicLink = findViewById(R.id.btn_view_public_link); // Ánh xạ nút mới
        btnViewPublicLink.setVisibility(View.GONE); // Ẩn nút này ban đầu
    }

    private void setupListeners() {
        btnGeneratePublicLink.setOnClickListener(v -> generatePublicLink());

        // Sự kiện click để mở Activity xem link (Deep Link)
        btnViewPublicLink.setOnClickListener(v -> {
            if (lastGeneratedUrl != null) {
                // Khởi tạo Intent với ACTION_VIEW và Uri của link
                Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(lastGeneratedUrl));

                // Thêm package name để đảm bảo nó mở trong chính ứng dụng này
                viewIntent.setPackage(getPackageName());

                // Nếu không tìm thấy component xử lý (có thể do lỗi cấu hình Manifest)
                if (viewIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(viewIntent);
                } else {
                    Toast.makeText(this, "Không thể mở link trong ứng dụng. Kiểm tra Manifest Deep Link.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng tạo liên kết công khai trước.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCertificatesFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("certificates")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Certificate> certList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Certificate cert = document.toObject(Certificate.class);
                                // Rất quan trọng: Gán ID Firestore cho Model để sử dụng khi chọn
                                cert.setId(document.getId());
                                certList.add(cert);
                            } catch (Exception e) {
                                Log.e("Firestore", "Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                            }
                        }

                        if (certList.isEmpty()) {
                            rvCertSelection.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            rvCertSelection.setVisibility(View.VISIBLE);
                            tvEmptyState.setVisibility(View.GONE);

                            adapter = new CertificateSelectionAdapter(certList);
                            rvCertSelection.setLayoutManager(new LinearLayoutManager(this));
                            rvCertSelection.setAdapter(adapter);
                        }
                    } else {
                        Log.w("Firestore", "Lỗi khi lấy documents: ", task.getException());
                        Toast.makeText(this, "Không thể tải chứng chỉ.", Toast.LENGTH_LONG).show();
                        rvCertSelection.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Đã xảy ra lỗi khi tải dữ liệu.");
                    }
                });
    }

    private void generatePublicLink() {
        if (adapter == null) {
            Toast.makeText(this, "Đang tải dữ liệu. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedIds = adapter.getSelectedCertificateIds();
        String customSlug = etCustomSlug.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) return;

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một chứng chỉ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo định danh hồ sơ
        String profileIdentifier = customSlug.isEmpty() ? currentUser.getUid() : customSlug;

        // 2. Chuẩn bị dữ liệu lưu vào Firestore
        Map<String, Object> publicProfileData = new HashMap<>();
        publicProfileData.put("userId", currentUser.getUid());
        publicProfileData.put("certificateIds", selectedIds);
        publicProfileData.put("createdAt", new Date());

        // 3. Lưu vào collection 'public_profiles'
        db.collection("public_profiles").document(profileIdentifier)
                .set(publicProfileData)
                .addOnSuccessListener(aVoid -> {
                    lastGeneratedUrl = "https://prm392.com/profile/" + profileIdentifier;

                    Toast.makeText(this, "Hồ sơ đã được lưu và tạo link thành công!", Toast.LENGTH_LONG).show();
                    Log.i("PublicProfile", "Liên kết công khai: " + lastGeneratedUrl);

                    btnViewPublicLink.setVisibility(View.VISIBLE);
                    shareLink(lastGeneratedUrl);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu cấu hình hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firestore", "Lỗi lưu Public Profile", e);
                });
    }

    private void shareLink(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hồ sơ Chứng chỉ Công khai của tôi");
        String shareBody = "Đây là hồ sơ chứng chỉ công khai của tôi. Bạn có thể xem các bằng cấp của tôi tại đây:\n" + url;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ hồ sơ qua..."));
    }
}