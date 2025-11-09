package com.prm392.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.prm392.R;
import com.prm392.adapters.CertificateSelectionAdapter;
import com.prm392.model.Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PublicProfileSetupActivity extends AppCompatActivity {

    private RecyclerView rvCertSelection;
    private EditText etCustomSlug;
    private Button btnGeneratePublicLink;
    private CertificateSelectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile_setup);

        rvCertSelection = findViewById(R.id.rv_cert_selection);
        etCustomSlug = findViewById(R.id.et_custom_slug);
        btnGeneratePublicLink = findViewById(R.id.btn_generate_public_link);

        // Giả lập dữ liệu chứng chỉ
        List<Certificate> allCertificates = getMockCertificates();

        adapter = new CertificateSelectionAdapter(allCertificates);
        rvCertSelection.setLayoutManager(new LinearLayoutManager(this));
        rvCertSelection.setAdapter(adapter);

        btnGeneratePublicLink.setOnClickListener(v -> generatePublicLink());
    }

    private void generatePublicLink() {
        List<String> selectedIds = adapter.getSelectedCertificateIds();
        String customSlug = etCustomSlug.getText().toString().trim();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một chứng chỉ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Gửi dữ liệu lên Backend qua API Call (Giả lập)
        Log.d("PublicProfile", "Cert IDs: " + selectedIds);
        Log.d("PublicProfile", "Slug: " + (customSlug.isEmpty() ? "Mặc định" : customSlug));

        // API Call: POST /api/v1/profile/generate
        // Body: { "certificate_ids": ["id1", "id2"], "slug": "my-slug" }

        String profileIdentifier = customSlug.isEmpty() ? "user-" + (System.currentTimeMillis() / 1000) : customSlug;
        String generatedUrl = "https://prm392.com/profile/" + profileIdentifier;

        // 2. Thông báo và cho phép chia sẻ
        Toast.makeText(this, "Hồ sơ đã được tạo thành công!", Toast.LENGTH_LONG).show();
        Log.i("PublicProfile", "Liên kết công khai: " + generatedUrl);

        // *Thực hiện: Mở Share Intent để chia sẻ URL*
        shareLink(generatedUrl);
    }

    private void shareLink(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hồ sơ Chứng chỉ Công khai của tôi");

        String shareBody = "Đây là hồ sơ chứng chỉ công khai của tôi. Bạn có thể xem các bằng cấp của tôi tại đây:\n" + url;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ hồ sơ qua..."));
    }


    private List<Certificate> getMockCertificates() {
        return Arrays.asList(
                new Certificate("cert1", "Android Dev Expert", "Google", "CRID123", new Date(), new Date(System.currentTimeMillis() + 31536000000L), "url", "file1.pdf", "user1"),
                new Certificate("cert2", "Java Spring Professional", "Oracle", "ORC999", new Date(), new Date(System.currentTimeMillis() + 15768000000L), "url", "file2.pdf", "user1"),
                new Certificate("cert3", "AWS Certified Developer", "Amazon", "AWS777", new Date(), new Date(System.currentTimeMillis() + 31536000000L), "url", "file3.pdf", "user1")
        );
    }
}