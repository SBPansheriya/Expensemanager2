package com.kmsoft.expensemanager.Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kmsoft.expensemanager.Activity.FloatingButton.AddCategoryActivity;
import com.kmsoft.expensemanager.Model.Category;
import com.kmsoft.expensemanager.R;

import java.util.ArrayList;

public class AddCategoryAdapter extends RecyclerView.Adapter<AddCategoryAdapter.ViewHolder> {

    AddCategoryActivity addCategoryActivity;
    ArrayList<Category> categoryArrayList;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public AddCategoryAdapter(AddCategoryActivity addCategoryActivity, ArrayList<Category> categoryArrayList) {
        this.addCategoryActivity = addCategoryActivity;
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public AddCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_category_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddCategoryAdapter.ViewHolder holder, int position) {

        holder.categoryName.setText(categoryArrayList.get(position).getCategoryName());

        if (categoryArrayList.get(position).getCategoryImage() == 0) {
            holder.addNewCategoryImage.setImageResource(R.drawable.i);
        } else {
            holder.addNewCategoryImage.setImageResource(categoryArrayList.get(position).getCategoryImage());
        }

        holder.editCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddCategoryActivity) addCategoryActivity).showEditNewCategoryBottomDialog(categoryArrayList.get(position),position);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkbox.performClick();
            }
        });

        holder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = categoryArrayList.get(position).getCategoryName();
                int categoryImage = categoryArrayList.get(position).getCategoryImage();
                ((AddCategoryActivity) addCategoryActivity).getData(categoryName, categoryImage);
            }
        });

//        if (holder.getAdapterPosition() == categoryArrayList.size() - 1) {
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 20, 0, 350);
//            holder.relative.setLayoutParams(layoutParams);
//        }
    }

    public void updateData(ArrayList<Category> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView addNewCategoryImage;
        CheckBox checkbox;
        RelativeLayout relative, editCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.show_category_name);
            relative = itemView.findViewById(R.id.relative);
            checkbox = itemView.findViewById(R.id.checkbox);
            addNewCategoryImage = itemView.findViewById(R.id.add_new_category_image);
            editCategory = itemView.findViewById(R.id.edit_category);
        }
    }
}
