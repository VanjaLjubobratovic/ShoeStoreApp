package com.example.shoestoreapp.employee.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DeliveryRecyclerViewAdapter extends RecyclerView.Adapter<DeliveryRecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private MaterialButton confirmBtn, addBtn;
    private EditText modelEt;
    private ItemModel itemToEdit;

    private ArrayList<ItemModel> deliveryItems;

    private OnDeliveryItemListener deliveryItemListener;

    public DeliveryRecyclerViewAdapter (Context mContext, ArrayList<ItemModel> deliveryItems, OnDeliveryItemListener deliveryItemListener,
                                        MaterialButton confirmBtn, ItemModel itemToEdit, EditText modelEt, MaterialButton addBtn) {
        this.mContext = mContext;
        this.deliveryItems = deliveryItems;
        this.deliveryItemListener = deliveryItemListener;
        this.confirmBtn = confirmBtn;
        this.itemToEdit = itemToEdit;
        this.modelEt = modelEt;
        this.addBtn = addBtn;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //TODO: change layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_delivery, parent, false);
        return new ViewHolder(view, deliveryItemListener);
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

        if(getItemCount() > 0)
            confirmBtn.setEnabled(true);

        holder.editBtn.setOnClickListener(view -> {
            //In case user tries to edit another item during editing
            if (!itemToEdit.getModel().equals(" ") && addBtn.isEnabled()) {
                Toast.makeText(mContext, "Prvo spremite promjene na trenutnom predmetu", Toast.LENGTH_SHORT).show();
                return;
            }

            itemToEdit.parseModelColor(item.getModel() + "-" + item.getColor());
            itemToEdit.setAmounts(item.getAmounts());
            modelEt.setText(deliveryItems.get(holder.getLayoutPosition()).getModel());
            modelEt.setEnabled(false);
            removeAt(holder.getLayoutPosition());
            Log.d("DELIVERY RECYCLER", "item: " + itemToEdit.toString());
        });

        holder.removeBtn.setOnClickListener(view -> {
            removeAt(holder.getLayoutPosition());
        });
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

    private void removeAt(int position) {
        deliveryItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        if (getItemCount() == 0)
            confirmBtn.setEnabled(false);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView itemImage;
        TextView itemName, totalAmount;
        LinearLayout amountsLayout, dynamicButtonLayout;
        OnDeliveryItemListener deliveryItemListener;
        MaterialButton editBtn, removeBtn;
        boolean expanded = false;

        public ViewHolder(View itemView, OnDeliveryItemListener onDeliveryItemListener) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.recyclerItemImage);
            itemName = itemView.findViewById(R.id.recylcerItemName);
            totalAmount = itemView.findViewById(R.id.recyclerTotalAmount);
            amountsLayout = itemView.findViewById(R.id.recyclerSizesLayout);
            dynamicButtonLayout = itemView.findViewById(R.id.recyclerButtonsLayout);
            editBtn = itemView.findViewById(R.id.recyclerItemEditBtn);
            removeBtn = itemView.findViewById(R.id.recyclerItemRemoveBtn);

            this.deliveryItemListener = onDeliveryItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            deliveryItemListener.onDeliveryItemClick(getAbsoluteAdapterPosition());

            if(!expanded) {
                editBtn.setVisibility(View.VISIBLE);
                removeBtn.setVisibility(View.VISIBLE);
                expanded = true;
            } else {
                editBtn.setVisibility(View.GONE);
                removeBtn.setVisibility(View.GONE);
                expanded = false;
            }
        }
    }

    public interface OnDeliveryItemListener {
        void onDeliveryItemClick(int position);
    }
}
