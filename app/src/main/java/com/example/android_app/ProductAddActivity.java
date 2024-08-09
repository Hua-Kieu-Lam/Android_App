//package com.example.android_app;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.example.android_app.databinding.ActivityProductAddBinding;
//import com.example.models.Product;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//
//public class ProductAddActivity extends AppCompatActivity {
//    ActivityProductAddBinding binding;
//    FirebaseDatabase db;
//    DatabaseReference productsRef;
//    DatabaseReference idCounterRef;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        db = FirebaseDatabase.getInstance();
//        productsRef = db.getReference("Product");
//        idCounterRef = db.getReference("productIdCounter");
//        binding = ActivityProductAddBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//        addEvents();
//    }
//
//    private void addEvents() {
//        binding.btnAddProduct.setOnClickListener(view -> {
//            String productName = binding.edtProductName.getText().toString();
//            String productCalo = binding.edtProductCalo.getText().toString();
//            String productCarb = binding.edtProductCarb.getText().toString();
//            String productFat = binding.edtProductFat.getText().toString();
//            String productProtein = binding.edtProductProtein.getText().toString();
//            String productMoisture = binding.edtProductMoisture.getText().toString();
//
//            if (productName.isEmpty() || productCalo.isEmpty() || productCarb.isEmpty() || productFat.isEmpty() || productProtein.isEmpty() || productMoisture.isEmpty()) {
//                Toast.makeText(ProductAddActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            try {
//                double pCalo = Double.parseDouble(productCalo);
//                double pCarb = Double.parseDouble(productCarb);
//                double pFat = Double.parseDouble(productFat);
//                double pProtein = Double.parseDouble(productProtein);
//                double pMoisture = Double.parseDouble(productMoisture);
//
//                // Lấy giá trị ID tối đa hiện tại từ Firebase
//                idCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot snapshot) {
//                        int currentId = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
//                        int newId = currentId + 1;
//
//                        // Tạo đối tượng Product mới với ID tự động tăng
//                        Product product = new Product(newId, productName, "", pCalo, pCarb, pFat, pProtein, pMoisture);
//
//                        // Thêm sản phẩm vào Firebase
//                        productsRef.push().setValue(product).addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                idCounterRef.setValue(newId);
//
//                                Toast.makeText(ProductAddActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
//                                binding.edtProductName.setText("");
//                                binding.edtProductCalo.setText("");
//                                binding.edtProductCarb.setText("");
//                                binding.edtProductFat.setText("");
//                                binding.edtProductProtein.setText("");
//                                binding.edtProductMoisture.setText("");
//                            } else {
//                                Toast.makeText(ProductAddActivity.this, "Lỗi khi thêm sản phẩm", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        Toast.makeText(ProductAddActivity.this, "Lỗi khi lấy ID", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (NumberFormatException e) {
//                Toast.makeText(ProductAddActivity.this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
package com.example.android_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

import java.util.UUID;

public class ProductAddActivity extends AppCompatActivity {
    ActivityProductAddBinding binding;
    FirebaseDatabase db;
    DatabaseReference productsRef;
    DatabaseReference idCounterRef;
    FirebaseStorage storage;
    StorageReference storageRef;

    Uri imageUri;
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
        idCounterRef = db.getReference("productIdCounter");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        binding = ActivityProductAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addEvents();
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

                if (imageUri != null) {
                    String fileName = UUID.randomUUID().toString();
                    StorageReference imageRef = storageRef.child("images/" + fileName);

                    UploadTask uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();

                            // Lấy giá trị ID tối đa hiện tại từ Firebase
                            idCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    int currentId = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                                    int newId = currentId + 1;

                                    // Tạo đối tượng Product mới với ID tự động tăng và URL hình ảnh
                                    Product product = new Product(newId, productName, imageUrl, pCalo, pCarb, pFat, pProtein, pMoisture);

                                    // Thêm sản phẩm vào Firebase
                                    productsRef.push().setValue(product).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Cập nhật giá trị ID tối đa
                                            idCounterRef.setValue(newId);

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
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
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
