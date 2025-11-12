package com.prm392.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class EditCertificateActivity extends AppCompatActivity {

    private static final String TAG = "EditCertificateActivity";
    private Certificate originalCertificate;
    private EditText etName, etIssuer, etCredentialId, etIssueDate, etExpirationDate;
    private Button btnBack;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_certificate);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ các View
        etName = findViewById(R.id.et_certificate_name_edit);
        etIssuer = findViewById(R.id.et_issuing_organization_edit);
        etCredentialId = findViewById(R.id.et_credential_id_edit);
        etIssueDate = findViewById(R.id.et_issue_date_edit);
        etExpirationDate = findViewById(R.id.et_expiration_date_edit);
        Button btnSave = findViewById(R.id.btn_save_edit);
        btnBack = findViewById(R.id.btn_back); // Ánh xạ nút Quay lại

        // 2. Lấy đối tượng Certificate
        originalCertificate = (Certificate) getIntent().getSerializableExtra("CERTIFICATE_TO_EDIT");

        if (originalCertificate != null) {
            populateData(originalCertificate);

            setupDatePicker(etIssueDate);
            setupDatePicker(etExpirationDate);

            btnSave.setOnClickListener(v -> {
                saveCertificateChanges();
            });

            btnBack.setOnClickListener(v -> {
                finish();
            });
        } else {
            Toast.makeText(this, "Lỗi: Không có dữ liệu để chỉnh sửa.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupDatePicker(final EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // Thiết lập ngày mặc định nếu có sẵn
            try {
                if (!editText.getText().toString().isEmpty()) {
                    Date date = dateFormat.parse(editText.getText().toString());
                    if (date != null) {
                        calendar.setTime(date);
                    }
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
            Log.e(TAG, "Lỗi định dạng ngày: " + e.getMessage());
            Toast.makeText(this, "Lỗi định dạng ngày.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Cập nhật model
        originalCertificate.setCertificateName(newName);
        originalCertificate.setIssuingOrganization(newIssuer);
        originalCertificate.setCredentialId(newCredentialId);
        originalCertificate.setIssueDate(newIssueDate);
        originalCertificate.setExpirationDate(newExpiryDate);

        updateCertificateInDatabase(originalCertificate);

    }

    // HÀM MỚI: Cập nhật dữ liệu lên Firestore
    private void updateCertificateInDatabase(Certificate certificate) {
        String certificateId = certificate.getId();

        if (certificateId == null || certificateId.isEmpty()) {
            Toast.makeText(this, "Không thể cập nhật: ID chứng chỉ không tồn tại.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("certificateName", certificate.getCertificateName());
        updates.put("issuingOrganization", certificate.getIssuingOrganization());
        updates.put("credentialId", certificate.getCredentialId());


        updates.put("issueDate", certificate.getIssueDate());
        updates.put("expirationDate", certificate.getExpirationDate());


        db.collection("certificates")
                .document(certificateId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Chỉnh sửa thành công! ID: " + certificateId, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật chứng chỉ: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}