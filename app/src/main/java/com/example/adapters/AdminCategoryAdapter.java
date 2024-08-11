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
import com.example.android_app.databinding.AdminCategoryItemBinding;
import com.example.android_app.databinding.AdminProductItemBinding;
import com.example.models.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {
    ArrayList<Category> categoryArrayList;
    Context context;

    public AdminCategoryAdapter(ArrayList<Category> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public AdminCategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        AdminCategoryItemBinding binding = AdminCategoryItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdminCategoryAdapter.CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCategoryAdapter.CategoryViewHolder holder, int position) {
        holder.binding.txtCategoryName.setText(categoryArrayList.get(position).getCategoryName());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop());

        Glide.with(context)
                .load(categoryArrayList.get(position).getCategoryImg())
                .apply(requestOptions)
                .into(holder.binding.imvCategory);

//        holder.binding.imvDelete.setOnClickListener(view -> {
//            int currentPosition = holder.getAdapterPosition();
//            if (currentPosition == RecyclerView.NO_POSITION) {
//                return;
//            }
//
//            String categoryId = categoryArrayList.get(currentPosition).getCategoryId();
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Category").child(categoryId);
//
//            ref.removeValue().addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    categoryArrayList.remove(currentPosition);
//                    notifyItemRemoved(currentPosition);
//                    notifyItemRangeChanged(currentPosition, categoryArrayList.size());
//                    Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        AdminCategoryItemBinding binding;

        public CategoryViewHolder(AdminCategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}