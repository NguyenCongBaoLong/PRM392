package com.prm392.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// 🔥 Import cho tính năng Tags
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.ArrayList; // Thêm import cho ArrayList
import java.util.Calendar;
import java.util.List; // Thêm import cho List
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CertificateDetailActivity extends AppCompatActivity {

    private Certificate currentCertificate;
    private Button btnBack, btnEdit, btnShare, btnSetReminder, btnDelete, btnArchive;

    //db
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // 🔥 KHAI BÁO CHO TAGS
    private ChipGroup chipGroupTags;
    private Button btnAddTag;

    // Dùng SimpleDateFormat để hiển thị ngày tháng
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Request Code cho màn hình Edit
    private static final int EDIT_CERTIFICATE_DETAIL_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);

        // 1. Lấy dữ liệu Certificate
        currentCertificate = (Certificate) getIntent().getSerializableExtra("SELECTED_CERTIFICATE");

        // 2. Ánh xạ các nút
        initViews();

        if (currentCertificate != null) {
            // Hiển thị chi tiết (đã bao gồm Tags)
            displayDetails(currentCertificate);

            // 3. Xử lý sự kiện click các nút
            setupClickListeners();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit_certificate);
        btnShare = findViewById(R.id.btn_share_certificate);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        btnDelete = findViewById(R.id.btn_delete);
        btnArchive = findViewById(R.id.btn_archive);

        // 🔥 ÁNH XẠ CHO TAGS
        chipGroupTags = findViewById(R.id.chip_group_tags);
        btnAddTag = findViewById(R.id.btn_add_tag);
    }

    private void setupClickListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Edit
        btnEdit.setOnClickListener(v -> openEditScreen(currentCertificate));

        // Nút Share
        btnShare.setOnClickListener(v -> shareCertificate());

        // Nút Set Reminder
        btnSetReminder.setOnClickListener(v -> setExpirationReminder());

        // 🔥 XỬ LÝ CLICK THÊM TAG
        btnAddTag.setOnClickListener(v -> showAddTagDialog());

        // Delete & Archive
        btnDelete.setOnClickListener(v -> showConfirmDialog("Delete", true));
        btnArchive.setOnClickListener(v -> showConfirmDialog("Archive", false));
    }

    // Hàm hiển thị dữ liệu chi tiết lên giao diện
    private void displayDetails(Certificate certificate) {
        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvIssuer = findViewById(R.id.tv_detail_issuer);
        TextView tvCredentialId = findViewById(R.id.tv_detail_credential_id);
        TextView tvIssueDate = findViewById(R.id.tv_detail_issue_date);
        TextView tvExpiry = findViewById(R.id.tv_detail_expiry);
        TextView tvFileName = findViewById(R.id.tv_detail_file_name);

        // Đổ dữ liệu cơ bản
        tvName.setText(certificate.getCertificateName());
        tvIssuer.setText(certificate.getIssuingOrganization());
        tvCredentialId.setText(certificate.getCredentialId() != null ? certificate.getCredentialId() : "N/A");
        tvFileName.setText(certificate.getFileName() != null ? certificate.getFileName() : "Chưa có file");

        // Xử lý ngày cấp
        if (certificate.getIssueDate() != null) {
            tvIssueDate.setText(dateFormat.format(certificate.getIssueDate()));
        } else {
            tvIssueDate.setText("N/A");
        }

        // Xử lý ngày hết hạn
        if (certificate.getExpirationDate() != null) {
            tvExpiry.setText(dateFormat.format(certificate.getExpirationDate()));
        } else {
            tvExpiry.setText("Vĩnh Viễn");
        }

        // 🔥 HIỂN THỊ TAGS
        displayTags(certificate.getTags());
    }

    // 🔥 PHẦN TAGS: Hiển thị các Chip (Tags)
    private void displayTags(List<String> tags) {
        chipGroupTags.removeAllViews(); // Xóa các chip cũ
        if (tags != null) {
            for (String tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setCloseIconVisible(true);
                chip.setClickable(true);
                chip.setCheckable(false);

                // Xử lý sự kiện xóa Chip
                chip.setOnCloseIconClickListener(v -> {
                    removeTag(tag);
                    // TODO: Sau khi removeTag thành công, gọi API để lưu thay đổi
                });
                chipGroupTags.addView(chip);
            }
        }
    }

    // 🔥 PHẦN TAGS: Dialog Thêm Tag mới
    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Thẻ (Tag)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nhập tên thẻ (ví dụ: Kỹ năng mềm)");
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

    // 🔥 PHẦN TAGS: Xử lý thêm Tag vào Model
    private void addNewTag(String tag) {
        if (currentCertificate == null) return;

        List<String> tags = currentCertificate.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
            currentCertificate.setTags(tags);
        }

        if (!tags.contains(tag)) {
            tags.add(tag);
            // TODO: Gọi API để lưu tags mới vào Backend/Database
            // saveCertificateChanges(currentCertificate);

            displayTags(tags); // Cập nhật UI
            Toast.makeText(this, "Đã thêm thẻ: " + tag, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Thẻ đã tồn tại.", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 PHẦN TAGS: Xử lý xóa Tag khỏi Model
    private void removeTag(String tag) {
        if (currentCertificate == null) return;

        List<String> tags = currentCertificate.getTags();
        if (tags != null && tags.remove(tag)) {
            // TODO: Gọi API để lưu thay đổi vào Backend/Database
            // saveCertificateChanges(currentCertificate);

            displayTags(tags); // Cập nhật UI
            Toast.makeText(this, "Đã xóa thẻ: " + tag, Toast.LENGTH_SHORT).show();
        }
    }

    // *** HÀM CHUYỂN SANG MÀN HÌNH CHỈNH SỬA ***
    private void openEditScreen(Certificate certificate) {
        Intent editIntent = new Intent(this, EditCertificateActivity.class);
        editIntent.putExtra("CERTIFICATE_TO_EDIT", certificate);
        startActivityForResult(editIntent, EDIT_CERTIFICATE_DETAIL_REQUEST_CODE);
    }

    // *** CHỨC NĂNG 7: SHARE CERTIFICATE SECURELY (Giữ nguyên) ***
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

    // *** CHỨC NĂNG 8: SET EXPIRATION REMINDERS VỚI WORKMANAGER (Giữ nguyên) ***
    private void setExpirationReminder() {
        if (currentCertificate == null || currentCertificate.getExpirationDate() == null) {
            Toast.makeText(this, "Chứng chỉ này không có ngày hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] reminderOptions = {
                "1 ngày trước",
                "3 ngày trước",
                "1 tuần trước",
                "2 tuần trước",
                "1 tháng trước"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt nhắc nhở hết hạn");
        builder.setItems(reminderOptions, (dialog, which) -> {
            switch (which) {
                case 0:
                    scheduleReminder(1);
                    break;
                case 1:
                    scheduleReminder(3);
                    break;
                case 2:
                    scheduleReminder(7);
                    break;
                case 3:
                    scheduleReminder(14);
                    break;
                case 4:
                    scheduleReminder(30);
                    break;
            }
        });
        builder.show();
    }

    private void scheduleReminder(int daysBefore) {
        if (currentCertificate == null || currentCertificate.getExpirationDate() == null) return;

        try {
            Calendar expirationDate = Calendar.getInstance();
            expirationDate.setTime(currentCertificate.getExpirationDate());

            Calendar reminderDate = (Calendar) expirationDate.clone();
            reminderDate.add(Calendar.DAY_OF_YEAR, -daysBefore);

            long delayInMillis = reminderDate.getTimeInMillis() - System.currentTimeMillis();

            if (delayInMillis > 0) {
                // Tạo data cho worker
                Data inputData = new Data.Builder()
                        .putString("certificate_name", currentCertificate.getCertificateName())
                        .putInt("days_left", daysBefore)
                        .build();

                // Tạo work request
                // LƯU Ý: Phải có class NotificationWorker trong project
                OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .build();

                // Lên lịch reminder
                WorkManager.getInstance(this).enqueue(reminderWork);

                String message = "Đã đặt nhắc nhở " + daysBefore + " ngày trước khi hết hạn";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // Lưu thông tin reminder
                saveReminderToPreferences(reminderWork.getId().toString(), daysBefore);

            } else {
                Toast.makeText(this, "Không thể đặt nhắc nhở cho quá khứ", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi đặt nhắc nhở", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReminderToPreferences(String workId, int daysBefore) {
        // TODO: Logic lưu reminder
    }

    // *** XỬ LÝ BACK & NAVIGATION ***
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CERTIFICATE_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            // Lý tưởng là fetch lại certificate từ DB sau khi Edit
            if (currentCertificate != null) {
                displayDetails(currentCertificate);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lý tưởng là fetch lại certificate từ DB để đảm bảo dữ liệu mới nhất
        if (currentCertificate != null) {
            displayDetails(currentCertificate);
        }
    }

    private void showConfirmDialog(String action, boolean isDelete) {
        new AlertDialog.Builder(this)
                .setTitle(action + " Certificate")
                .setMessage("Are you sure you want to " + action.toLowerCase() + " this certificate?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (isDelete) {
                        db.collection("certifications").document(currentCertificate.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);  // Thêm: Thông báo cho MyCertificateActivity reload
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        db.collection("certifications").document(currentCertificate.getId()).update("isArchived", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Archived successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);  // Thêm: Thông báo cho MyCertificateActivity reload
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error archiving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}