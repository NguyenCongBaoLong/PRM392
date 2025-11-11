package com.prm392.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UploadCertificateActivity extends AppCompatActivity {

    private EditText etCertificateName, etIssuingOrganization, etCredentialId;
    private Button btnIssueDate, btnExpirationDate, btnUploadFile, btnSaveCertificate;
    private TextView tvIssueDate, tvExpirationDate, tvFileName;
    private ProgressBar progressBar;

    private Calendar issueCalendar, expirationCalendar;
    private Uri selectedFileUri;
    private String selectedFileName;
    private SimpleDateFormat dateFormatter;

    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private static final int FILE_PICKER_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_certificate);

        initViews();
        setupFirebase();
        setupDatePickers();
        setupClickListeners();
    }

    private void initViews() {
        etCertificateName = findViewById(R.id.etCertificateName);
        etIssuingOrganization = findViewById(R.id.etIssuingOrganization);
        etCredentialId = findViewById(R.id.etCredentialId);
        btnIssueDate = findViewById(R.id.btnIssueDate);
        btnExpirationDate = findViewById(R.id.btnExpirationDate);
        btnUploadFile = findViewById(R.id.btnUploadFile);
        btnSaveCertificate = findViewById(R.id.btnSaveCertificate);
        tvIssueDate = findViewById(R.id.tvIssueDate);
        tvExpirationDate = findViewById(R.id.tvExpirationDate);
        tvFileName = findViewById(R.id.tvFileName);
        progressBar = findViewById(R.id.progressBar);

        issueCalendar = Calendar.getInstance();
        expirationCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    private void setupFirebase() {
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();   // Thêm Firestore instance
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener issueDateListener = (view, year, month, dayOfMonth) -> {
            issueCalendar.set(Calendar.YEAR, year);
            issueCalendar.set(Calendar.MONTH, month);
            issueCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            tvIssueDate.setText("Issue Date: " + dateFormatter.format(issueCalendar.getTime()));
        };

        DatePickerDialog.OnDateSetListener expirationDateListener = (view, year, month, dayOfMonth) -> {
            expirationCalendar.set(Calendar.YEAR, year);
            expirationCalendar.set(Calendar.MONTH, month);
            expirationCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            tvExpirationDate.setText("Expiration Date: " + dateFormatter.format(expirationCalendar.getTime()));
        };

        btnIssueDate.setOnClickListener(v -> new DatePickerDialog(
                this, issueDateListener,
                issueCalendar.get(Calendar.YEAR),
                issueCalendar.get(Calendar.MONTH),
                issueCalendar.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnExpirationDate.setOnClickListener(v -> new DatePickerDialog(
                this, expirationDateListener,
                expirationCalendar.get(Calendar.YEAR),
                expirationCalendar.get(Calendar.MONTH),
                expirationCalendar.get(Calendar.DAY_OF_MONTH)
        ).show());
    }

    private void setupClickListeners() {
        btnUploadFile.setOnClickListener(v -> openFilePicker());
        btnSaveCertificate.setOnClickListener(v -> saveCertificate());
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            selectedFileName = getFileName(selectedFileUri);
            tvFileName.setText("Selected: " + selectedFileName);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void saveCertificate() {
        String name = etCertificateName.getText().toString().trim();
        String organization = etIssuingOrganization.getText().toString().trim();
        String credentialId = etCredentialId.getText().toString().trim();

        if (name.isEmpty() || organization.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadToFirebase(name, organization, credentialId);
    }

    private void uploadToFirebase(String name, String organization, String credentialId) {
        showLoading(true);

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            showLoading(false);
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = selectedFileName.substring(selectedFileName.lastIndexOf("."));
        String fileName = "certificates/" + userId + "/" + System.currentTimeMillis() + fileExtension;

        StorageReference fileRef = storageReference.child(fileName);

        fileRef.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                    // Tạo map metadata thay vì object, dễ lưu Firestore
                    Map<String, Object> cert = new HashMap<>();
                    cert.put("name", name);
                    cert.put("organization", organization);
                    cert.put("credentialId", credentialId);
                    cert.put("issueDate", issueCalendar.getTime());
                    cert.put("expirationDate", expirationCalendar.getTime());
                    cert.put("fileUrl", downloadUri.toString());
                    cert.put("fileName", selectedFileName);
                    cert.put("userId", userId);

                    // Lưu lên Firestore
                    firestore.collection("certificates")
                            .add(cert)
                            .addOnSuccessListener(documentReference -> {
                                showLoading(false);
                                Toast.makeText(this, "Certificate uploaded & saved to Firestore!", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Failed to save certificate to Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveCertificate.setEnabled(!loading);
    }
}