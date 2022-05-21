package com.example.shoestoreapp.customer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.example.shoestoreapp.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecycleViewAdapter";
    private Context mContext;
    private ArrayList<ItemModel> items;
    private FirebaseStorage storage;
    private StorageReference storageRef;


    public RecyclerViewAdapter(Context mContext, ArrayList<ItemModel> items) {
        this.mContext = mContext;
        this.items = items;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view);
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

        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO item onclick
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        RatingBar productRating;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewItem);
            productName = itemView.findViewById(R.id.textViewItemName);
            productRating = itemView.findViewById(R.id.ratingBar);
            productPrice = itemView.findViewById(R.id.textViewItemPrice);
        }
    }
}
