package com.example.shoestoreapp.customer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.shoestoreapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CustomerOrderHistoryRecyclerAdapter extends RecyclerView.Adapter<CustomerOrderHistoryRecyclerAdapter.ViewHolder> implements CustomerSingleOrderRecyclerAdapter.onItemReviewListener{
    ArrayList<TestOrderModel> mOrders;
    Context mContext;
    onItemReviewGet mOnItemReviewGet;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_history_list_item, parent, false);
        return new ViewHolder(view);
    }

    public CustomerOrderHistoryRecyclerAdapter(Context mContext, ArrayList<TestOrderModel> mOrders, onItemReviewGet listener) {
        this.mOrders = mOrders;
        this.mContext = mContext;
        mOnItemReviewGet = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestOrderModel currentOrder = mOrders.get(position);

        holder.orderTime.setText(currentOrder.getTime());
        holder.orderStatus.setText("Not delivered");
        if(holder.orderStatus.getText().equals("Not delivered")){
            holder.orderStatus.setTextColor(Color.RED);
        }
        holder.finalPrice.setText(currentOrder.getPrice());
        holder.orderId.setText("Random id");

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false);
        ((SimpleItemAnimator)holder.orderItems.getItemAnimator()).setSupportsChangeAnimations(false);
        holder.orderItems.setLayoutManager(layoutManager);

        CustomerSingleOrderRecyclerAdapter adapter = new CustomerSingleOrderRecyclerAdapter(mContext, currentOrder.getItems(), this);
        holder.orderItems.setAdapter(adapter);

        boolean isVisible = currentOrder.isExpanded();
        holder.expandedLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    @Override
    public void OnItemReviewClick(ItemModel item) {
        mOnItemReviewGet.itemReviewGet(item);
    }

    @Override
    public void OnItemComplaintClick(ItemModel item) {
        mOnItemReviewGet.itemComplaintGet(item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderTime, orderStatus, finalPrice, orderId;
        RecyclerView orderItems;
        ConstraintLayout expandedLayout, itemLayout;
        MaterialButton confirmDelivery;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderTime = itemView.findViewById(R.id.orderHistoryTimeTextView);
            orderStatus = itemView.findViewById(R.id.orderHistoryStatusTextView);
            finalPrice = itemView.findViewById(R.id.orderHistoryPriceTextView);
            orderId = itemView.findViewById(R.id.orderHistoryIdTextView);
            orderItems = itemView.findViewById(R.id.orderHistoryItemsRecyclerView);
            expandedLayout = itemView.findViewById(R.id.orderHistorySubLayout);
            itemLayout = itemView.findViewById(R.id.orderHistoryItemLayout);
            confirmDelivery = itemView.findViewById(R.id.confirmDeliveryButton);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TestOrderModel order = mOrders.get(getBindingAdapterPosition());
                    order.setExpanded(!order.isExpanded());
                    notifyItemChanged(getBindingAdapterPosition());
                }
            });
            confirmDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orderStatus.setText("Delivered");
                    orderStatus.setTextColor(Color.GREEN);
                }
            });


        }

    }

    public interface onItemReviewGet{
        public void itemReviewGet(ItemModel reviewItem);
        public void itemComplaintGet(ItemModel reviewItem);
    }
}
