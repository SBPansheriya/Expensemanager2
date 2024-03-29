package com.kmsoft.expensemanager.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.kmsoft.expensemanager.Activity.Trancation.DetailsTransactionActivity;
import com.kmsoft.expensemanager.Fragment.TransactionFragment;
import com.kmsoft.expensemanager.Model.IncomeAndExpense;
import com.kmsoft.expensemanager.Model.ListDateModel;
import com.kmsoft.expensemanager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShowTransactionAdapter extends RecyclerView.Adapter<ShowTransactionAdapter.ViewHolder> {

    Context context;
    ArrayList<IncomeAndExpense> incomeAndExpenseArrayList;

    public ShowTransactionAdapter(Context context, ArrayList<IncomeAndExpense> incomeAndExpenseArrayList) {
        this.context = context;
        this.incomeAndExpenseArrayList = incomeAndExpenseArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_transaction_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncomeAndExpense incomeAndExpense = incomeAndExpenseArrayList.get(position);
        holder.itemName.setText(incomeAndExpense.getCategoryName());
        holder.itemDescription.setText(incomeAndExpense.getDescription());
        holder.itemDate.setText(incomeAndExpense.getTime());

        if (incomeAndExpense.getCategoryImage() == 0){
            holder.itemImage.setImageResource(R.drawable.i);
        }
        else {
            holder.itemImage.setImageResource(incomeAndExpense.getCategoryImage());
        }

        if (incomeAndExpense.getTag().equals("Income")){
            holder.itemAmount.setText("+" + incomeAndExpense.getAmount());
            holder.itemAmount.setTextColor(context.getResources().getColor(R.color.green));
        } else if (incomeAndExpense.getTag().equals("Expense")) {
            holder.itemAmount.setText("-" + incomeAndExpense.getAmount());
            holder.itemAmount.setTextColor(context.getResources().getColor(R.color.red));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsTransactionActivity.class);
                intent.putExtra("incomeAndExpense",incomeAndExpenseArrayList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incomeAndExpenseArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        RelativeLayout relative;
        TextView itemName,itemDescription,itemAmount,itemDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemDescription = itemView.findViewById(R.id.item_description);
            itemAmount = itemView.findViewById(R.id.item_amount);
            itemDate = itemView.findViewById(R.id.item_date);
            relative = itemView.findViewById(R.id.relative);
        }
    }
}