package com.example.shoestoreapp.employee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class EmployeeOrderSingleRecyclerAdapter extends RecyclerView.Adapter<EmployeeOrderSingleRecyclerAdapter.ViewHolder>{
    private ArrayList<ItemModel> mOrderedItems;
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @NonNull
    @Override
    public EmployeeOrderSingleRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_order_single_item, parent, false);
        return new ViewHolder(view);
    }

    public EmployeeOrderSingleRecyclerAdapter(Context mContext, ArrayList<ItemModel> mOrderedItems) {
        this.mOrderedItems = mOrderedItems;
        this.mContext = mContext;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull EmployeeOrderSingleRecyclerAdapter.ViewHolder holder, int position) {
        ItemModel currentItem = mOrderedItems.get(position);

        holder.itemModelAndColor.setText(currentItem.getModel() + " " + currentItem.getColor());
        holder.itemSizeAndAmount.setText("Broj: " + getPurchasedAmounts(currentItem));

        StorageReference imageReference = storageRef.child(currentItem.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.itemImage);

    }

    @Override
    public int getItemCount() {
        return mOrderedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView itemModelAndColor, itemSizeAndAmount;
        ImageView itemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemModelAndColor = itemView.findViewById(R.id.employeeOrderItemModelColorTextView);
            itemSizeAndAmount = itemView.findViewById(R.id.employeeOrderItemSizeTextView);
            itemImage = itemView.findViewById(R.id.employeeOrderItemImageView);

        }

    }

    public String getPurchasedAmounts(ItemModel purchasedItem){
        String amountsString = "";
        ArrayList<Integer> amounts = purchasedItem.getAmounts();
        ArrayList<Integer> sizes = purchasedItem.getSizes();
        int i = 0;
        for(Integer amount : amounts){
            if(amount > 0){
                if(!amountsString.isEmpty()){
                    amountsString += "\n \n";
                }
                amountsString +=  sizes.get(i);

            }
            i ++;
        }

        return amountsString;
    }

}
