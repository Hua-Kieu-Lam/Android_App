package com.example.android_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapters.AdminCategoryAdapter;
import com.example.adapters.AdminProductAdapter;
import com.example.android_app.databinding.ActivityAdminBinding;
import com.example.android_app.databinding.ActivityCategoryBinding;
import com.example.models.Category;
import com.example.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity {

    ActivityCategoryBinding binding;
    FirebaseDatabase db;
    private AdminCategoryAdapter adminCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseDatabase.getInstance();
        binding= ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addEvents();
    }

    private void addEvents() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData_Category();
    }

    private void loadData_Category() {
        DatabaseReference ref=db.getReference("Category");
        ArrayList<Category> categoryArrayList=new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot i:snapshot.getChildren()){
                        Category category = i.getValue(Category.class);
                        if (category != null) {
                            category.setCategoryId(i.getKey());
                            categoryArrayList.add(category);
                        }
                    }
                    if(!categoryArrayList.isEmpty()){
                        binding.rvCategory.setLayoutManager(new LinearLayoutManager(CategoryActivity.this));
                        adminCategoryAdapter = new AdminCategoryAdapter(categoryArrayList); // Khởi tạo adapter
                        binding.rvCategory.setAdapter(adminCategoryAdapter);
                        adminCategoryAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}