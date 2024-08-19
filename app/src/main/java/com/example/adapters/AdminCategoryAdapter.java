package com.example.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android_app.CategoryActivity;
import com.example.android_app.CategoryUpdateActivity;
import com.example.android_app.R;
import com.example.android_app.databinding.AdminCategoryItemBinding;
import com.example.android_app.databinding.AdminProductItemBinding;
import com.example.models.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

        holder.binding.imvUpdate.setOnClickListener(new View.OnClickListener() {
            final int position = holder.getAdapterPosition();
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, CategoryUpdateActivity.class);
                intent.putExtra("CATEGORY_ID",categoryArrayList.get(position).getCategoryId());
                context.startActivity(intent);
            }
        });
        holder.binding.imvDelete.setOnClickListener(new View.OnClickListener() {
            int position = holder.getAdapterPosition();

            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.delete_dialog);

                ImageView imvOK = dialog.findViewById(R.id.imvOK);
                ImageView imvCancel = dialog.findViewById(R.id.imvCancel);

                imvOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String categoryId = categoryArrayList.get(position).getCategoryId();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Category").child(categoryId);
                        ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                categoryArrayList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, categoryArrayList.size());
                                Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.dismiss();
                    }
                });
                imvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();
            }
        });
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
