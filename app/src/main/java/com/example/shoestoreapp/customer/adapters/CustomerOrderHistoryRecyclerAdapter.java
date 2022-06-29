package com.example.shoestoreapp.customer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.DataModels.OrderModel;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CustomerOrderHistoryRecyclerAdapter extends RecyclerView.Adapter<CustomerOrderHistoryRecyclerAdapter.ViewHolder> implements CustomerSingleOrderRecyclerAdapter.onItemReviewListener{
    ArrayList<OrderModel> mOrders;
    Context mContext;
    onItemReviewGet mOnItemReviewGet;
    UserModel user;
    private ArrayList<String> mUserReviews;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_history_list_item, parent, false);
        return new ViewHolder(view);
    }

    public CustomerOrderHistoryRecyclerAdapter(Context mContext, ArrayList<OrderModel> mOrders, onItemReviewGet listener, ArrayList<String> userReviews) {
        this.mOrders = mOrders;
        this.mContext = mContext;
        mOnItemReviewGet = listener;
        mUserReviews = userReviews;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel currentOrder = mOrders.get(position);

        Locale locale = new Locale("hr", "HR");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        String date = dateFormat.format(currentOrder.getDateCreated().toDate());

        holder.orderTime.setText(date);
        if(currentOrder.isPickedUp()) {
            holder.orderStatus.setText("Dostavljeno");
            holder.orderStatus.setTextColor(Color.GREEN);
        }
        else if(currentOrder.isInStore()){
            holder.orderStatus.setText("Poslano");
            holder.orderStatus.setTextColor(ContextCompat.getColor(mContext, R.color.darkerYellow));
        }
        else{
            holder.orderStatus.setText("Nije još poslano");
            holder.orderStatus.setTextColor(Color.RED);
        }
        Integer tmpPrice = (int)currentOrder.getTotal();
        holder.finalPrice.setText(tmpPrice.toString()+"kn");

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false);
        ((SimpleItemAnimator)holder.orderItems.getItemAnimator()).setSupportsChangeAnimations(false);
        holder.orderItems.setLayoutManager(layoutManager);

        CustomerSingleOrderRecyclerAdapter adapter = new CustomerSingleOrderRecyclerAdapter(mContext, currentOrder, this, mUserReviews);
        holder.orderItems.setAdapter(adapter);

        boolean isVisible = currentOrder.isExpanded();
        holder.expandedLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        String code = currentOrder.getOrderCode().toString();
        holder.orderCode.setText(code.substring(0,5));

        if(currentOrder.isPickedUp() || !currentOrder.isInStore()){
            holder.confirmDelivery.setClickable(false);
            holder.confirmDelivery.setAlpha(0.5f);
        }

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
    public void OnItemComplaintClick(ItemModel item, String orderCode) {
        mOnItemReviewGet.itemComplaintGet(item, orderCode);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderTime, orderStatus, finalPrice, orderCode;
        RecyclerView orderItems;
        ConstraintLayout expandedLayout, itemLayout;
        MaterialButton confirmDelivery;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderCode = itemView.findViewById(R.id.orderHistoryOrderCode);
            orderTime = itemView.findViewById(R.id.orderHistoryTimeTextView);
            orderStatus = itemView.findViewById(R.id.orderHistoryStatusTextView);
            finalPrice = itemView.findViewById(R.id.orderHistoryPriceTextView);
            orderItems = itemView.findViewById(R.id.orderHistoryItemsRecyclerView);
            expandedLayout = itemView.findViewById(R.id.orderHistorySubLayout);
            itemLayout = itemView.findViewById(R.id.orderHistoryItemLayout);
            confirmDelivery = itemView.findViewById(R.id.confirmDeliveryButton);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OrderModel order = mOrders.get(getBindingAdapterPosition());
                    order.setExpanded(!order.isExpanded());
                    notifyItemChanged(getBindingAdapterPosition());
                }
            });
            confirmDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemReviewGet.onConfirmClick(mOrders.get(getBindingAdapterPosition()));
                }
            });

        }

    }

    public interface onItemReviewGet{
        public void itemReviewGet(ItemModel reviewItem);
        public void itemComplaintGet(ItemModel reviewItem, String orderCode);
        public void onConfirmClick(OrderModel order);
    }
}
