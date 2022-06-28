package com.example.shoestoreapp.customer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecycleViewAdapter";
    private Context mContext;
    private ArrayList<ItemModel> items;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private OnItemListener mOnItemListener;

    public RecyclerViewAdapter(Context mContext, ArrayList<ItemModel> items, OnItemListener onItemListener) {
        this.mContext = mContext;
        this.items = items;
        this.mOnItemListener = onItemListener;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        ItemModel item = items.get(position);
        StorageReference imageReference = storageRef.child(item.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.productImage);

        //TODO:Change this naming to something better
        holder.productName.setText(item.toString());
        holder.productRating.setRating((float)item.getRating());
        holder.productPrice.setText((int)item.getPrice() + " kn");

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        RatingBar productRating;
        OnItemListener onItemListener;

        public ViewHolder(View itemView, OnItemListener onItemListener) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewItem);
            productName = itemView.findViewById(R.id.textViewItemName);
            productRating = itemView.findViewById(R.id.ratingBar);
            productPrice = itemView.findViewById(R.id.textViewItemPrice);

            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String viewFullName = view.getParent().toString();
            String name = viewFullName.substring(viewFullName.lastIndexOf("/") + 1);
            name = name.substring(0, name.length()-1);
            onItemListener.onItemClick(getBindingAdapterPosition(), name);
        }
    }

    public interface OnItemListener{
        void onItemClick(int position, String id);
    }
}
