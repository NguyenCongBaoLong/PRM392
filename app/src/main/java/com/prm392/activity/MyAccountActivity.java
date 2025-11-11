package com.prm392.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392.R;

public class MyAccountActivity extends AppCompatActivity {


    private Button homebtn, editprofile,publicshowcase,report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        homebtn = findViewById(R.id.btnHome);
        editprofile = findViewById(R.id.btnUpdateProfile);
        publicshowcase = findViewById(R.id.btnPublicShowcase);
        report = findViewById(R.id.btnReport);
        homebtn.setOnClickListener(view -> {
            Intent intent = new Intent(MyAccountActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        });
        editprofile.setOnClickListener(view -> {
            Intent intent = new Intent(MyAccountActivity.this,ProfileActivity.class);
            startActivity(intent);
            finish();
        });
        publicshowcase.setOnClickListener(view -> {
            Intent intent = new Intent(MyAccountActivity.this,PublicProfileSetupActivity.class);
            startActivity(intent);
            finish();
        });
        report.setOnClickListener(view -> {
            Intent intent = new Intent(MyAccountActivity.this,ReportActivity.class);
            startActivity(intent);
            finish();
        });
    }
}