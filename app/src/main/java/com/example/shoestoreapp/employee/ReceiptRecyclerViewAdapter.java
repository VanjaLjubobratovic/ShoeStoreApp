package com.example.shoestoreapp.employee;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ReceiptRecyclerViewAdapter extends RecyclerView.Adapter<ReceiptRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ReceiptRecycleViewAdapter";
    private Context mContext;
    private ArrayList<ItemModel> items;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public ReceiptRecyclerViewAdapter(Context mContext, ArrayList<ItemModel> items) {
        this.mContext = mContext;
        this.items = items;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receipt_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = items.get(position);
        StorageReference imageReference = storageRef.child(item.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.productImage);

        holder.productModel.setText(item.getModel());
        holder.productColor.setText(item.getColor());
        //holder.productSize.setText();
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
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return items.size();
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
