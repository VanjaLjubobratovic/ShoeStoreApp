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
import com.example.shoestoreapp.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecycleViewAdapter";

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<Float> ratings = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context mContext, ArrayList<String> mNames, ArrayList<String> mImageUrls, ArrayList<Float> ratings) {
        this.mNames = mNames;
        this.mImageUrls = mImageUrls;
        this.ratings = ratings;
        this.mContext = mContext;
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

        Glide.with(mContext)
                .asBitmap()
                .load(mImageUrls.get(position))
                .into(holder.productImage);
        holder.productName.setText(mNames.get(position));
        holder.productRating.setRating(ratings.get(position));

        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO item onclick
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView productImage;
        TextView productName;
        RatingBar productRating;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewItem);
            productName = itemView.findViewById(R.id.textViewItemName);
            productRating = itemView.findViewById(R.id.ratingBar);

        }
    }
}
