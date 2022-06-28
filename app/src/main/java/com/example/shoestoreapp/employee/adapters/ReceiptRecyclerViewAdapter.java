package com.example.shoestoreapp.employee.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.DataModels.ReceiptModel;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.customer.ShoppingCartActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ReceiptRecyclerViewAdapter extends RecyclerView.Adapter<ReceiptRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ReceiptRecycleViewAdapter";
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ReceiptModel receipt;
    private MaterialButton confirmBtn;

    private TextView totalTextView;
    private ArrayList<ItemModel> itemsToRemove = new ArrayList<>();

    public ReceiptRecyclerViewAdapter(Context mContext, ReceiptModel receipt, TextView totalTextView, MaterialButton confirmBtn, ArrayList<ItemModel> itemsToRemove) {
        this.mContext = mContext;
        this.receipt = receipt;
        this.totalTextView = totalTextView;
        this.confirmBtn = confirmBtn;
        this.itemsToRemove = itemsToRemove;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        checkIfNoData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receipt_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = receipt.getItems().get(position);
        StorageReference imageReference = storageRef.child(item.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.productImage);

        holder.productModel.setText(item.getModel());
        holder.productColor.setText(item.getColor());

        //TODO: fix this hack
        Log.d("ARRAYLIST", item.getAmounts().toString());
        holder.productSize.setText(item.getSizes().get(item.getAmounts().indexOf(1)).toString());
        holder.productPrice.setText((int)item.getPrice() + "kn");

        Log.d(TAG, "Item model: " + item.getModel());

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAt(holder.getLayoutPosition());
            }
        });
    }

    private void removeAt(int position) {
        itemsToRemove.add(receipt.getItems().get(position));
        receipt.removeAt(position);

        //Checking if the remove was called from shoppingCartActivity
        //If so, remove the item from SharedPreferences as well
        if(mContext.getClass().equals(ShoppingCartActivity.class)){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            if(receipt.getItems().size() > 0) {
                Gson receiptGson = new Gson();
                String receiptJson = receiptGson.toJson(receipt);
                editor.putString("ShoppingCartReceipt", receiptJson);
            }
            else{
                editor.remove("ShoppingCartReceipt");
            }
            editor.apply();
        }
        checkIfNoData();

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return receipt.getItems().size();
    }

    public void checkIfNoData() {
        if(getItemCount() == 0) {
            confirmBtn.setEnabled(false);
        } else {
            confirmBtn.setEnabled(true);
        }
        totalTextView.setText("UKUPNO: " + receipt.getTotal() + "kn");
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView productImage;
        TextView productModel;
        TextView productPrice;
        TextView productColor;
        TextView productSize;
        ImageButton removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.receiptProductImage);
            productModel = itemView.findViewById(R.id.receiptItemModel);
            productPrice = itemView.findViewById(R.id.receiptItemPrice);
            productColor = itemView.findViewById(R.id.receiptItemColor);
            productSize = itemView.findViewById(R.id.receiptItemSize);
            removeButton = itemView.findViewById(R.id.receiptRemoveBtn);
        }
    }
}
