package com.prm392.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prm392.CloudinaryManager;
import com.prm392.R;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button btnSave,btnChangePass,btnBack;
    private EditText etFullName;
    private TextView tvErr;

    private Uri selectedImageUri;
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Hiển thị ảnh ngay bằng Picasso
                    Picasso.get().load(uri).into(imageView);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.img_avatar);
        btnSave = findViewById(R.id.btnSave);
        etFullName = findViewById(R.id.etFullName);
        btnChangePass = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btn_back);
        tvErr = findViewById(R.id.tvSaveErr);
        LoadData();

        imageView.setOnClickListener(v->{
            pickImageLauncher.launch("image/*");
        });

        btnSave.setOnClickListener(view -> {
            UpdateProfile();
        });
        btnChangePass.setOnClickListener(view->{
            Intent intent = new Intent(ProfileActivity.this, ChangePassActivity.class);
            startActivity(intent);

        });
        btnBack.setOnClickListener(view->{
            Intent intent = new Intent(ProfileActivity.this, MyAccountActivity.class);
            startActivity(intent);
        });

    }




    private void UpdateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
            startActivity(intent);
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);

        // Nếu người dùng chọn ảnh mới
        if (selectedImageUri != null) {


            new Thread(() -> {
                try {
                    // Chuẩn bị upload
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);

                    Map options = ObjectUtils.asMap(
                            "upload_preset", "prm392",
                            "resource_type", "image"
                    );

                    // Upload lên Cloudinary
                    Cloudinary cloudinary = CloudinaryManager.getInstance();
                    Map uploadResult = cloudinary.uploader().upload(inputStream, options);
                    String imageUrl = uploadResult.get("secure_url").toString();

                    // Lưu link lên Firestore
                    updates.put("avatarUri", imageUrl);

                    runOnUiThread(() -> {
                        userRef.set(updates, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {

                                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {

                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }
        else {
            // Chỉ cập nhật họ tên
            userRef.set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void LoadData(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();



            // 2. Lấy thông tin chi tiết từ Firestore
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String imageUrl = documentSnapshot.getString("avatarUri"); // nếu có ảnh trong Firestore

                    if (fullName != null && !fullName.isEmpty()) {
                        etFullName.setText(fullName);
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(imageView);
                    }
                } else {
                    tvErr.setText("Không tìm thấy thông tin người dùng.");
                }
            }).addOnFailureListener(e -> {
                tvErr.setText("Lỗi khi tải dữ liệu: " + e.getMessage());
            });

        } else {
            tvErr.setText("Bạn chưa đăng nhập!");
        }
    }

}