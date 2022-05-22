package com.example.shoestoreapp.customer;

import android.content.Context;
import android.util.Log;
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

import java.util.ArrayList;

public class ModelRecycleViewAdapter extends RecyclerView.Adapter<ModelRecycleViewAdapter.ViewHolder>{
    private ArrayList<ItemModel> items;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Context mContext;
    private static final String TAG = "ModelRecycleViewAdapter";

    public ModelRecycleViewAdapter(Context mContext, ArrayList<ItemModel> items) {
        this.items = items;
        this.mContext = mContext;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_model_list_item, parent, false);
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
                .into(holder.modelImage);

        holder.modelName.setText("Model " + item.getModel());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO model onClick
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView modelImage;
        TextView modelName;

        public ViewHolder(View itemView) {
            super(itemView);
            modelImage = itemView.findViewById(R.id.imageViewModel);
            modelName = itemView.findViewById(R.id.textViewModelName);


        }
    }
}
