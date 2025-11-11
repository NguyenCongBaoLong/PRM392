package com.prm392.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Firebase & UI Imports
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CertificateDetailActivity extends AppCompatActivity {

    private Certificate currentCertificate;
    private String certificateId; // Biến lưu ID chứng chỉ

    private Button btnBack, btnEdit, btnShare; // Loại bỏ btnSetReminder
    private ChipGroup chipGroupTags;
    private Button btnAddTag;

    private FirebaseFirestore db;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final int EDIT_CERTIFICATE_DETAIL_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Lấy ID chứng chỉ từ Intent
        certificateId = getIntent().getStringExtra("CERTIFICATE_ID");

        // Cấu hình ActionBar (nếu có)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết Chứng chỉ");
        }

        initViews();
        setupClickListeners();

        if (certificateId != null) {
            // Tải dữ liệu từ Firestore
            fetchCertificateDetails(certificateId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID chứng chỉ.", Toast.LENGTH_LONG).show();
            finish();
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

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit_certificate);
        btnShare = findViewById(R.id.btn_share_certificate);
        // Loại bỏ ánh xạ ProgressBar và btnSetReminder

        chipGroupTags = findViewById(R.id.chip_group_tags);
        btnAddTag = findViewById(R.id.btn_add_tag);
    }

    private void setupClickListeners() {
        // Tắt các nút cho đến khi dữ liệu được tải
        enableButtons(false);

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> openEditScreen(currentCertificate));
        btnShare.setOnClickListener(v -> shareCertificate());
        // Loại bỏ sự kiện click cho btnSetReminder
        btnAddTag.setOnClickListener(v -> showAddTagDialog());
    }

    private void enableButtons(boolean enable) {
        btnEdit.setEnabled(enable);
        btnShare.setEnabled(enable);
        // Loại bỏ btnSetReminder.setEnabled(enable);
        btnAddTag.setEnabled(enable);
    }

    /**
     * Tải chi tiết chứng chỉ từ Firestore dựa trên ID.
     */
    private void fetchCertificateDetails(String id) {
        // Loại bỏ progressBar.setVisibility(View.VISIBLE);
        enableButtons(false);

        db.collection("certificates").document(id)
                .get()
                .addOnCompleteListener(task -> {
                    // Loại bỏ progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Chuyển đổi DocumentSnapshot sang đối tượng Certificate
                            currentCertificate = document.toObject(Certificate.class);
                            Objects.requireNonNull(currentCertificate).setId(document.getId());

                            displayDetails(currentCertificate);
                            enableButtons(true);
                        } else {
                            Toast.makeText(this, "Chứng chỉ không tồn tại.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    /**
     * Hiển thị chi tiết chứng chỉ lên giao diện.
     * Sửa: Sử dụng getName() và getOrganization().
     */
    private void displayDetails(Certificate certificate) {
        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvIssuer = findViewById(R.id.tv_detail_issuer);
        TextView tvCredentialId = findViewById(R.id.tv_detail_credential_id);
        TextView tvIssueDate = findViewById(R.id.tv_detail_issue_date);
        TextView tvExpiry = findViewById(R.id.tv_detail_expiry);
        TextView tvFileName = findViewById(R.id.tv_detail_file_name);

        // SỬA LỖI: Dùng getName() và getOrganization()
        tvName.setText(certificate.getCertificateName());
        tvIssuer.setText(certificate.getIssuingOrganization());

        tvCredentialId.setText(certificate.getCredentialId() != null ? certificate.getCredentialId() : "N/A");
        tvFileName.setText(certificate.getFileName() != null ? certificate.getFileName() : "Chưa có file");

        if (certificate.getIssueDate() != null) {
            tvIssueDate.setText(dateFormat.format(certificate.getIssueDate()));
        } else {
            tvIssueDate.setText("N/A");
        }

        if (certificate.getExpirationDate() != null) {
            tvExpiry.setText(dateFormat.format(certificate.getExpirationDate()));
        } else {
            tvExpiry.setText("Vĩnh Viễn");
        }

        displayTags(certificate.getTags());
    }

    // --- CHỨC NĂNG TAGS ---

    private void displayTags(List<String> tags) {
        chipGroupTags.removeAllViews();
        if (tags != null) {
            for (String tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setCloseIconVisible(true);
                chip.setClickable(true);
                chip.setCheckable(false);

                chip.setOnCloseIconClickListener(v -> removeTag(tag));
                chipGroupTags.addView(chip);
            }
        }
    }

    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Thẻ (Tag)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nhập tên thẻ");
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String newTag = input.getText().toString().trim();
            if (!newTag.isEmpty()) {
                addNewTag(newTag);
            } else {
                Toast.makeText(this, "Tên thẻ không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addNewTag(String tag) {
        if (currentCertificate == null) return;

        List<String> tags = currentCertificate.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
            currentCertificate.setTags(tags);
        }

        if (!tags.contains(tag)) {
            tags.add(tag);
            saveCertificateChanges();
            displayTags(tags);
            Toast.makeText(this, "Đã thêm thẻ: " + tag, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Thẻ đã tồn tại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeTag(String tag) {
        if (currentCertificate == null) return;

        List<String> tags = currentCertificate.getTags();
        if (tags != null && tags.remove(tag)) {
            saveCertificateChanges();
            displayTags(tags);
            Toast.makeText(this, "Đã xóa thẻ: " + tag, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lưu thay đổi (Tags) của chứng chỉ lên Firestore.
     */
    private void saveCertificateChanges() {
        if (currentCertificate == null || currentCertificate.getId() == null) return;

        // Loại bỏ progressBar.setVisibility(View.VISIBLE);

        db.collection("certificates").document(currentCertificate.getId())
                .update("tags", currentCertificate.getTags()) // Chỉ cập nhật trường 'tags'
                .addOnSuccessListener(aVoid -> {
                    // Loại bỏ progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // Loại bỏ progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi lưu Tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- CHỨC NĂNG CHỈNH SỬA & CHIA SẺ ---

    private void openEditScreen(Certificate certificate) {
        Intent editIntent = new Intent(this, EditCertificateActivity.class);
        editIntent.putExtra("CERTIFICATE_TO_EDIT", certificate);
        startActivityForResult(editIntent, EDIT_CERTIFICATE_DETAIL_REQUEST_CODE);
    }

    private void shareCertificate() {
        showShareOptionsDialog();
    }

    private void showShareOptionsDialog() {
        String[] shareOptions = {"Chia sẻ Link", "Gửi qua Email", "Chia sẻ dạng Text"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chia sẻ Chứng chỉ");
        builder.setItems(shareOptions, (dialog, which) -> {
            switch (which) {
                case 0:
                    generateShareableLink();
                    break;
                case 1:
                    shareViaEmail();
                    break;
                case 2:
                    shareAsText();
                    break;
            }
        });
        builder.show();
    }

    private void generateShareableLink() {
        if (currentCertificate == null) return;

        String certificateId = currentCertificate.getId() != null ? currentCertificate.getId() : "temp_id";
        String shareUrl = "https://prm392-certificate.com/share/" + certificateId;

        showShareIntent(shareUrl, "Link chia sẻ chứng chỉ");
    }

    private void shareViaEmail() {
        if (currentCertificate == null) return;

        // SỬA LỖI: Dùng getName() và getOrganization()
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Chứng chỉ: " + currentCertificate.getCertificateName());
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Chi tiết chứng chỉ:\n\n" +
                        "Tên: " + currentCertificate.getCertificateName() + "\n" +
                        "Tổ chức cấp: " + currentCertificate.getIssuingOrganization() + "\n" +
                        "Mã chứng nhận: " + (currentCertificate.getCredentialId() != null ? currentCertificate.getCredentialId() : "N/A") + "\n" +
                        "Ngày cấp: " + (currentCertificate.getIssueDate() != null ? dateFormat.format(currentCertificate.getIssueDate()) : "N/A") + "\n" +
                        "Ngày hết hạn: " + (currentCertificate.getExpirationDate() != null ? dateFormat.format(currentCertificate.getExpirationDate()) : "Vĩnh viễn")
        );

        try {
            startActivity(Intent.createChooser(emailIntent, "Gửi email..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareAsText() {
        if (currentCertificate == null) return;

        // SỬA LỖI: Dùng getName() và getOrganization()
        String shareText =
                "Chứng chỉ của tôi:\n\n" +
                        "🔸 Tên: " + currentCertificate.getCertificateName() + "\n" +
                        "🔸 Tổ chức: " + currentCertificate.getIssuingOrganization() + "\n" +
                        "🔸 Mã: " + (currentCertificate.getCredentialId() != null ? currentCertificate.getCredentialId() : "N/A") + "\n" +
                        "🔸 Ngày cấp: " + (currentCertificate.getIssueDate() != null ? dateFormat.format(currentCertificate.getIssueDate()) : "N/A") + "\n" +
                        "🔸 Hết hạn: " + (currentCertificate.getExpirationDate() != null ? dateFormat.format(currentCertificate.getExpirationDate()) : "Vĩnh viễn") + "\n\n" +
                        "--- PRM392 Certificate App ---";

        showShareIntent(shareText, "Chia sẻ chứng chỉ");
    }

    private void showShareIntent(String content, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ chứng chỉ"));
    }

    // --- XỬ LÝ LIFECYCLE VÀ NAVIGATION ---
    // Loại bỏ toàn bộ chức năng Reminder

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CERTIFICATE_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            // Sau khi chỉnh sửa thành công, fetch lại dữ liệu mới nhất từ Firebase
            if (certificateId != null) {
                fetchCertificateDetails(certificateId);
                setResult(RESULT_OK); // Đặt kết quả cho Activity gọi nó
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại dữ liệu khi quay lại màn hình
        if (certificateId != null) {
            fetchCertificateDetails(certificateId);
        }
    }
}