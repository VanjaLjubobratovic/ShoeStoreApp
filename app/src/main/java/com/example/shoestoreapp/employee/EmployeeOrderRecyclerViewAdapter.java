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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.customer.TestOrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.type.DateTime;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;

public class EmployeeOrderRecyclerViewAdapter extends RecyclerView.Adapter<EmployeeOrderRecyclerViewAdapter.ViewHolder>{

    private ArrayList<OrderModel> mOrders;
    private Context mContext;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public EmployeeOrderRecyclerViewAdapter(Context mContext, ArrayList<OrderModel> mOrders, MyAdapterListener listener) {
        this.mContext = mContext;
        this.mOrders = mOrders;
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

        OrderModel currentOrder = mOrders.get(position);
        holder.customerName.setText(mOrders.get(position).getUser());
        long miliCreated = mOrders.get(position).getDateCreated().toDate().getTime();
        long miliNow = System.currentTimeMillis();
        long dayDiff = (miliCreated - miliNow) / (24*60*60*1000);
        holder.orderAge.setText(Long.toString(dayDiff) + " dana");

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        holder.singleOrderRecycler.setLayoutManager(layoutManager);

        EmployeeOrderSingleRecyclerAdapter adapter = new EmployeeOrderSingleRecyclerAdapter(mContext, currentOrder.getItems());
        holder.singleOrderRecycler.setAdapter(adapter);

    }

    public void deleteItem(final int position) {
        mOrders.remove(position);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView customerName, orderAge;
        RecyclerView singleOrderRecycler;
        MaterialButton deleteButton;
        ConstraintLayout expandedItem, fullItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.employeeOrderItemNameTextView);
            orderAge = itemView.findViewById(R.id.employeeOrderItemAgeTextView);
            singleOrderRecycler = itemView.findViewById(R.id.employeeOrderItemRecyclerView);
            deleteButton = itemView.findViewById(R.id.employeeOrderItemDeleteButton);
            expandedItem = itemView.findViewById(R.id.employeeOrderItemSubLayout);
            fullItem = itemView.findViewById(R.id.employeeOrderItemFullLayout);


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
    }
}
