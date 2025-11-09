package com.prm392.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.R;

public class ChangePassActivity extends AppCompatActivity {

    private Button btnSave;
    private EditText etOldPass,etNewPass,etConfirmNewPass;
    private FirebaseAuth auth;
    private TextView tvErr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSave = findViewById(R.id.btnChangePass);
        etOldPass =findViewById(R.id.etOldPassword);
        etNewPass = findViewById(R.id.etNewPassword);
        etConfirmNewPass = findViewById(R.id.etConfirmPassword);
        tvErr = findViewById(R.id.tvErrorPass);
        auth = FirebaseAuth.getInstance();

        // Xử lý khi nhấn nút "Lưu"
        btnSave.setOnClickListener(v -> changePassword());

    }
    private void changePassword() {
        String oldPass = etOldPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmNewPass.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            tvErr.setVisibility(View.VISIBLE);
            tvErr.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            tvErr.setVisibility(View.VISIBLE);
            tvErr.setText("Mật khẩu xác nhận không khớp!");
            return;
        }

        if (newPass.length() < 6 ||
                !newPass.matches(".*[A-Z].*") ||
                !newPass.matches(".*[a-z].*")) {
            tvErr.setVisibility(View.VISIBLE);
            tvErr.setText("Mật khẩu mới phải từ 6 ký tự trở lên!");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            tvErr.setVisibility(View.VISIBLE);
            tvErr.setText("Không xác định được tài khoản!");
            return;
        }

        // Xác thực lại người dùng bằng mật khẩu cũ
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Nếu xác thực thành công → cập nhật mật khẩu mới
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePassActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        tvErr.setVisibility(View.VISIBLE);
                        tvErr.setText("Không thể đổi mật khẩu. Thử lại sau!");
                    }
                });
            } else {
                tvErr.setVisibility(View.VISIBLE);
                tvErr.setText("Mật khẩu cũ không chính xác!");
            }
        });
    }
}