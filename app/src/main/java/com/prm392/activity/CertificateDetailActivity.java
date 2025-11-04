package com.prm392.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prm392.R;
import com.prm392.model.Certificate;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CertificateDetailActivity extends AppCompatActivity {

    private TextView tvName, tvIssuer, tvCredentialId, tvExpiryDate;
    private Button btnViewFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Khai báo key Intent
    public static final String EXTRA_CERTIFICATE = "extra_certificate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);

        // Ánh xạ View
        tvName = findViewById(R.id.tv_detail_name);
        tvIssuer = findViewById(R.id.tv_detail_issuer);
        tvCredentialId = findViewById(R.id.tv_detail_id);
        tvExpiryDate = findViewById(R.id.tv_detail_expiry_date);
        btnViewFile = findViewById(R.id.btn_view_file);

        // Lấy dữ liệu từ Intent
        Certificate certificate = (Certificate) getIntent().getSerializableExtra(EXTRA_CERTIFICATE);

        if (certificate != null) {
            displayCertificateDetails(certificate);
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu chứng chỉ!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayCertificateDetails(Certificate cert) {
        tvName.setText(cert.getCertificateName());
        tvIssuer.setText(cert.getIssuingOrganization());
        tvCredentialId.setText(cert.getCredentialId());

        if (cert.getExpirationDate() != null) {
            tvExpiryDate.setText(dateFormat.format(cert.getExpirationDate()));
        } else {
            tvExpiryDate.setText("Vĩnh viễn");
        }

        btnViewFile.setOnClickListener(v -> {
            Toast.makeText(this, "Tải và mở file: " + cert.getFileName(), Toast.LENGTH_LONG).show();
        });
    }
}