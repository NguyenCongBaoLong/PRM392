package com.prm392.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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

            // 3. Ánh xạ nút Edit
            Button btnEdit = findViewById(R.id.btn_edit_certificate);

            // 4. Xử lý sự kiện click nút Edit
            btnEdit.setOnClickListener(v -> {
                openEditScreen(currentCertificate);
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