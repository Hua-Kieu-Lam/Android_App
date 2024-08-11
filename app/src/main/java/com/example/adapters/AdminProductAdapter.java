package com.example.adapters;

import android.content.Context;
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

        holder.binding.imvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firebaseId = productArrayList.get(position).getFirebaseId();

                // Kiểm tra nếu firebaseId bị null
                if (firebaseId == null) {
                    Log.e("AdminProductAdapter", "firebaseId is null for product: " + productArrayList.get(position).getProductName());
                    Toast.makeText(context, "Không thể xóa sản phẩm vì ID không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product").child(firebaseId);
                Log.d("AdminProductAdapter", "Deleting product with Firebase ID: " + firebaseId);

                ref.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productArrayList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, productArrayList.size());
                    } else {
                        // Xử lý nếu việc xóa thất bại
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
