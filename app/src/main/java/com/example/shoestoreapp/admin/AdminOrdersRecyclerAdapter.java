package com.example.shoestoreapp.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.CustomerSingleOrderRecyclerAdapter;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.employee.EmployeeOrderSingleRecyclerAdapter;
import com.example.shoestoreapp.employee.OrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.firestore.v1.StructuredQuery;

import java.util.ArrayList;

public class AdminOrdersRecyclerAdapter extends RecyclerView.Adapter<AdminOrdersRecyclerAdapter.ViewHolder>{
    private ArrayList<OrderModel> mOrders= new ArrayList<>();
    private Context mContext;
    private onAdminOrders mOnAdminOrders;

    public AdminOrdersRecyclerAdapter(Context mContext, ArrayList<OrderModel> mOrders, onAdminOrders mOnAdminOrders) {
        this.mOrders = mOrders;
        this.mContext = mContext;
        this.mOnAdminOrders = mOnAdminOrders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_order_layout, parent, false);
        return new AdminOrdersRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel currentOrder = mOrders.get(position);
        holder.orderCode.setText(currentOrder.getOrderCode().toString());
        holder.orderCustomer.setText(currentOrder.getUser());
        holder.orderAddress.setText(currentOrder.getDeliveryAddress());
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false);
        ((SimpleItemAnimator)holder.orderItems.getItemAnimator()).setSupportsChangeAnimations(false);
        holder.orderItems.setLayoutManager(layoutManager);

        EmployeeOrderSingleRecyclerAdapter adapter = new EmployeeOrderSingleRecyclerAdapter(mContext, currentOrder.getItems());
        holder.orderItems.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderCustomer, orderCode, orderAddress;
        RecyclerView orderItems;
        MaterialButton confirmDelivery, denyDelivery;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderCustomer = itemView.findViewById(R.id.adminOrderCustomerTextView);
            orderCode = itemView.findViewById(R.id.adminOrderCodeTextView);
            orderAddress = itemView.findViewById(R.id.adminOrderAddressTextView);
            orderItems = itemView.findViewById(R.id.adminSingleOrderRecycler);
            confirmDelivery = itemView.findViewById(R.id.adminOrderConfirmButton);
            denyDelivery = itemView.findViewById(R.id.adminOrderDenyButton);

            confirmDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnAdminOrders.orderConfirmed(mOrders.get(getBindingAdapterPosition()));
                }
            });

            denyDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnAdminOrders.orderDenied(mOrders.get(getBindingAdapterPosition()));
                }
            });
        }
    }

    public interface onAdminOrders{
        public void orderConfirmed(OrderModel orderItem);
        public void orderDenied(OrderModel orderItem);
    }
}
