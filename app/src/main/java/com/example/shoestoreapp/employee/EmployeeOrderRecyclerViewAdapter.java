package com.example.shoestoreapp.employee;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.customer.TestOrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.type.DateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class EmployeeOrderRecyclerViewAdapter extends RecyclerView.Adapter<EmployeeOrderRecyclerViewAdapter.ViewHolder>{

    private ArrayList<ItemModel> mItems;
    private ArrayList<String> mCustomerNames;
    private ArrayList<LocalDate> mOrderDates;
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public EmployeeOrderRecyclerViewAdapter(Context mContext, ArrayList<ItemModel> mItems, ArrayList<String> mCustomerNames, ArrayList<LocalDate> mOrderDates, MyAdapterListener listener) {
        this.mItems = mItems;
        this.mCustomerNames = mCustomerNames;
        this.mOrderDates = mOrderDates;
        this.mContext = mContext;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        onClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_order_list_item, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel currentItem = mItems.get(position);
        holder.modelAndColor.setText(currentItem.getModel() + " " + currentItem.getColor());
        ArrayList<Integer> tmpAmounts = currentItem.getAmounts();
        Integer size = tmpAmounts.indexOf(1);
        holder.size.setText(Integer.toString(currentItem.getSizes().get(size)));
        holder.customerName.setText(mCustomerNames.get(position));
        long dayDiff = ChronoUnit.DAYS.between(mOrderDates.get(position), LocalDateTime.now());
        holder.orderAge.setText(Integer.toString((int) dayDiff) + " dana");

        StorageReference imageReference = storageRef.child(currentItem.getImage());

        Glide.with(mContext)
                .asBitmap()
                .load(imageReference)
                .into(holder.modelImage);

        boolean isVisible = currentItem.isEmployeeOrderExpanded();
        holder.expandedItem.setVisibility(isVisible ? View.VISIBLE : View.GONE);


    }

    public void deleteItem(final int position) {

        mItems.remove(position);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView modelAndColor, size, customerName, orderAge;
        ImageView modelImage;
        MaterialButton deleteButton, editButton;
        ConstraintLayout expandedItem, fullItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            modelAndColor = itemView.findViewById(R.id.employeeOrderItemModelAndColorTextView);
            size = itemView.findViewById(R.id.employeeOrderItemSizeTextView);
            customerName = itemView.findViewById(R.id.employeeOrderItemNameTextView);
            orderAge = itemView.findViewById(R.id.employeeOrderItemAgeTextView);
            modelImage = itemView.findViewById(R.id.employeeOrderItemModelImage);
            deleteButton = itemView.findViewById(R.id.employeeOrderItemDeleteButton);
            editButton = itemView.findViewById(R.id.employeeOrderItemDeleteButton);
            expandedItem = itemView.findViewById(R.id.employeeOrderItemSubLayout);
            fullItem = itemView.findViewById(R.id.employeeOrderItemFullLayout);

            fullItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ItemModel item = mItems.get(getBindingAdapterPosition());
                    item.setEmployeeOrderExpanded(!item.isEmployeeOrderExpanded());
                    notifyItemChanged(getBindingAdapterPosition());
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.deleteButtonOnClick(view, getBindingAdapterPosition());
                }
            });
        }
    }

    public MyAdapterListener onClickListener;

    public interface MyAdapterListener {

        void deleteButtonOnClick(View v, int position);
        void editButtonOnClick(View v, int position);
    }
}
