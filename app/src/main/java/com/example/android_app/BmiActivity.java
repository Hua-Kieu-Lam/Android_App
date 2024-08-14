package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_app.databinding.ActivityBmiBinding;

public class BmiActivity extends AppCompatActivity {

    ActivityBmiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBmiBinding.inflate(getLayoutInflater());
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
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.btnChatAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BmiActivity.this, ChatAIActivity.class);
                startActivity(intent);
            }
        });
        binding.btnBMICalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateBMI();
            }
        });
    }

    private void calculateBMI() {
        // Validate input
        if (binding.edtHeight.getText().toString().isEmpty() || binding.edtWeight.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ chiều cao và cân nặng", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double height = Double.parseDouble(binding.edtHeight.getText().toString());
            double weight = Double.parseDouble(binding.edtWeight.getText().toString());

            double bmi = weight / (height * height);

            binding.txtBMIResult.setText(String.format("BMI: %.2f", bmi));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Dữ liệu nhập không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
