package com.example.shoestoreapp.employee;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DeliveryRecyclerViewAdapter extends RecyclerView.Adapter<DeliveryRecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private ArrayList<ItemModel> deliveryItems;

    public DeliveryRecyclerViewAdapter (Context mContext, ArrayList<ItemModel> deliveryItems) {
        this.mContext = mContext;
        this.deliveryItems = deliveryItems;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //TODO: change layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryRecyclerViewAdapter.ViewHolder holder, int position) {
        ItemModel item = deliveryItems.get(position);
        StorageReference imageReference = storageRef.child(item.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.itemImage);

        holder.itemName.setText(item.toString());
        holder.totalAmount.setText("Količina ukupno: " + totalAmount(item));

        addAmounts(holder, item);
        Log.d("DELIVERY RECYCLER", "item: " + item.toString());
    }

    @Override
    public int getItemCount() {
        return deliveryItems.size();
    }

    private int totalAmount(ItemModel item) {
        //TODO: make this a ItemModel class method
        int total = 0;
        ArrayList<Integer> amounts = item.getAmounts();
        for(Integer amount : amounts)
            total += amount;
        return total;
    }

    private void addAmounts(DeliveryRecyclerViewAdapter.ViewHolder holder, ItemModel item) {
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemImage;
        TextView itemName, totalAmount;
        LinearLayout amountsLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.recyclerItemImage);
            itemName = itemView.findViewById(R.id.recylcerItemName);
            totalAmount = itemView.findViewById(R.id.recyclerTotalAmount);
            amountsLayout = itemView.findViewById(R.id.recyclerSizesLayout);
        }
    }
}
