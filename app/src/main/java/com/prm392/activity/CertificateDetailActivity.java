package com.prm392.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CertificateDetailActivity extends AppCompatActivity {

    private Certificate currentCertificate;
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

        if (currentCertificate != null) {
            // Hiển thị chi tiết
            displayDetails(currentCertificate);

            // 3. Ánh xạ các nút
            Button btnEdit = findViewById(R.id.btn_edit_certificate);
            Button btnShare = findViewById(R.id.btn_share_certificate);
            Button btnSetReminder = findViewById(R.id.btn_set_reminder);

            // 4. Xử lý sự kiện click nút Edit
            btnEdit.setOnClickListener(v -> {
                openEditScreen(currentCertificate);
            });

            // Xử lý sự kiện click nút Share
            btnShare.setOnClickListener(v -> {
                shareCertificate();
            });

            // Xử lý sự kiện click nút Reminder
            btnSetReminder.setOnClickListener(v -> {
                setExpirationReminder();
            });
        }
    }

    // Hàm hiển thị dữ liệu chi tiết lên giao diện
    private void displayDetails(Certificate certificate) {
        // TODO: Cần đảm bảo các ID View này có trong activity_certificate_detail.xml
        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvIssuer = findViewById(R.id.tv_detail_issuer);
        TextView tvCredentialId = findViewById(R.id.tv_detail_credential_id);
        TextView tvIssueDate = findViewById(R.id.tv_detail_issue_date);
        TextView tvExpiry = findViewById(R.id.tv_detail_expiry);
        TextView tvFileName = findViewById(R.id.tv_detail_file_name);

        // Đổ dữ liệu
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
    }

    // *** HÀM CHUYỂN SANG MÀN HÌNH CHỈNH SỬA (Dùng startActivityForResult) ***
    private void openEditScreen(Certificate certificate) {
        Intent editIntent = new Intent(this, EditCertificateActivity.class);
        editIntent.putExtra("CERTIFICATE_TO_EDIT", certificate);

        // QUAN TRỌNG: Dùng startActivityForResult để nhận kết quả từ màn Edit
        startActivityForResult(editIntent, EDIT_CERTIFICATE_DETAIL_REQUEST_CODE);
    }

    // *** CHỨC NĂNG 7: SHARE CERTIFICATE SECURELY ***
    private void shareCertificate() {
        // Hiển thị dialog chọn phương thức share
        showShareOptionsDialog();
    }

    // Hiển thị dialog chọn phương thức chia sẻ
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

    // Tạo và chia sẻ link
    private void generateShareableLink() {
        if (currentCertificate == null) return;

        // Tạo link chia sẻ (có thể kết nối với backend sau)
        String certificateId = currentCertificate.getId() != null ? currentCertificate.getId() : "temp_id";
        String shareUrl = "https://prm392-certificate.com/share/" + certificateId;

        // Hiển thị intent chia sẻ
        showShareIntent(shareUrl, "Link chia sẻ chứng chỉ");
    }

    // Chia sẻ qua Email
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

    // Chia sẻ dạng Text thông thường
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

    // Phương thức hiển thị Share Intent chung
    private void showShareIntent(String content, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ chứng chỉ"));
    }

    // *** CHỨC NĂNG 8: SET EXPIRATION REMINDERS ***
    private void setExpirationReminder() {
        if (currentCertificate == null || currentCertificate.getExpirationDate() == null) {
            Toast.makeText(this, "Chứng chỉ này không có ngày hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] reminderOptions = {
                "1 ngày trước",
                "1 tuần trước",
                "2 tuần trước",
                "1 tháng trước"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt nhắc nhở hết hạn");
        builder.setItems(reminderOptions, (dialog, which) -> {
            switch (which) {
                case 0:
                    scheduleReminder(1, "ngày");
                    break;
                case 1:
                    scheduleReminder(7, "ngày");
                    break;
                case 2:
                    scheduleReminder(14, "ngày");
                    break;
                case 3:
                    scheduleReminder(30, "ngày");
                    break;
            }
        });
        builder.show();
    }

    // Lên lịch nhắc nhở
    private void scheduleReminder(int daysBefore, String unit) {
        // TODO: Triển khai logic lên lịch nhắc nhở thực tế
        // Có thể sử dụng AlarmManager hoặc WorkManager

        String message = "Đã đặt nhắc nhở " + daysBefore + " " + unit + " trước khi hết hạn";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Ghi log hoặc lưu preference
        saveReminderPreference(daysBefore);
    }

    // Lưu cài đặt nhắc nhở (tạm thời)
    private void saveReminderPreference(int daysBefore) {
        // TODO: Lưu vào SharedPreferences hoặc database
        // SharedPreferences prefs = getSharedPreferences("reminder_prefs", MODE_PRIVATE);
        // prefs.edit().putInt("reminder_days_before", daysBefore).apply();
    }

    // Xử lý kết quả trả về từ EditCertificateActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CERTIFICATE_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            // Nếu chỉnh sửa thành công, báo cho màn hình danh sách biết
            setResult(RESULT_OK);

            // Tải lại chi tiết ngay lập tức trên màn hình này
            if (currentCertificate != null) {
                displayDetails(currentCertificate);
            }
        }
    }

    // Dùng onResume để đảm bảo màn hình Detail được cập nhật khi quay lại
    @Override
    protected void onResume() {
        super.onResume();
        if (currentCertificate != null) {
            displayDetails(currentCertificate);
        }
    }
}