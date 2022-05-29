package com.example.shoestoreapp.employee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReceiptListRecyclerViewAdapter extends RecyclerView.Adapter<ReceiptListRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ReceiptListRecycleViewAdapter";
    private Context mContext;
    private ArrayList<ReceiptModel> receiptList;


    public ReceiptListRecyclerViewAdapter(Context mContext, ArrayList<ReceiptModel> receiptList) {
        this.mContext = mContext;
        this.receiptList = receiptList;

        for(ReceiptModel receipt : receiptList)
            if(receipt.isPacked())
                receipt.unpackItems();
    }

    @NonNull
    @Override
    public ReceiptListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receipt_list_item, parent, false);
        return new ReceiptListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceiptModel receipt = receiptList.get(position);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        holder.receiptID.setText("ID računa:\n" + receipt.getStoreID());
        holder.receiptTotal.setText("Iznos:\n" + receipt.getTotal() + "kn");
        holder.receiptTime.setText("Vrijeme:\n" + simpleDateFormat.format(receipt.getTime().toDate()));
        holder.itemList.setText("");

        for(ItemModel item : receipt.getItems()) {
            int size = item.getSizes().get(item.getAmounts().indexOf(1));
            holder.itemList.append(item + " " + size + "\n");
        }

        //TODO: add onclick
    }

    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView receiptID, receiptTotal, receiptTime, itemList;

        public ViewHolder(View itemView) {
            super(itemView);
            receiptID = itemView.findViewById(R.id.receiptID);
            receiptTotal = itemView.findViewById(R.id.receiptTotal);
            receiptTime = itemView.findViewById(R.id.receiptTime);
            itemList = itemView.findViewById(R.id.receiptItemList);
        }
    }
}
