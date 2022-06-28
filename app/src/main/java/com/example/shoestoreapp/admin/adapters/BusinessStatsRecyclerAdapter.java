package com.example.shoestoreapp.admin.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class BusinessStatsRecyclerAdapter extends  RecyclerView.Adapter<BusinessStatsRecyclerAdapter.ViewHolder> {
    private Context mContext;
    private FirebaseStorage storage;
    private FirebaseFirestore database;


    private ArrayList<ItemModel> itemsSold;

    public BusinessStatsRecyclerAdapter(Context mContext, ArrayList<ItemModel> itemsSold) {
        this.mContext = mContext;
        this.itemsSold = itemsSold;

        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = itemsSold.get(position);
        StorageReference imageRef = storage.getReference().child(item.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageRef)
                .into(holder.itemImage);

        holder.itemName.setText(item.toString());
        holder.totalAmount.setText("Količina ukupno: " + totalAmount(item));

        addAmounts(holder, item);
    }

    private void addAmounts(BusinessStatsRecyclerAdapter.ViewHolder holder, ItemModel item) {
        holder.amountsLayout.removeAllViews();

        for(int i = 0; i < item.getAmounts().size(); i++) {
            LinearLayout ll = new LinearLayout(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 5, 30, 5);
            ll.setLayoutParams(params);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.CENTER);

            TextView tvAmounts = new TextView(mContext);
            tvAmounts.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tvAmounts.setText(item.getAmounts().get(i).toString());
            tvAmounts.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tvAmounts.setPaintFlags(tvAmounts.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvAmounts.setGravity(Gravity.CENTER);
            tvAmounts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            TextView tvSizes = new TextView(mContext);
            tvSizes.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tvSizes.setText(item.getSizes().get(i).toString());
            tvSizes.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tvSizes.setGravity(Gravity.CENTER);
            tvSizes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvSizes.setTextColor(mContext.getColor(R.color.black));

            View v = new View(mContext);
            LinearLayout.LayoutParams llDiv =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llDiv.width = 1;
            llDiv.height = 100;
            v.setLayoutParams(llDiv);
            v.setBackgroundColor(mContext.getColor(R.color.lightGrey));

            ll.addView(tvAmounts);
            ll.addView(tvSizes);
            holder.amountsLayout.addView(ll);
            holder.amountsLayout.addView(v);
        }

        if(holder.amountsLayout.getChildCount() != 0)
            holder.amountsLayout.removeViewAt(holder.amountsLayout.getChildCount() - 1);
    }

    private int totalAmount(ItemModel item) {
        //TODO: make this a ItemModel class method
        int total = 0;
        ArrayList<Integer> amounts = item.getAmounts();
        for(Integer amount : amounts)
            total += amount;
        return total;
    }

    @Override
    public int getItemCount() {
        return itemsSold.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, totalAmount;
        LinearLayout amountsLayout, dynamicButtonLayout;
        MaterialButton editBtn, removeBtn;
        boolean expanded = false;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.recyclerItemImage);
            itemName = itemView.findViewById(R.id.recylcerItemName);
            totalAmount = itemView.findViewById(R.id.recyclerTotalAmount);
            amountsLayout = itemView.findViewById(R.id.recyclerSizesLayout);
            dynamicButtonLayout = itemView.findViewById(R.id.recyclerButtonsLayout);
            editBtn = itemView.findViewById(R.id.recyclerItemEditBtn);
            removeBtn = itemView.findViewById(R.id.recyclerItemRemoveBtn);
        }
    }
}
