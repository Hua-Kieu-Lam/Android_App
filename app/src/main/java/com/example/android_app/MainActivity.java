package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase db;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addEvents();
        getUserAccount();
    }

    private void getUserAccount() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            binding.txtAccount.setText(email);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    private void addEvents() {
        binding.btnChatAI.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatAIActivity.class));
        });
        binding.btnBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, BmiActivity.class);
                startActivity(intent);
            }
        });
        binding.btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, ShowMenuProductActivity.class);
                startActivity(intent);
            }
        });
        binding.btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });
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
                        Product product = i.getValue(Product.class);
                        if (product != null) {
                            product.setFirebaseId(snapshot.getKey());
                            productArrayList.add(product);
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