package com.example.shoestoreapp.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.ComplaintModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class AdminComplaintsAdapter extends RecyclerView.Adapter<AdminComplaintsAdapter.ViewHolder>{

    private ArrayList<ComplaintModel> mComplaints = new ArrayList<>();
    private Context mContext;
    private onComplaintListener mOnComplaintListener;

    public AdminComplaintsAdapter(Context mContext, ArrayList<ComplaintModel> mComplaints, onComplaintListener listener) {
        this.mComplaints = mComplaints;
        this.mContext = mContext;
        mOnComplaintListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_complaints_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComplaintModel curComplaint = mComplaints.get(position);
        holder.orderCode.setText(curComplaint.getOrderCode().toString());
        holder.itemModel.setText(curComplaint.getModel());
        holder.complaintType.setText("Vrsta: " + curComplaint.getComplaintType());
        holder.complaintText.setText(curComplaint.getComplaint());
        if(curComplaint.isResend()){
            holder.newArticle.setText("Korisnik želi novi artikl");
            holder.resendLayout.setVisibility(View.VISIBLE);
            holder.noResendLayout.setVisibility(View.GONE);
        }
        else{
            holder.newArticle.setText("Korisnik ne želi novi artikl");
            holder.resendLayout.setVisibility(View.GONE);
            holder.noResendLayout.setVisibility(View.VISIBLE);
        }



    }

    @Override
    public int getItemCount() {
        return mComplaints.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView orderCode, itemModel, complaintType, complaintText, newArticle;
        MaterialButton resendConfirm, resendDeny, markAsRead;
        ConstraintLayout resendLayout, noResendLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderCode = itemView.findViewById(R.id.adminComplaintItemOrder);
            itemModel = itemView.findViewById(R.id.adminComplaintItemModel);
            complaintType = itemView.findViewById(R.id.adminComplaintItemReason);
            complaintText = itemView.findViewById(R.id.adminComplaintItemComplaint);
            newArticle = itemView.findViewById(R.id.adminComplaintResend);

            resendConfirm = itemView.findViewById(R.id.adminConfirmResend);
            resendDeny = itemView.findViewById(R.id.adminRefuseResend);
            markAsRead = itemView.findViewById(R.id.adminMarkAsRead);

            resendLayout = itemView.findViewById(R.id.adminItemReturnLayout);
            noResendLayout = itemView.findViewById(R.id.adminMarkAsReadLayout);

            resendConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnComplaintListener.OnComplaintResendClick(mComplaints.get(getBindingAdapterPosition()));
                }
            });

            resendDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnComplaintListener.OnComplaintDenyClick(mComplaints.get(getBindingAdapterPosition()));
                }
            });

            markAsRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnComplaintListener.OnComplaintSeenClick(mComplaints.get(getBindingAdapterPosition()));
                }
            });


        }
    }

    public interface onComplaintListener{
        void OnComplaintResendClick(ComplaintModel complaint);
        void OnComplaintDenyClick(ComplaintModel complaint);
        void OnComplaintSeenClick(ComplaintModel complaint);
    }


}
