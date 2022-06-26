package com.example.shoestoreapp.employee;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReceiptListRecyclerViewAdapter extends RecyclerView.Adapter<ReceiptListRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ReceiptListRecycleViewAdapter";
    private Context mContext;
    private ArrayList<ReceiptModel> receiptList;
    private OnReceiptListener onReceiptListener;
    private FragmentActivity activity;


    public ReceiptListRecyclerViewAdapter(Context mContext, ArrayList<ReceiptModel> receiptList, OnReceiptListener onReceiptListener, FragmentActivity activity) {
        this.mContext = mContext;
        this.receiptList = receiptList;
        this.onReceiptListener = onReceiptListener;
        this.activity = activity;


        for(ReceiptModel receipt : receiptList)
            if(receipt.isPacked())
                receipt.unpackItems();
    }

    @NonNull
    @Override
    public ReceiptListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receipt_list_item, parent, false);
        return new ReceiptListRecyclerViewAdapter.ViewHolder(view, onReceiptListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceiptModel receipt = receiptList.get(position);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        holder.receiptID.setText("ID računa:\n" + receipt.getReceiptID());
        holder.receiptTotal.setText("Iznos:\n" + receipt.getTotal() + "kn");
        holder.receiptTime.setText("Vrijeme:\n" + simpleDateFormat.format(receipt.getTime().toDate()));
        holder.itemList.setText("");

        if(receipt.isAnnulled())
            holder.parentLayout.setBackgroundColor(mContext.getColor(R.color.annulledRed));

        for(ItemModel item : receipt.getItems()) {
            int size = item.getSizes().get(item.getAmounts().indexOf(1));
            holder.itemList.append(item + " " + size + "\n");
        }

        holder.itemView.setOnClickListener(view -> {
            if (receiptList.get(position).isAnnulled()) {
                Toast.makeText(mContext, "Ne možete uređivati stornirani račun", Toast.LENGTH_SHORT).show();
                return;
            }

            if(holder.editButton.getVisibility() == View.GONE) {
                holder.editButton.setVisibility(View.VISIBLE);
                holder.annulButton.setVisibility(View.VISIBLE);
            } else {
                holder.editButton.setVisibility(View.GONE);
                holder.annulButton.setVisibility(View.GONE);
            }
        });

        holder.editButton.setOnClickListener(view -> {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            Fragment fragment = ReceiptFragment.newInstance(receiptList.get(position));

            ft.replace(R.id.employeeActivityLayout, fragment);
            ft.addToBackStack("name").commit();
        });

        holder.annulButton.setOnClickListener(view -> {
            annulReceipt(holder.getAbsoluteAdapterPosition(), holder);
        });
    }

    private void annulReceipt(int position, ViewHolder holder) {
        FirebaseFirestore.getInstance().document("/receipts/" + receiptList.get(position).getReceiptID())
                .update("annulled", true)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(mContext, "Račun uspješno storniran", Toast.LENGTH_SHORT).show();
                        adjustInventory(position);
                        receiptList.get(position).setAnnulled(true);
                        buttonsGone(holder);
                        notifyItemChanged(position);
                    }
                });
    }

    private void buttonsGone(ViewHolder holder) {
        holder.editButton.setVisibility(View.GONE);
        holder.annulButton.setVisibility(View.GONE);
    }

    private void adjustInventory(int position) {
        ArrayList<ItemModel> itemsToAdjust = receiptList.get(position).getItems();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String storeID = receiptList.get(position).getStoreID();

        //TODO: find if there's a better method
        for(ItemModel item : itemsToAdjust) {
            DocumentReference itemDocumentRef = database.document("/locations/" + storeID + "/items/" + item.toString());
            ArrayList<Integer> adjustedAmountsList = new ArrayList<>();

            //TODO: Generalise this boilerplate code
            itemDocumentRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Log.d("FIRESTORE", task.getResult().toString());
                    ItemModel itemToEdit = task.getResult().toObject(ItemModel.class);
                    if(itemToEdit == null)
                        return;

                    for(int i = 0; i < itemToEdit.getAmounts().size(); i++) {
                        int adjustedAmount = itemToEdit.getAmounts().get(i) + item.getAmounts().get(i);
                        adjustedAmountsList.add(adjustedAmount);

                        //TODO:Merge this with fetchItem method
                    }

                    itemDocumentRef.update("amounts", adjustedAmountsList)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("adjustInventory", "Succesful amount update");
                                }
                            });
                }
            }).addOnFailureListener(e -> Log.d("FIRESTORE", "task not successful"));
        }
    }

    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView receiptID, receiptTotal, receiptTime, itemList;
        LinearLayout parentLayout;
        OnReceiptListener onReceiptListener;
        MaterialButton editButton, annulButton;

        public ViewHolder(View itemView, OnReceiptListener onReceiptListener) {
            super(itemView);
            receiptID = itemView.findViewById(R.id.receiptID);
            receiptTotal = itemView.findViewById(R.id.receiptTotal);
            receiptTime = itemView.findViewById(R.id.receiptTime);
            itemList = itemView.findViewById(R.id.receiptItemList);
            parentLayout = itemView.findViewById(R.id.receiptListItemLinearLayout);
            editButton = itemView.findViewById(R.id.editButton);
            annulButton = itemView.findViewById(R.id.annulButton);

            editButton.setVisibility(View.GONE);
            annulButton.setVisibility(View.GONE);

            this.onReceiptListener = onReceiptListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onReceiptListener.onReceiptClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnReceiptListener {
        void onReceiptClick(int position);
    }
}
