// File: com/example/android_app/ProductAddActivity.java
package com.example.android_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_app.databinding.ActivityProductAddBinding;
import com.example.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductAddActivity extends AppCompatActivity {
    ActivityProductAddBinding binding;
    FirebaseDatabase db;
    DatabaseReference productsRef;
    DatabaseReference categoriesRef; // Thêm tham chiếu đến Category
    FirebaseStorage storage;
    StorageReference storageRef;
    Uri imageUri;
    List<String> categoryList = new ArrayList<>();
    Map<String, String> categoryMap = new HashMap<>(); // Map để lưu categoryName và categoryId
    ArrayAdapter<String> categoryAdapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.imgProduct.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance();
        productsRef = db.getReference("Product");
        categoriesRef = db.getReference("Category"); // Khởi tạo tham chiếu Category
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        binding = ActivityProductAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinnerCategory = binding.spinnerCategory;

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        loadCategories();
        addEvents();
    }

    private void loadCategories() {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                categoryList.clear();  // Clear the list to avoid duplicates
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryId = categorySnapshot.getKey();
                    String categoryName = categorySnapshot.child("categoryName").getValue(String.class);
                    if (categoryName != null && categoryId != null) {
                        categoryList.add(categoryName);
                        categoryMap.put(categoryName, categoryId); // Lưu cặp key-value vào map
                    }
                }
                categoryAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the spinner
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductAddActivity.this, "Lỗi khi lấy dữ liệu category", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEvents() {
        binding.btnChooseImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnAddProduct.setOnClickListener(view -> {
            String productName = binding.edtProductName.getText().toString();
            String productCalo = binding.edtProductCalo.getText().toString();
            String productCarb = binding.edtProductCarb.getText().toString();
            String productFat = binding.edtProductFat.getText().toString();
            String productProtein = binding.edtProductProtein.getText().toString();
            String productMoisture = binding.edtProductMoisture.getText().toString();
            String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();

            if (productName.isEmpty() || productCalo.isEmpty() || productCarb.isEmpty() || productFat.isEmpty() || productProtein.isEmpty() || productMoisture.isEmpty()) {
                Toast.makeText(ProductAddActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double pCalo = Double.parseDouble(productCalo);
                double pCarb = Double.parseDouble(productCarb);
                double pFat = Double.parseDouble(productFat);
                double pProtein = Double.parseDouble(productProtein);
                double pMoisture = Double.parseDouble(productMoisture);
                String categoryId = categoryMap.get(selectedCategory); // Lấy categoryId từ Map

                if (imageUri != null) {
                    String fileName = UUID.randomUUID().toString();
                    StorageReference imageRef = storageRef.child("images/" + fileName);

                    UploadTask uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();

                            String productId = productsRef.push().getKey();
                            Product product = new Product(productName, imageUrl, pCalo, pCarb, pFat, pProtein, pMoisture, categoryId);

                            // Thêm sản phẩm vào Firebase
                            productsRef.child(productId).setValue(product).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProductAddActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                    binding.edtProductName.setText("");
                                    binding.edtProductCalo.setText("");
                                    binding.edtProductCarb.setText("");
                                    binding.edtProductFat.setText("");
                                    binding.edtProductProtein.setText("");
                                    binding.edtProductMoisture.setText("");
                                    binding.imgProduct.setImageResource(R.drawable.upload_file);
                                    imageUri = null;
                                    finish();
                                } else {
                                    Toast.makeText(ProductAddActivity.this, "Lỗi khi thêm sản phẩm", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(ProductAddActivity.this, "Lỗi khi tải hình ảnh lên", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(ProductAddActivity.this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(ProductAddActivity.this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
