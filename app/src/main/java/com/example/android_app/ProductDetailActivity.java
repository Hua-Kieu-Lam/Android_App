package com.example.android_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android_app.databinding.ActivityProductDetailBinding;
import com.example.models.Product;
import com.google.firebase.database.FirebaseDatabase;

public class ProductDetailActivity extends AppCompatActivity {

    ActivityProductDetailBinding binding;
    FirebaseDatabase db;
    private Product product;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance();
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
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
        product = (Product) getIntent().getSerializableExtra("object");
        Glide.with(this)
                .load(product.getProductImg())
                .into(binding.imgProduct);
        binding.txtProductName.setText(product.getProductName());
        binding.txtProductCalo.setText(String.valueOf(product.getProductCalo()));
        binding.txtProductCarb.setText(String.valueOf(product.getProductCarb()));
        binding.txtProductProtein.setText(String.valueOf(product.getProductProtein()));
        binding.txtProductFat.setText(String.valueOf(product.getProductFat()));
        binding.txtProductMoisture.setText(String.valueOf(product.getProductMoisture()));
    }
}