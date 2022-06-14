package com.example.shoestoreapp.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.common.StringUtils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployeeRecyclerViewAdapter extends RecyclerView.Adapter<EmployeeRecyclerViewAdapter.ViewHolder> {
    private ArrayList<UserModel> employeeList;
    private Context mContext;

    private FirebaseFirestore database;

    public EmployeeRecyclerViewAdapter(ArrayList<UserModel> employeeList, Context mContext) {
        this.employeeList = employeeList;
        this.mContext = mContext;

        database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public EmployeeRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_employee_item, parent, false);
        return  new EmployeeRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel employee = employeeList.get(position);

        holder.name.setText(employee.getFullName());
        holder.email.setText(employee.getEmail());
        holder.address.setText(employee.getAddress());

        loadStores(holder, employee.getEmail());

        //TODO: load user image

        holder.itemView.setOnClickListener(view -> {
            if(holder.fireBtn.getVisibility() == View.GONE) {
                holder.fireBtn.setVisibility(View.VISIBLE);
                holder.promoteBtn.setVisibility(View.VISIBLE);
            } else {
                holder.fireBtn.setVisibility(View.GONE);
                holder.promoteBtn.setVisibility(View.GONE);
            }
        });

        holder.fireBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Otpusti zaposlenika");
            builder.setMessage("Želite li zaista otpustiti zaposlenika: " + employeeList.get(holder.getAbsoluteAdapterPosition()).getFullName() + "?");

            builder.setPositiveButton("OTPUSTI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    fireEmployee(employeeList.get(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition());
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton("ODUSTANI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        holder.promoteBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Unaprijedi zaposlenika");
            builder.setMessage("Želite li zaista unaprijediti zaposlenika: " + employeeList.get(holder.getAbsoluteAdapterPosition()).getFullName() + "?");

            builder.setPositiveButton("UNAPRIJEDI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    promoteEmployee(employeeList.get(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition());
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton("ODUSTANI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }

    private void loadStores(ViewHolder holder, String email) {
        database.collection("/locations").whereArrayContains("employees", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot document : task.getResult()) {
                                holder.stores.append(" " + document.getId() + ",");
                            }

                            if (holder.stores.getText().equals("Trgovine: "))
                                holder.itemView.setBackgroundColor(mContext.getColor(R.color.annulledRed));
                        }
                    }
                });

    }

    private void promoteEmployee(UserModel employee, int position) {
        database.document("/users/" + employee.getEmail()).update("role", "admin")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(mContext, "Zaposlenik uspješno unaprijeđen", Toast.LENGTH_SHORT).show();
                        notifyItemChanged(position);
                    }
                });
    }

    private void fireEmployee(UserModel employee, int position) {
        //TODO: make admins unable to fire admins
        database.document("/users/" + employee.getEmail()).update("role", "customer")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(mContext, "Zaposlenik uspješno otpušten", Toast.LENGTH_SHORT).show();
                        employeeList.remove(employee);
                        notifyItemRemoved(position);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView employeeImage;
        TextView name, email, address, stores;
        MaterialButton fireBtn, promoteBtn;
        LinearLayout background;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            employeeImage = itemView.findViewById(R.id.employeeImage);
            name = itemView.findViewById(R.id.employeeName);
            email = itemView.findViewById(R.id.employeeEmail);
            address = itemView.findViewById(R.id.employeeAddress);
            stores = itemView.findViewById(R.id.employeeStores);
            fireBtn = itemView.findViewById(R.id.fireButton);
            promoteBtn = itemView.findViewById(R.id.promoteButton);
            background = itemView.findViewById(R.id.employeeBackground);

            fireBtn.setVisibility(View.GONE);
            promoteBtn.setVisibility(View.GONE);
        }
    }
}
