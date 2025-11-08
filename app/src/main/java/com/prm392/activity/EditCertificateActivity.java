package com.prm392.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditCertificateActivity extends AppCompatActivity {

    private Certificate originalCertificate;
    private EditText etName, etIssuer, etCredentialId, etIssueDate, etExpirationDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_certificate);

        // 1. Ánh xạ các View (TODO: Cần kiểm tra ID trong layout)
        etName = findViewById(R.id.et_certificate_name_edit);
        etIssuer = findViewById(R.id.et_issuing_organization_edit);
        etCredentialId = findViewById(R.id.et_credential_id_edit);
        etIssueDate = findViewById(R.id.et_issue_date_edit);
        etExpirationDate = findViewById(R.id.et_expiration_date_edit);
        Button btnSave = findViewById(R.id.btn_save_edit);

        // 2. Lấy đối tượng Certificate
        originalCertificate = (Certificate) getIntent().getSerializableExtra("CERTIFICATE_TO_EDIT");

        if (originalCertificate != null) {
            populateData(originalCertificate);

            // 3. Setup DatePicker cho các trường ngày tháng
            setupDatePicker(etIssueDate);
            setupDatePicker(etExpirationDate);

            // 4. Xử lý sự kiện nút Lưu (Save)
            btnSave.setOnClickListener(v -> {
                saveCertificateChanges();
            });
        } else {
            Toast.makeText(this, "Lỗi: Không có dữ liệu để chỉnh sửa.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Setup DatePickerDialog cho EditText ngày tháng
    private void setupDatePicker(final EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // Thiết lập ngày mặc định nếu có sẵn
            try {
                if (!editText.getText().toString().isEmpty()) {
                    calendar.setTime(dateFormat.parse(editText.getText().toString()));
                }
            } catch (Exception ignored) { }

            new DatePickerDialog(
                    EditCertificateActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(selectedYear, selectedMonth, selectedDay);
                        editText.setText(dateFormat.format(selectedCal.getTime()));
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Cho phép Long Click để xóa nội dung ngày tháng
        editText.setOnLongClickListener(v -> {
            editText.setText("");
            Toast.makeText(this, "Đã xóa ngày", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void populateData(Certificate certificate) {
        etName.setText(certificate.getCertificateName());
        etIssuer.setText(certificate.getIssuingOrganization());
        etCredentialId.setText(certificate.getCredentialId());

        if (certificate.getIssueDate() != null) {
            etIssueDate.setText(dateFormat.format(certificate.getIssueDate()));
        }

        if (certificate.getExpirationDate() != null) {
            etExpirationDate.setText(dateFormat.format(certificate.getExpirationDate()));
        }
    }

    private void saveCertificateChanges() {
        // 1. Lấy dữ liệu mới
        String newName = etName.getText().toString().trim();
        String newIssuer = etIssuer.getText().toString().trim();
        String newCredentialId = etCredentialId.getText().toString().trim();
        String newIssueDateStr = etIssueDate.getText().toString().trim();
        String newExpiryDateStr = etExpirationDate.getText().toString().trim();

        if (newName.isEmpty() || newIssuer.isEmpty()) {
            Toast.makeText(this, "Tên và Tổ chức cấp là bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date newIssueDate = null;
        Date newExpiryDate = null;

        // 2. Chuyển đổi String sang Date
        try {
            if (!newIssueDateStr.isEmpty()) newIssueDate = dateFormat.parse(newIssueDateStr);
            if (!newExpiryDateStr.isEmpty()) newExpiryDate = dateFormat.parse(newExpiryDateStr);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi định dạng ngày.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Cập nhật model
        originalCertificate.setCertificateName(newName);
        originalCertificate.setIssuingOrganization(newIssuer);
        originalCertificate.setCredentialId(newCredentialId);
        originalCertificate.setIssueDate(newIssueDate);
        originalCertificate.setExpirationDate(newExpiryDate);

        // 4. THỰC HIỆN LƯU VÀO CƠ SỞ DỮ LIỆU/API
        boolean success = updateCertificateInDatabase(originalCertificate);

        if (success) {
            Toast.makeText(this, "Chỉnh sửa thành công! ID: " + originalCertificate.getId(), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Gửi tín hiệu thành công
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu dữ liệu.", Toast.LENGTH_SHORT).show();
        }
    }

    // HÀM MOCK: THAY THẾ BẰNG LOGIC DB/API THỰC TẾ
    private boolean updateCertificateInDatabase(Certificate certificate) {
        // Dựa vào certificate.getId() để cập nhật trong DB
        return true;
    }
}