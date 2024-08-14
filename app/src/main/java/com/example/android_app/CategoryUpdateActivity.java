package com.example.android_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.android_app.databinding.ActivityCategoryUpdateBinding;
import com.example.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class CategoryUpdateActivity extends AppCompatActivity {

    ActivityCategoryUpdateBinding binding;
    private DatabaseReference categoryRef;
    FirebaseDatabase db;
    FirebaseStorage storage;
    StorageReference storageRef;
    Uri imageUri;
    String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCategoryUpdateBinding.inflate(getLayoutInflater());
        db=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        storageRef=storage.getReference();

        setContentView(binding.getRoot());
        categoryId=getIntent().getStringExtra("CATEGORY_ID");
        categoryRef=db.getReference("Category");

        loadCategoryData();
        addEvents();
    }

    private void loadCategoryData() {
        categoryRef.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Category category = snapshot.getValue(Category.class);
                if (category != null) {
                    binding.edtCategoryName.setText(category.getCategoryName());

                    Glide.with(CategoryUpdateActivity.this)
                            .load(category.getCategoryImg())
                            .into(binding.imgCategory);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.imgCategory.setImageURI(imageUri);
                }
            }
    );
    private void addEvents() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });
        binding.btnUpdateCategory.setOnClickListener(view -> {
            String categoryName = binding.edtCategoryName.getText().toString();

            if (categoryName.isEmpty()) {
                Toast.makeText(CategoryUpdateActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                if (imageUri != null) {
                    String fileName = UUID.randomUUID().toString();
                    StorageReference imageRef = storageRef.child("images/" + fileName);

                    UploadTask uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            updateCategory(imageUrl,categoryName);
                        });
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(CategoryUpdateActivity.this, "Lỗi khi tải hình ảnh lên", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    categoryRef.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Category category = snapshot.getValue(Category.class);
                            if (category != null) {
                                String currentImageUrl = category.getCategoryImg();
                                updateCategory(currentImageUrl,categoryName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

            } catch (NumberFormatException e) {
                Toast.makeText(CategoryUpdateActivity.this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateCategory(String categoryName, String imageUrl) {
        Category updatedCategory = new Category(categoryName, imageUrl);

        categoryRef.child(categoryId).setValue(updatedCategory).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CategoryUpdateActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CategoryUpdateActivity.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
    }
}