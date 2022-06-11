package com.example.shoestoreapp.employee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class InventoryGridAdapter extends ArrayAdapter<ItemModel> {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ArrayList<ItemModel> items;

    public InventoryGridAdapter(@NonNull Context context, ArrayList<ItemModel> itemList) {
        super(context, 0, itemList);

        this.items = itemList;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_grid_inventory_item, parent, false);
        }

        ItemModel item = getItem(position);
        StorageReference imageRef = storageRef.child(item.getImage());

        TextView itemDesc = listItemView.findViewById(R.id.gridItemDescription);
        ImageView itemImage = listItemView.findViewById(R.id.gridItemImage);

        itemDesc.setText(item.toString());
        Glide.with(getContext())
                .asBitmap()
                .load(imageRef)
                .into(itemImage);

        return listItemView;
    }

    public int getCount() {
        return items.size();
    }
}
