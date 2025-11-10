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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spinnerFormat;
    private Button btnExport;
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
        tvStatus = findViewById(R.id.tvStatus);
        btnExport.setOnClickListener(v -> {
            String format = spinnerFormat.getSelectedItem().toString();
            List<Map<String, Object>> fakeData = generateFakeData();

            if (format.equalsIgnoreCase("PDF")) {
                exportToPDF(fakeData);
            } else {
                exportToCSV(fakeData);
            }
        });


    }

    // Tạo dữ liệu giả định
    private List<Map<String, Object>> generateFakeData() {
        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("UserID", "user_" + i);
            row.put("Action", i % 2 == 0 ? "Login" : "View Page");
            row.put("Time", "2025-11-09 14:" + (10 + i));
            list.add(row);
        }

        return list;
    }


    private void exportToCSV(List<Map<String, Object>> data) {
        try {
            File file = new File(getExternalFilesDir(null), "report.csv");
            FileWriter writer = new FileWriter(file);
            writer.append("UserID,Action,Time\n");

            for (Map<String, Object> row : data) {
                writer.append(row.get("UserID") + "," + row.get("Action") + "," + row.get("Time") + "\n");
            }

            writer.flush();
            writer.close();

            tvStatus.setText("Đã tạo file CSV tại:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            tvStatus.setText("Lỗi khi tạo CSV: " + e.getMessage());
        }
    }


    private void exportToPDF(List<Map<String, Object>> data) {
        try {
            File file = new File(getExternalFilesDir(null), "report.pdf");

            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();
            paint.setTextSize(14);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int y = 40;
            canvas.drawText("User Activity Report", 200, y, paint);
            y += 30;

            for (Map<String, Object> row : data) {
                String line = row.get("UserID") + " | " + row.get("Action") + " | " + row.get("Time");
                canvas.drawText(line, 30, y, paint);
                y += 20;
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



}