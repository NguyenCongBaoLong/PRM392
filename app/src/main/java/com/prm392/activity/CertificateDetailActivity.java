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
import android.util.Log;
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
// THÊM IMPORT MODEL TAG MỚI
import com.prm392.model.Tag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CertificateDetailActivity extends AppCompatActivity {

    private static final String TAG = "CertDetailActivity";
    private static final String CERTIFICATES_COLLECTION = "certificates";

    private Certificate currentCertificate;
    private String certificateId;

    private Button btnBack, btnEdit, btnShare, btnDelete, btnArchive;
    private ChipGroup chipGroupTags;
    private Button btnAddTag;

    private FirebaseFirestore db;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final int EDIT_CERTIFICATE_DETAIL_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);

        db = FirebaseFirestore.getInstance();

        certificateId = getIntent().getStringExtra("CERTIFICATE_ID");
        Log.d(TAG, "ID nhận được: " + certificateId);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết Chứng chỉ");
        }

        initViews();
        setupClickListeners();

        if (certificateId != null && !certificateId.isEmpty()) {
            fetchCertificateDetails(certificateId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID chứng chỉ (KEY_MISMATCH).", Toast.LENGTH_LONG).show();
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
        btnDelete = findViewById(R.id.btn_delete);
        btnArchive = findViewById(R.id.btn_archive);

        chipGroupTags = findViewById(R.id.chip_group_tags);
        btnAddTag = findViewById(R.id.btn_add_tag);
    }

    private void setupClickListeners() {
        enableButtons(false);
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> openEditScreen(currentCertificate));
        btnShare.setOnClickListener(v -> shareCertificate());
        btnAddTag.setOnClickListener(v -> showAddTagDialog());
        btnDelete.setOnClickListener(v -> showConfirmDialog("Delete", true));
        btnArchive.setOnClickListener(v -> showConfirmDialog("Archive", false));
    }

    private void enableButtons(boolean enable) {
        btnEdit.setEnabled(enable);
        btnShare.setEnabled(enable);
        btnAddTag.setEnabled(enable);
        btnDelete.setEnabled(enable);
        btnArchive.setEnabled(enable);
    }

    /**
     * Tải chi tiết chứng chỉ từ Firestore dựa trên ID.
     */
    private void fetchCertificateDetails(String id) {
        enableButtons(false);

        db.collection(CERTIFICATES_COLLECTION).document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentCertificate = document.toObject(Certificate.class);
                            Objects.requireNonNull(currentCertificate).setId(document.getId());

                            displayDetails(currentCertificate);
                            enableButtons(true);
                        } else {
                            Toast.makeText(this, "Chứng chỉ không tồn tại.", Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Document not found for ID: " + id);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching certificate", task.getException());
                        finish();
                    }
                });
    }

    /**
     * Hiển thị chi tiết chứng chỉ lên giao diện.
     */
    private void displayDetails(Certificate certificate) {
        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvIssuer = findViewById(R.id.tv_detail_issuer);
        TextView tvCredentialId = findViewById(R.id.tv_detail_credential_id);
        TextView tvIssueDate = findViewById(R.id.tv_detail_issue_date);
        TextView tvExpiry = findViewById(R.id.tv_detail_expiry);
        TextView tvFileName = findViewById(R.id.tv_detail_file_name);

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

        // SỬA: Truyền List<Tag>
        displayTags(certificate.getTags());
    }

    // --- CHỨC NĂNG TAGS (Cập nhật để dùng List<Tag>) ---

    // SỬA: Hàm hiển thị chấp nhận List<Tag>
    private void displayTags(List<Tag> tags) {
        chipGroupTags.removeAllViews();
        if (tags != null) {
            for (Tag tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag.getName()); // Lấy tên từ đối tượng Tag
                chip.setCloseIconVisible(true);
                chip.setClickable(true);
                chip.setCheckable(false);

                chip.setOnCloseIconClickListener(v -> removeTag(tag.getName())); // Truyền tên để xóa
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
            String newTagName = input.getText().toString().trim();
            if (!newTagName.isEmpty()) {
                addNewTag(newTagName);
            } else {
                Toast.makeText(this, "Tên thẻ không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // SỬA: Thêm Tag mới (tạo Tag object)
    private void addNewTag(String tagName) {
        if (currentCertificate == null) return;

        List<Tag> tags = currentCertificate.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
            currentCertificate.setTags(tags);
        }

        Tag newTag = new Tag(tagName);

        // Kiểm tra trùng lặp bằng cách lặp qua tên (hoặc dùng List.contains() nếu Tag.equals() đã được override)
        boolean exists = false;
        for (Tag t : tags) {
            if (t.getName().equalsIgnoreCase(tagName)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            tags.add(newTag);
            saveCertificateChanges();
            displayTags(tags);
            Toast.makeText(this, "Đã thêm thẻ: " + tagName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Thẻ đã tồn tại.", Toast.LENGTH_SHORT).show();
        }
    }

    // SỬA: Xóa Tag (tìm Tag object dựa trên tên)
    private void removeTag(String tagName) {
        if (currentCertificate == null) return;

        List<Tag> tags = currentCertificate.getTags();
        if (tags == null) return;

        boolean removed = false;

        // Tìm và xóa đối tượng Tag dựa trên tên
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).getName().equals(tagName)) {
                tags.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            saveCertificateChanges();
            displayTags(tags);
            Toast.makeText(this, "Đã xóa thẻ: " + tagName, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lưu thay đổi (Tags) của chứng chỉ lên Firestore.
     */
    private void saveCertificateChanges() {
        if (currentCertificate == null || currentCertificate.getId() == null) return;

        db.collection(CERTIFICATES_COLLECTION).document(currentCertificate.getId())
                .update("tags", currentCertificate.getTags()) // Firestore sẽ lưu List<Tag> dưới dạng List of Maps
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu Tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- CHỨC NĂNG CHỈNH SỬA & CHIA SẺ (Giữ nguyên) ---

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

    // --- XỬ LÝ LIFECYCLE VÀ NAVIGATION (Giữ nguyên) ---

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CERTIFICATE_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            if (certificateId != null) {
                fetchCertificateDetails(certificateId);
                setResult(RESULT_OK);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (certificateId != null) {
            fetchCertificateDetails(certificateId);
        }
    }

    private void showConfirmDialog(String action, boolean isDelete) {
        new AlertDialog.Builder(this)
                .setTitle(action + " Certificate")
                .setMessage("Are you sure you want to " + action.toLowerCase() + " this certificate?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (currentCertificate == null || currentCertificate.getId() == null) {
                        Toast.makeText(this, "Lỗi: Không thể thực hiện thao tác do thiếu ID chứng chỉ.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isDelete) {
                        db.collection(CERTIFICATES_COLLECTION).document(currentCertificate.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        db.collection(CERTIFICATES_COLLECTION).document(currentCertificate.getId()).update("isArchived", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Archived successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error archiving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}