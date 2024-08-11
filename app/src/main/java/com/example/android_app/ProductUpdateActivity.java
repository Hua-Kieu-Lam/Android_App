// File: com/example/android_app/ProductUpdateActivity.java
package com.example.android_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.android_app.databinding.ActivityProductUpdateBinding;
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

public class ProductUpdateActivity extends AppCompatActivity {

    ActivityProductUpdateBinding binding;
    private FirebaseDatabase db;
    private DatabaseReference productRef;
    private DatabaseReference categoriesRef;
    FirebaseStorage storage;
    StorageReference storageRef;
    Uri imageUri;
    String productId;
    List<String> categoryList = new ArrayList<>();
    Map<String, String> categoryMap = new HashMap<>();
    ArrayAdapter<String> categoryAdapter;
    String selectedCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        binding = ActivityProductUpdateBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productId = getIntent().getStringExtra("PRODUCT_ID");
        productRef = db.getReference("Product");
        categoriesRef = db.getReference("Category");
        Spinner spinnerCategory = binding.spinnerCategory;

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        loadCategories();
        loadProductData();
        addEvents();
    }

    private void loadCategories() {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryId = categorySnapshot.getKey();
                    String categoryName = categorySnapshot.child("categoryName").getValue(String.class);
                    if (categoryName != null && categoryId != null) {
                        categoryList.add(categoryName);
                        categoryMap.put(categoryName, categoryId);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductUpdateActivity.this, "Lỗi khi lấy dữ liệu category", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductData() {
        productRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    binding.edtProductName.setText(product.getProductName());
                    binding.edtProductCalo.setText(String.valueOf(product.getProductCalo()));
                    binding.edtProductCarb.setText(String.valueOf(product.getProductCarb()));
                    binding.edtProductFat.setText(String.valueOf(product.getProductFat()));
                    binding.edtProductProtein.setText(String.valueOf(product.getProductProtein()));
                    binding.edtProductMoisture.setText(String.valueOf(product.getProductMoisture()));

                    Glide.with(ProductUpdateActivity.this)
                            .load(product.getProductImg())
                            .into(binding.imgProduct);

                    // Chọn category hiện tại
                    if (product.getCategoryId() != null) {
                        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                            if (entry.getValue().equals(product.getCategoryId())) {
                                binding.spinnerCategory.setSelection(categoryList.indexOf(entry.getKey()));
                                break;
                            }
                        }
                    }
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
                    binding.imgProduct.setImageURI(imageUri);
                }
            }
    );

    private void addEvents() {
        binding.btnChooseImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnUpdateProduct.setOnClickListener(view -> {
            String productName = binding.edtProductName.getText().toString();
            String productCalo = binding.edtProductCalo.getText().toString();
            String productCarb = binding.edtProductCarb.getText().toString();
            String productFat = binding.edtProductFat.getText().toString();
            String productProtein = binding.edtProductProtein.getText().toString();
            String productMoisture = binding.edtProductMoisture.getText().toString();
            String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();

            if (productName.isEmpty() || productCalo.isEmpty() || productCarb.isEmpty() || productFat.isEmpty() || productProtein.isEmpty() || productMoisture.isEmpty()) {
                Toast.makeText(ProductUpdateActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double pCalo = Double.parseDouble(productCalo);
                double pCarb = Double.parseDouble(productCarb);
                double pFat = Double.parseDouble(productFat);
                double pProtein = Double.parseDouble(productProtein);
                double pMoisture = Double.parseDouble(productMoisture);
                selectedCategoryId = categoryMap.get(selectedCategory);

                if (imageUri != null) {
                    String fileName = UUID.randomUUID().toString();
                    StorageReference imageRef = storageRef.child("images/" + fileName);

                    UploadTask uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            updateProduct(productName, imageUrl, pCalo, pCarb, pFat, pProtein, pMoisture, selectedCategoryId);
                        });
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(ProductUpdateActivity.this, "Lỗi khi tải hình ảnh lên", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    productRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Product product = snapshot.getValue(Product.class);
                            if (product != null) {
                                String currentImageUrl = product.getProductImg();
                                updateProduct(productName, currentImageUrl, pCalo, pCarb, pFat, pProtein, pMoisture, selectedCategoryId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

            } catch (NumberFormatException e) {
                Toast.makeText(ProductUpdateActivity.this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct(String productName, String imageUrl, double pCalo, double pCarb, double pFat, double pProtein, double pMoisture, String categoryId) {
        Product updatedProduct = new Product(productName, imageUrl, pCalo, pCarb, pFat, pProtein, pMoisture, categoryId);

        productRef.child(productId).setValue(updatedProduct).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProductUpdateActivity.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ProductUpdateActivity.this, "Lỗi khi cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
