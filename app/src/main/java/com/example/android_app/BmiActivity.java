package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_app.databinding.ActivityBmiBinding;
import com.example.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BmiActivity extends AppCompatActivity {

    ActivityBmiBinding binding;
    boolean isMale;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference productRef, userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBmiBinding.inflate(getLayoutInflater());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        productRef = database.getReference("Product");
        userRef = database.getReference("User");
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addEvents();
    }

    private void addEvents() {
        binding.btnBack.setOnClickListener(view -> finish());

        binding.btnChatAI.setOnClickListener(view -> {
            Intent intent = new Intent(BmiActivity.this, ChatAIActivity.class);
            startActivity(intent);
        });

        binding.rbMale.setOnClickListener(view -> isMale = true);

        binding.rbFemale.setOnClickListener(view -> isMale = false);

        binding.btnBMICalculator.setOnClickListener(view -> calculateBMIAndFetchProducts());

        binding.btnProductData.setOnClickListener(view -> {
            Intent intent = new Intent(BmiActivity.this, ShowMenuProductActivity.class);
            startActivity(intent);
        });
    }

    private void calculateBMIAndFetchProducts() {
        if (binding.edtHeight.getText().toString().isEmpty() || binding.edtWeight.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ chiều cao và cân nặng", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double height = Double.parseDouble(binding.edtHeight.getText().toString());
            double weight = Double.parseDouble(binding.edtWeight.getText().toString());
            int age = Integer.parseInt(binding.edtAge.getText().toString());

            double bmi = weight / (height * height);

            binding.txtBMIResult.setText(String.format("BMI: %.2f", bmi));

            double dailyCalories = calculateCaloricNeeds(bmi, weight, height, age, isMale);

            saveBMIToFirebase(bmi, dailyCalories);

            fetchRecommendedProductsFromFirebase(dailyCalories);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Dữ liệu nhập không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateCaloricNeeds(double bmi, double weight, double height, int age, boolean isMale) {
        double bmr;
        if (isMale) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * height * 100) - (5.677 * age);
        } else {
            bmr = 447.593 + (9.247 * weight) + (3.098 * height * 100) - (4.330 * age);
        }
        return bmr * 1.55;
    }

    private void saveBMIToFirebase(double bmi, double dailyCalories) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userBmiRef = userRef.child(userId).child("BMI");

            Map<String, Object> bmiData = new HashMap<>();
            bmiData.put("bmi", bmi);
            bmiData.put("dailyCalories", dailyCalories);

            userBmiRef.push().setValue(bmiData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(BmiActivity.this, "Lưu BMI thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BmiActivity.this, "Lưu BMI thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchRecommendedProductsFromFirebase(double targetCalories) {
        productRef.orderByChild("productCalo").endAt(targetCalories).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Product> productList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    productList.add(product);
                }
                ArrayList<Product> recommendedProducts = getRecommendedProducts(productList, targetCalories);
                updateMenuInFirebase(recommendedProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BmiActivity.this, "Tải dữ liệu không thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayList<Product> getRecommendedProducts(List<Product> products, double targetCalories) {
        ArrayList<Product> selectedProducts = new ArrayList<>();
        double currentCalories = 0;

        products.sort((p1, p2) -> Double.compare(p2.getProductCalo(), p1.getProductCalo()));

        for (Product product : products) {
            if (currentCalories + product.getProductCalo() <= targetCalories) {
                selectedProducts.add(product);
                currentCalories += product.getProductCalo();
            }

            if (currentCalories >= targetCalories) {
                break;
            }
        }

        return selectedProducts;
    }

    private void updateMenuInFirebase(ArrayList<Product> recommendedProducts) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userMenuRef = userRef.child(userId).child("Menu");

            userMenuRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (Product product : recommendedProducts) {
                        Map<String, Object> menuData = new HashMap<>();
                        menuData.put("productName", product.getProductName());
                        menuData.put("productImg", product.getProductImg());
                        menuData.put("productCalo", product.getProductCalo());
                        menuData.put("productCarb", product.getProductCarb());
                        menuData.put("productFat", product.getProductFat());
                        menuData.put("productProtein", product.getProductProtein());
                        menuData.put("productMoisture", product.getProductMoisture());

                        userMenuRef.push().setValue(menuData);
                    }
                    Toast.makeText(BmiActivity.this, "Cập nhật menu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BmiActivity.this, "Cập nhật menu thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
