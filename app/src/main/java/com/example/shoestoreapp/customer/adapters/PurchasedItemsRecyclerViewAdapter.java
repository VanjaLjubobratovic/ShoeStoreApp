package com.example.shoestoreapp.customer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.R;

import java.util.ArrayList;

public class PurchasedItemsRecyclerViewAdapter extends RecyclerView.Adapter<PurchasedItemsRecyclerViewAdapter.ViewHolder>{

    private ArrayList<ItemModel> mPurchasedItems;
    private Context mContext;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchased_list_item, parent, false);
        return new ViewHolder(view);
    }

    public PurchasedItemsRecyclerViewAdapter(Context mContext, ArrayList<ItemModel> mPurchasedItems) {
        this.mPurchasedItems = mPurchasedItems;
        this.mContext = mContext;
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel currentItem = mPurchasedItems.get(position);

        holder.itemModel.setText(currentItem.getModel());
        holder.itemColor.setText(currentItem.getColor());
        holder.itemSize.setText(getPurchasedSizes(currentItem));
        holder.itemAmount.setText(getPurchasedAmounts(currentItem));
        holder.itemPrice.setText(getPrices(currentItem));
    }

    @Override
    public int getItemCount() {
        return mPurchasedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView itemModel, itemColor, itemSize, itemAmount, itemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemModel = itemView.findViewById(R.id.orderItemModelTextView);
            itemColor = itemView.findViewById(R.id.orderItemColorTextView);
            itemSize = itemView.findViewById(R.id.orderItemSizeTextView);
            itemAmount = itemView.findViewById(R.id.orderItemAmountTextView);
            itemPrice = itemView.findViewById(R.id.orderItemPriceTextView);

        }

    }

    public String getPurchasedSizes(ItemModel purchasedItem){
        String sizesString = "";
        ArrayList<Integer> sizes = purchasedItem.getSizes();
        for(Integer size : sizes){
            if(size > 0){
                sizesString += Integer.toString(size);
                sizesString += "\n \n";
            }
        }

        return sizesString;
    }

    public String getPurchasedAmounts(ItemModel purchasedItem){
        String amountsString = "";
        ArrayList<Integer> amounts = purchasedItem.getAmounts();
        for(Integer amount : amounts){
            if(amount > 0){
                amountsString += Integer.toString(amount);
                amountsString += "\n \n";
            }
        }

        return amountsString;
    }

    public String getPrices(ItemModel purchasedItem){
        String pricesString = "";
        ArrayList<Integer> amounts = purchasedItem.getAmounts();
        for(Integer amount : amounts){
            if(amount > 0){
                pricesString += String.valueOf(amount * purchasedItem.getPrice());
                pricesString += "\n \n";
            }
        }

        return pricesString;
    }


}
