package com.prm392.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.GetTokenResult;

import com.prm392.R;
import com.prm392.entity.User;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText fullName,email,password,confirmPassword;
    Button btnRegister;
    TextView tvError, tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //
        fullName = findViewById(R.id.etFullName);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        confirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvError = findViewById(R.id.tvError);
        tvLogin = findViewById(R.id.tvLogin);
        //
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateUser();
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void CreateUser(){
        String fullName = this.fullName.getText().toString();
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();
        String confirmPassword = this.confirmPassword.getText().toString();
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email không hợp lệ.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không trùng khớp.");
            return;
        }

        if (password.length() < 6 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*")) {
            showError("Mật khẩu phải có ít nhất 6 ký tự, bao gồm cả chữ hoa và chữ thường.");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {

                            // Lưu thêm thông tin người dùng vào Firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            User user1 = new User(user.getUid(), fullName, email);


                            db.collection("users").document(user.getUid())
                                    .set(user1)
                                    .addOnCompleteListener(setTask -> {

                                        user.getIdToken(true)
                                                .addOnCompleteListener(tokenTask -> {
                                                    if (tokenTask.isSuccessful()) {
                                                        String jwtToken = tokenTask.getResult().getToken();

                                                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                                        prefs.edit().putString("auth_token", jwtToken).apply();

                                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Không lấy được token", Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    });

                        }
                    }
                    else {
                        Toast.makeText(RegisterActivity.this,
                                task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại",
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void showError(String mess) {
        tvError.setText(mess);
        tvError.setVisibility(View.VISIBLE);
    }






}