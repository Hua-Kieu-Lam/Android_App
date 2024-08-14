package com.example.android_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_app.databinding.ActivityCategoryAddBinding;
import com.example.android_app.databinding.ActivityProductAddBinding;
import com.example.models.Category;
import com.example.models.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class CategoryAddActivity extends AppCompatActivity {

    ActivityCategoryAddBinding binding;
    FirebaseDatabase db;
    DatabaseReference categotyRef;
    FirebaseStorage storage;
    StorageReference storageRef;
    Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.imgCategory.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance();
        categotyRef = db.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addEvents();
    }

    private void addEvents() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.btnChooseImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnAddCategory.setOnClickListener(view -> {
            String categoryName = binding.edtCategoryName.getText().toString();

            if (categoryName.isEmpty()) {
                Toast.makeText(CategoryAddActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }


                if (imageUri != null) {
                    String fileName = UUID.randomUUID().toString();
                    StorageReference imageRef = storageRef.child("images/" + fileName);

                    UploadTask uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();

                            String categoryId = categotyRef.push().getKey();
                            Category category = new Category(imageUrl, categoryName);

                            // Thêm sản phẩm vào Firebase
                            categotyRef.child(categoryId).setValue(category).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CategoryAddActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                                    binding.imgCategory.setImageResource(R.drawable.upload_file);
                                    imageUri = null;
                                    finish();
                                } else {
                                    Toast.makeText(CategoryAddActivity.this, "Lỗi khi thêm", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(CategoryAddActivity.this, "Lỗi khi tải hình ảnh lên", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(CategoryAddActivity.this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
                }
        });
    }
}