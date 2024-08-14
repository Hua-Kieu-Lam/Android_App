package com.example.android_app;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapters.ProductAdapter;
import com.example.android_app.databinding.ActivityShowMenuProductBinding;
import com.example.models.Product;

import java.util.ArrayList;

public class ShowMenuProductActivity extends AppCompatActivity {

    ActivityShowMenuProductBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowMenuProductBinding.inflate(getLayoutInflater());
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
        Intent intent = getIntent();
        ArrayList<Product> products = (ArrayList<Product>) intent.getSerializableExtra("recommendedProducts");

        if (products != null) {
            displayRecommendedProducts(products);
        }
    }

    private void displayRecommendedProducts(ArrayList<Product> products) {
        ProductAdapter adapter = new ProductAdapter(products);
        binding.rvMenuProduct.setLayoutManager(new GridLayoutManager(ShowMenuProductActivity.this, 2));
        binding.rvMenuProduct.setAdapter(adapter);
    }
}
