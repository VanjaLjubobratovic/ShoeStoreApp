package com.example.shoestoreapp.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.admin.adapters.EmployeeRecyclerViewAdapter;
import com.example.shoestoreapp.databinding.FragmentEmployeeManagementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EmployeeManagementFragment extends Fragment {
    private FragmentEmployeeManagementBinding binding;
    private ViewFlipper flipper;
    private ImageButton back;
    private MaterialButton addEmployee;

    private ArrayList<UserModel> employeeList;

    private FirebaseFirestore database;

    private EmployeeRecyclerViewAdapter adapter;

    public EmployeeManagementFragment() {
        // Required empty public constructor
    }

    public static EmployeeManagementFragment newInstance() {
        EmployeeManagementFragment fragment = new EmployeeManagementFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmployeeManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flipper = binding.employeeManagementFlipper;
        back = binding.backBtn;
        addEmployee = flipper.getRootView().findViewById(R.id.newEmployeeBtn);

        fetchEmployees();
        initRecyclerView();

        back.setOnClickListener(view1 -> {
            try {
                getActivity().onBackPressed();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });

        addEmployee.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Unesite email zaposlenika");

            final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
            builder.setView(customLayout);

            builder.setPositiveButton("DODAJ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText et = customLayout.findViewById(R.id.editText);
                    if(!et.getText().toString().equals(""))
                        newEmployee(et.getText().toString());
                    else Toast.makeText(getContext(), "Morate unijeti email", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("ODUSTANI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });


            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void newEmployee(String email) {
        database.document("/users/" + email).update("role", "employee")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "Zaposlenik uspješno dodan", Toast.LENGTH_SHORT).show();
                            //TODO: ne ovako
                            fetchEmployees();
                        } else Toast.makeText(getContext(), "Neuspješno dodavanje zaposlenika", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchEmployees() {
        employeeList = new ArrayList<>();
        database.collection("users").whereNotEqualTo("role", "customer").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot document : task.getResult()) {
                                //TODO: don't add yourself
                                UserModel employee = document.toObject(UserModel.class);
                                employeeList.add(employee);
                            }
                            //adapter.notifyDataSetChanged();
                            initRecyclerView();
                        }
                    }
                });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = flipper.findViewById(R.id.employeeRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new EmployeeRecyclerViewAdapter(employeeList, getContext());
        recyclerView.setAdapter(adapter);
    }
}