package com.prm392.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(v -> login());

        //tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, DashboardActivity.class)));
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
            // Implement Firebase password reset
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                showErrorDialog("Nhập Email để reset password.");
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Email reset đã gửi!", Toast.LENGTH_SHORT).show();
                        } else {
                            showErrorDialog("Lỗi: " + task.getException().getMessage());
                        }
                    });
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showErrorDialog("Vui lòng nhập đầy đủ Email và Password.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorDialog("Định dạng Email không hợp lệ.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Fetch JWT (ID token)
                        mAuth.getCurrentUser().getIdToken(false)  // false = don't force refresh
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        String jwtToken = tokenTask.getResult().getToken();
                                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                        prefs.edit().putString("auth_token", jwtToken).apply();

                                        //startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Lỗi lấy token: " + tokenTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai thông tin đăng nhập: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
