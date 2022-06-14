package com.example.shoestoreapp.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoreEmployeesRecyclerViewAdapter extends RecyclerView.Adapter<StoreEmployeesRecyclerViewAdapter.ViewHolder> {
    Context mContext;
    ArrayList<UserModel> storeEmployees, nonStoreEmployees;
    ArrayList<UserModel> allEmployees;
    String storeID;

    FirebaseFirestore database;

    public StoreEmployeesRecyclerViewAdapter(Context mContext, ArrayList<UserModel> storeEmployees, ArrayList<UserModel> nonStoreEmployees, String storeID) {
        this.storeEmployees = storeEmployees;
        this.nonStoreEmployees = nonStoreEmployees;
        this.mContext = mContext;
        this.storeID = storeID;

        allEmployees = new ArrayList<>();
        allEmployees.addAll(storeEmployees);
        allEmployees.addAll(nonStoreEmployees);
    }

    @NonNull
    @Override
    public StoreEmployeesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        database = FirebaseFirestore.getInstance();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_employee_item, parent, false);
        return new StoreEmployeesRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel employee = allEmployees.get(position);

        holder.employeeName.setText(employee.getEmail());
        if(storeEmployees.contains(employee))
            holder.addRemoveBtn.setImageResource(R.drawable.ic_baseline_person_remove_24);
        else holder.addRemoveBtn.setImageResource(R.drawable.ic_baseline_person_add_24);

        holder.addRemoveBtn.setOnClickListener(view -> {
            if(storeEmployees.contains(allEmployees.get(holder.getAbsoluteAdapterPosition()))) {
                removeEmployee(allEmployees.get(holder.getAbsoluteAdapterPosition()));
            } else addEmployee(allEmployees.get(holder.getAbsoluteAdapterPosition()));

            updateStoreEmployees();
        });

    }

    private void removeEmployee(UserModel user) {
        storeEmployees.remove(user);
        nonStoreEmployees.add(user);
        allEmployees = new ArrayList<>();
        allEmployees.addAll(storeEmployees);
        allEmployees.addAll(nonStoreEmployees);
        notifyDataSetChanged();
    }

    private void addEmployee(UserModel user) {
        nonStoreEmployees.remove(user);
        storeEmployees.add(user);
        allEmployees = new ArrayList<>();
        allEmployees.addAll(storeEmployees);
        allEmployees.addAll(nonStoreEmployees);
        notifyDataSetChanged();
    }

    private void updateStoreEmployees() {
        database = FirebaseFirestore.getInstance();
        ArrayList<String> users = new ArrayList<>();

        for(UserModel user : storeEmployees)
            users.add(user.getEmail());

        database.document("/locations/" + storeID)
                .update("employees", users)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       Toast.makeText(mContext, "Uspješno ažuriranje zaposlenika", Toast.LENGTH_SHORT).show();
                   }
                });
    }

    @Override
    public int getItemCount() {
        return allEmployees.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView employeeImage;
        TextView employeeName;
        ImageButton addRemoveBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            employeeImage = itemView.findViewById(R.id.storeEmployeeImage);
            employeeName = itemView.findViewById(R.id.storeEmployeeName);
            addRemoveBtn = itemView.findViewById(R.id.storeAddRemoveEmployee);
        }
    }
}
