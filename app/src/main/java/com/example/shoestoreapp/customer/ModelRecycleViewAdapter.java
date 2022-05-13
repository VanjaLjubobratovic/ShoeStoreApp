package com.example.shoestoreapp.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;

import java.util.ArrayList;

public class ModelRecycleViewAdapter extends RecyclerView.Adapter<ModelRecycleViewAdapter.ViewHolder>{
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private Context mContext;

    public ModelRecycleViewAdapter(Context mContext, ArrayList<String> mNames, ArrayList<String> mImageUrls) {
        this.mNames = mNames;
        this.mImageUrls = mImageUrls;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_model_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).
                asBitmap()
                .load(mImageUrls.get(position))
                .into(holder.modelImage);
        holder.modelName.setText(mNames.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO model onClick
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView modelImage;
        TextView modelName;

        public ViewHolder(View itemView) {
            super(itemView);
            modelImage = itemView.findViewById(R.id.imageViewItem);
            modelName = itemView.findViewById(R.id.textViewItemName);


        }
    }
}
