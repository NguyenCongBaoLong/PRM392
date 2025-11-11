package com.prm392.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.R;
import com.prm392.model.Certificate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spinnerFormat;
    private Button btnExport,btnBack;
    private TextView tvStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            Intent intent = new Intent(ReportActivity.this,LoginActivity.class);
            startActivity(intent);
            return;
        }
        spinnerFormat = findViewById(R.id.spinnerFormat);
        btnExport = findViewById(R.id.btnExport);
        btnBack = findViewById(R.id.btn_back);
        tvStatus = findViewById(R.id.tvStatus);
        btnExport.setOnClickListener(v -> {
            String format = spinnerFormat.getSelectedItem().toString();
            List<Certificate> data = createMockData();

            if (format.equalsIgnoreCase("PDF")) {
                exportToPDF(data);
            } else {
                exportToCSV(data);
            }
        });
        btnBack.setOnClickListener(view->{
            Intent intent = new Intent(ReportActivity.this,MyAccountActivity.class);
            startActivity(intent);
        });


    }

    // Tạo dữ liệu giả định
    private List<Certificate> createMockData() {
        List<Certificate> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Date issueDate = new Date();


        cal.add(Calendar.DAY_OF_YEAR, 10);
        list.add(new Certificate("1", "Chứng chỉ Sắp Hạn", "FPT Software", "CRD123", issueDate, cal.getTime(),
                "url_a", "file_a.pdf", "user_1"));


        cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        list.add(new Certificate("2", "Chứng chỉ Đã Hết", "Alphabet (Google)", "CRD456", issueDate, cal.getTime(),
                "url_b", "file_b.pdf", "user_1"));


        list.add(new Certificate("3", "Vĩnh Viễn Certificate", "Microsoft", "CRD789", issueDate, null,
                "url_c", "file_c.pdf", "user_1"));


        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 100);
        list.add(new Certificate("4", "Chứng chỉ Hợp Lệ Dài Hạn", "Amazon", "CRD100", issueDate, cal.getTime(),
                "url_d", "file_d.pdf", "user_1"));

        return list;
    }



    private void exportToPDF(List<Certificate> data) {
        try {
            File file = new File(getExternalFilesDir(null), "certificates_report.pdf");

            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();
            paint.setTextSize(14);
            int pageWidth = 600;
            int pageHeight = 800;

            int y = 60;
            int pageNumber = 1;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            paint.setFakeBoldText(true);
            canvas.drawText("BÁO CÁO CHỨNG CHỈ NGƯỜI DÙNG", 120, y, paint);
            paint.setFakeBoldText(false);
            y += 40;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            for (Certificate cert : data) {
                if (y > 700) { // Nếu gần cuối trang, tạo trang mới
                    pdfDocument.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }

                String content =
                        "Tên: " + cert.getCertificateName() + "\n" +
                                "Tổ chức cấp: " + cert.getIssuingOrganization() + "\n" +
                                "Mã chứng nhận: " + cert.getCredentialId() + "\n" +
                                "Ngày cấp: " + (cert.getIssueDate() != null ? sdf.format(cert.getIssueDate()) : "N/A") + "\n" +
                                "Ngày hết hạn: " + (cert.getExpirationDate() != null ? sdf.format(cert.getExpirationDate()) : "Vĩnh viễn") + "\n" +
                                "File: " + cert.getFileName() + "\n" +
                                "URL: " + cert.getFileUrl();

                for (String line : content.split("\n")) {
                    canvas.drawText(line, 40, y, paint);
                    y += 22;
                }

                y += 18; // khoảng cách giữa các chứng chỉ
            }

            pdfDocument.finishPage(page);

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            tvStatus.setText("Đã tạo file PDF tại:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            tvStatus.setText("Lỗi khi tạo PDF: " + e.getMessage());
        }
    }


    private void exportToCSV(List<Certificate> data) {
        try {
            File file = new File(getExternalFilesDir(null), "certificates_report.csv");
            FileWriter writer = new FileWriter(file);

            // Header
            writer.append("ID,Tên chứng chỉ,Tổ chức cấp,Mã chứng nhận,Ngày cấp,Ngày hết hạn,File,URL\n");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            // Ghi từng hàng
            for (Certificate cert : data) {
                writer.append(cert.getId()).append(",");
                writer.append(cert.getCertificateName()).append(",");
                writer.append(cert.getIssuingOrganization()).append(",");
                writer.append(cert.getCredentialId()).append(",");
                writer.append(cert.getIssueDate() != null ? sdf.format(cert.getIssueDate()) : "N/A").append(",");
                writer.append(cert.getExpirationDate() != null ? sdf.format(cert.getExpirationDate()) : "Vĩnh viễn").append(",");
                writer.append(cert.getFileName()).append(",");
                writer.append(cert.getFileUrl()).append("\n");
            }

            writer.flush();
            writer.close();

            tvStatus.setText(" Đã tạo file CSV tại:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            tvStatus.setText(" Lỗi khi tạo CSV: " + e.getMessage());
        }
    }


}