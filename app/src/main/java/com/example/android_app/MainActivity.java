package com.example.android_app;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapters.AdminProductAdapter;
import com.example.adapters.CategoryAdapter;
import com.example.adapters.ProductAdapter;
import com.example.android_app.databinding.ActivityMainBinding;
import com.example.models.Category;
import com.example.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseDatabase.getInstance();
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadData();
    }

    private void loadData() {
        loadData_Category();
        loadData_Product();
    }

    private void loadData_Category() {
    DatabaseReference ref = db.getReference("Category");
    ArrayList<Category> categoryArrayList = new ArrayList<>();
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                for (DataSnapshot i : snapshot.getChildren()) {
                    Category category = i.getValue(Category.class);
                    if (category != null) {
                        categoryArrayList.add(category);
                    }
                }
                if (!categoryArrayList.isEmpty()) {
                    binding.rvCategory.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvCategory.setAdapter(new CategoryAdapter(categoryArrayList));
                } else {
                    Log.e("MainActivity", "No categories found.");
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("MainActivity", "Error loading data: " + error.getMessage());
        }
    });
}

    private void loadData_Product() {
        DatabaseReference ref=db.getReference("Product");
        ArrayList<Product> productArrayList=new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot i : snapshot.getChildren()) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setFirebaseId(snapshot.getKey()); // Lưu trữ ID Firebase vào product
                            productArrayList.add(product); // Thêm sản phẩm vào danh sách
                        }
                    }
                    if(!productArrayList.isEmpty()){
                        binding.rvProducts.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
                        binding.rvProducts.setAdapter(new ProductAdapter(productArrayList));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}