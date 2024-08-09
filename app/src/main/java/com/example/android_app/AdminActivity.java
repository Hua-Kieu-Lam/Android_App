package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapters.AdminProductAdapter;
import com.example.adapters.ProductAdapter;
import com.example.android_app.databinding.ActivityAdminBinding;
import com.example.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    ActivityAdminBinding binding;
    FirebaseDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseDatabase.getInstance();
        binding=ActivityAdminBinding.inflate(getLayoutInflater());
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
        binding.btnAddProduct.setOnClickListener(view -> {
            startActivity(new Intent(AdminActivity.this, ProductAddActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData_Product();
    }

    private void loadData_Product() {
        DatabaseReference ref=db.getReference("Product");
        ArrayList<Product> productArrayList=new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot i:snapshot.getChildren()){
                        productArrayList.add(i.getValue(Product.class));
                    }
                    if(!productArrayList.isEmpty()){
                        binding.rvProducts.setLayoutManager(new LinearLayoutManager(AdminActivity.this));
                        binding.rvProducts.setAdapter(new AdminProductAdapter(productArrayList));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.mnProduct){
            Intent intent= new Intent(AdminActivity.this, AdminActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}