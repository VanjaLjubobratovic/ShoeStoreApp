package com.example.shoestoreapp.customer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;

import java.util.ArrayList;

public class CustomerComplaintsAdapter extends RecyclerView.Adapter<CustomerComplaintsAdapter.ViewHolder>{

    ArrayList<ComplaintModel> mComplaints;
    Context mContext;

    public CustomerComplaintsAdapter(Context mContext, ArrayList<ComplaintModel> mComplaints) {
        this.mComplaints = mComplaints;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cutomer_complaints_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComplaintModel curComp = mComplaints.get(position);
        holder.orderCode.setText("Narudžba: " + curComp.getOrderCode().toString());
        holder.complaint.setText(curComp.getComplaint());
        holder.complaintType.setText("Vrsta: " + curComp.getComplaintType());
        holder.status.setText("Status: " + curComp.getResolved());
        holder.item.setText(curComp.getModel() + " broj " + curComp.getSize());
        if(curComp.getResolved().equals("Approved")){
            holder.compLayout.setBackgroundColor(Color.GREEN);
        }
        else if(curComp.getResolved().equals("Denied")){
            holder.compLayout.setBackgroundColor(Color.RED);
            holder.denyReason.setText("Razlog: " + curComp.getReason());
            holder.denyReason.setVisibility(View.VISIBLE);
        }
        else{
            holder.compLayout.setBackgroundColor(Color.YELLOW);
        }

    }

    @Override
    public int getItemCount() {
        return mComplaints.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderCode, complaint, complaintType, status, item, denyReason;
        ConstraintLayout compLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderCode = itemView.findViewById(R.id.complaintItemOrder);
            complaint = itemView.findViewById(R.id.complaintItemComplaint);
            complaintType = itemView.findViewById(R.id.complaintItemReason);
            status = itemView.findViewById(R.id.complaintItemStatus);
            item = itemView.findViewById(R.id.complaintItemModel);
            denyReason = itemView.findViewById(R.id.complaintItemDenyReason);
            compLayout = itemView.findViewById(R.id.complaintItemLayout);
        }
    }

}
