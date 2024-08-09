package com.example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_app.databinding.CategoryItemBinding;
import com.example.models.Category;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private ArrayList<Category> categoryArrayList;
    private Context context;

    public CategoryAdapter(ArrayList<Category> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        CategoryItemBinding binding=CategoryItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        holder.binding.txtCategoryName.setText(categoryArrayList.get(position).getCategoryName());
        Glide.with(context)
                .load(categoryArrayList.get(position).getCategoryImg())
                .into(holder.binding.imgCategory);
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder{
        CategoryItemBinding binding;
        public CategoryViewHolder(CategoryItemBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
