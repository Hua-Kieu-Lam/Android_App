package com.example.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android_app.ProductUpdateActivity;
import com.example.android_app.databinding.AdminProductItemBinding;
import com.example.models.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder > {
    ArrayList<Product> productArrayList;
    Context context;
    public AdminProductAdapter(ArrayList<Product> productArrayList) {
        this.productArrayList = productArrayList;
    }
    @NonNull
    @Override
    public AdminProductAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        AdminProductItemBinding binding=AdminProductItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdminProductAdapter.ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductAdapter.ProductViewHolder holder, int position) {
        holder.binding.txtProductName.setText(productArrayList.get(position).getProductName());
        holder.binding.txtProductCalo.setText(productArrayList.get(position).getProductCalo() + " Calo");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop());

        Glide.with(context).load(productArrayList.get(position).getProductImg()).apply(requestOptions)
                .into(holder.binding.imvProduct);

        holder.binding.imvUpdate.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProductUpdateActivity.class);
            intent.putExtra("PRODUCT_ID", productArrayList.get(position).getFirebaseId());
            context.startActivity(intent);
        });
        holder.binding.imvDelete.setOnClickListener(new View.OnClickListener() {
            int position = holder.getAdapterPosition();
            @Override
            public void onClick(View view) {
                String firebaseId = productArrayList.get(position).getFirebaseId();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product").child(firebaseId);

                ref.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productArrayList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, productArrayList.size());
                        Toast.makeText(context, "Xóa sản phẩm thanh cong", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder{
        AdminProductItemBinding binding;
        public ProductViewHolder(AdminProductItemBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
