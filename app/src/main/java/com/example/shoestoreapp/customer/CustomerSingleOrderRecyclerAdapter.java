package com.example.shoestoreapp.customer;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerSingleOrderRecyclerAdapter extends RecyclerView.Adapter<CustomerSingleOrderRecyclerAdapter.ViewHolder>{
    private ArrayList<ItemModel> mPurchasedItems;
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @NonNull
    @Override
    public CustomerSingleOrderRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_single_order_list_item, parent, false);
        return new ViewHolder(view);
    }

    public CustomerSingleOrderRecyclerAdapter(Context mContext, ArrayList<ItemModel> mPurchasedItems) {
        this.mPurchasedItems = mPurchasedItems;
        this.mContext = mContext;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull CustomerSingleOrderRecyclerAdapter.ViewHolder holder, int position) {
        ItemModel currentItem = mPurchasedItems.get(position);

        holder.itemModelAndColor.setText(currentItem.getModel() + " " + currentItem.getColor());
        holder.itemSizeAndAmount.setText(getPurchasedAmounts(currentItem));
        holder.itemPrice.setText(getPrices(currentItem));

        /*StorageReference imageReference = storageRef.child(currentItem.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.itemImage);*/
    }

    @Override
    public int getItemCount() {
        return mPurchasedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView itemModelAndColor, itemSizeAndAmount, itemPrice;
        ImageView itemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemModelAndColor = itemView.findViewById(R.id.orderedItemModelColorTextView);
            itemSizeAndAmount = itemView.findViewById(R.id.orderedItemSizeAmountTextView);
            itemPrice = itemView.findViewById(R.id.orderedItemPriceTextView);
            itemImage = itemView.findViewById(R.id.orderedItemImageView);

        }

    }

    /*public String getPurchasedSizes(ItemModel purchasedItem){
        String sizesString = "";
        ArrayList<Integer> sizes = purchasedItem.getSizes();
        for(Integer size : sizes){
            if(size > 0){
                sizesString += Integer.toString(size);
                sizesString += "\n \n";
            }
        }

        return sizesString;
    }*/

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
                amountsString +=  sizes.get(i) + " x" + amount;

            }
            i ++;
        }

        return amountsString;
    }

    public String getPrices(ItemModel purchasedItem){
        String pricesString = "";
        DecimalFormat df = new DecimalFormat("0.#");
        ArrayList<Integer> amounts = purchasedItem.getAmounts();
        for(Integer amount : amounts){
            if(amount > 0){
                if(!pricesString.isEmpty()){
                    pricesString += "\n \n";
                }
                pricesString += String.valueOf(df.format(amount * purchasedItem.getPrice()) + " kn");

            }
        }

        return pricesString;
    }
}
