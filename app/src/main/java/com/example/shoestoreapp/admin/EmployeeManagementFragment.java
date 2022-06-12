package com.example.shoestoreapp.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
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
            flipper.showNext();
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
                            adapter.notifyDataSetChanged();
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