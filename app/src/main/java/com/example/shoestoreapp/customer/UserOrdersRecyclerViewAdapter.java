package com.example.shoestoreapp.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.shoestoreapp.R;

import java.util.ArrayList;

public class UserOrdersRecyclerViewAdapter extends RecyclerView.Adapter<UserOrdersRecyclerViewAdapter.ViewHolder>{

    ArrayList<TestOrderModel> mOrders;
    Context mContext;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_item, parent, false);
        return new ViewHolder(view);
    }

    public UserOrdersRecyclerViewAdapter(Context mContext, ArrayList<TestOrderModel> mOrders) {
        this.mOrders = mOrders;
        this.mContext = mContext;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestOrderModel currentOrder = mOrders.get(position);

        holder.orderTime.setText(currentOrder.getTime());
        holder.employee.setText(currentOrder.getEmployee());
        holder.finalPrice.setText(currentOrder.getPrice());

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false);
        ((SimpleItemAnimator)holder.orderItems.getItemAnimator()).setSupportsChangeAnimations(false);
        holder.orderItems.setLayoutManager(layoutManager);

        PurchasedItemsRecyclerViewAdapter adapter = new PurchasedItemsRecyclerViewAdapter(mContext, currentOrder.getItems());
        holder.orderItems.setAdapter(adapter);

        boolean isVisible = currentOrder.isExpanded();
        holder.expandedLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderTime, employee, finalPrice;
        RecyclerView orderItems;
        ConstraintLayout expandedLayout;
        RelativeLayout itemLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderTime = itemView.findViewById(R.id.orderTimeTextView);
            employee = itemView.findViewById(R.id.orderEmployeeTextView);
            finalPrice = itemView.findViewById(R.id.orderPriceTextView);
            orderItems = itemView.findViewById(R.id.orderItemsRecyclerView);
            expandedLayout = itemView.findViewById(R.id.sub_item_layout);
            itemLayout = itemView.findViewById(R.id.orderRelativeLayout);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    TestOrderModel order = mOrders.get(getBindingAdapterPosition());
                    order.setExpanded(!order.isExpanded());
                    notifyItemChanged(getBindingAdapterPosition());
                }
            });
        }
    }
}
