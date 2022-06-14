package com.example.shoestoreapp.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoreRecyclerViewAdapter extends RecyclerView.Adapter<StoreRecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private FragmentActivity activity;
    private ArrayList<StoreModel> storeList;
    private FirebaseFirestore database;

    public StoreRecyclerViewAdapter(Context mContext, ArrayList<StoreModel> storeList, FragmentActivity activity) {
        this.mContext = mContext;
        this.storeList = storeList;
        this.activity = activity;

        database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public StoreRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_recycler_item, parent, false);
        return new StoreRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreModel store = storeList.get(position);

        holder.storeID.setText(store.getStoreID());
        holder.storeAddress.setText(store.getAddress());
        holder.storeType.setText(store.getType());

        if(store.getType().equals("webshop"))
            holder.storeImage.setImageResource(R.drawable.ic_baseline_computer_24);
        else if(store.getType().equals("store"))
            holder.storeImage.setImageResource(R.drawable.ic_baseline_store_24);
        else holder.storeImage.setImageResource(R.drawable.ic_baseline_storefront_24);

        for(String employee : store.getEmployees())
            holder.storeEmployees.append(employee + ", ");

        if (store.getEmployees().isEmpty())
            holder.storeEmployees.append("NEMA ZAPOSLENIH");

        if(!store.isEnabled())
            holder.itemView.setBackgroundColor(mContext.getColor(R.color.annulledRed));

        holder.itemView.setOnClickListener(view -> {
            if(holder.employeesBtn.getVisibility() == View.GONE) {
                holder.employeesBtn.setVisibility(View.VISIBLE);
                holder.relocateBtn.setVisibility(View.VISIBLE);
            } else {
                holder.employeesBtn.setVisibility(View.GONE);
                holder.relocateBtn.setVisibility(View.GONE);
            }
        });

        holder.employeesBtn.setOnClickListener(view -> {
            StoreManageEmployeesFragment fragment = StoreManageEmployeesFragment.newInstance(storeList.get(holder.getAbsoluteAdapterPosition()).getStoreID(),
                    storeList.get(holder.getAbsoluteAdapterPosition()).getEmployees());
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            fragmentTransaction.replace(R.id.adminActivityLayout, fragment);
            fragmentTransaction.addToBackStack("name").commit();
        });

        holder.relocateBtn.setOnClickListener(view -> {

        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView storeImage;
        TextView storeID, storeAddress, storeType, storeEmployees;
        MaterialButton employeesBtn, relocateBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storeImage = itemView.findViewById(R.id.storeImage);
            storeID = itemView.findViewById(R.id.storeID);
            storeAddress = itemView.findViewById(R.id.storeAddress);
            storeType = itemView.findViewById(R.id.storeType);
            storeEmployees = itemView.findViewById(R.id.storeEmployees);
            employeesBtn = itemView.findViewById(R.id.employeesButton);
            relocateBtn = itemView.findViewById(R.id.relocateButton);

            employeesBtn.setVisibility(View.GONE);
            relocateBtn.setVisibility(View.GONE);
        }
    }
}
