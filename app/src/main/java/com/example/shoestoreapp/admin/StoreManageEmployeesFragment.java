package com.example.shoestoreapp.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.admin.adapters.StoreEmployeesRecyclerViewAdapter;
import com.example.shoestoreapp.databinding.FragmentStoreManageEmployeesBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StoreManageEmployeesFragment extends Fragment {
    private FragmentStoreManageEmployeesBinding binding;
    private String storeID;
    private TextView title;

    private ArrayList<UserModel> storeEmployees, nonStoreEmployees;
    private ArrayList<String> storeEmployeesStringList;

    FirebaseFirestore database;

    public StoreManageEmployeesFragment() {
        // Required empty public constructor
    }

    public static StoreManageEmployeesFragment newInstance(String storeID, ArrayList<String> storeEmployees) {
        StoreManageEmployeesFragment fragment = new StoreManageEmployeesFragment();
        Bundle args = new Bundle();
        args.putString("storeID", storeID);
        args.putStringArrayList("employees", storeEmployees);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey("storeID") && getArguments().containsKey("employees")) {
            this.storeID = getArguments().getString("storeID");
            storeEmployeesStringList = getArguments().getStringArrayList("employees");
        } else Toast.makeText(getContext(), "No arguments", Toast.LENGTH_SHORT).show();

        database = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStoreManageEmployeesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = binding.storeEmployeesTitle;

        title.setText("Trgovina:" + storeID);
        fetchEmployees();
    }

    private void fetchEmployees() {
        database = FirebaseFirestore.getInstance();
        storeEmployees = new ArrayList<>();
        nonStoreEmployees = new ArrayList<>();

        database.collection("users").whereNotEqualTo("role", "customer").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot document : task.getResult()) {
                                UserModel employee = document.toObject(UserModel.class);
                                if(employee == null)
                                    continue;

                                if(storeEmployeesStringList.contains(employee.getEmail()))
                                    storeEmployees.add(employee);
                                else nonStoreEmployees.add(employee);
                            }

                            initRecyclerView();
                        }
                    }
                });
    }

    private void initRecyclerView() {
        Log.d("LISTS", storeEmployees.toString());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = binding.storeEmployeesRecycler;
        recyclerView.setLayoutManager(layoutManager);
        StoreEmployeesRecyclerViewAdapter adapter = new StoreEmployeesRecyclerViewAdapter(getContext(), storeEmployees, nonStoreEmployees, storeID);
        recyclerView.setAdapter(adapter);
    }
}