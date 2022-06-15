package com.example.shoestoreapp.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.databinding.FragmentAdminMainBinding;
import com.example.shoestoreapp.employee.ReceiptFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminMainFragment extends Fragment {
    private FragmentAdminMainBinding binding;
    private MaterialButton employeeBtn, storeBtn, itemsBtn, statsBtn, ordersBtn, complaintsBtn;

    private FirebaseFirestore database;
    private UserModel user;

    public AdminMainFragment() {
        // Required empty public constructor
    }
    public static AdminMainFragment newInstance(String param1, String param2) {
        AdminMainFragment fragment = new AdminMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
        user = (UserModel) getActivity().getIntent().getParcelableExtra("userData");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        employeeBtn = binding.employeeManagement;
        storeBtn = binding.storeManagement;
        itemsBtn = binding.itemManagement;
        statsBtn = binding.statistics;
        ordersBtn = binding.orderManagement;
        complaintsBtn = binding.complaints;

        employeeBtn.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            fragmentTransaction.replace(R.id.adminActivityLayout, EmployeeManagementFragment.class, null);
            fragmentTransaction.addToBackStack("name").commit();
        });

        storeBtn.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            fragmentTransaction.replace(R.id.adminActivityLayout, StoreManagementFragment.class, null);
            fragmentTransaction.addToBackStack("name").commit();
        });

        itemsBtn.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            fragmentTransaction.replace(R.id.adminActivityLayout, ItemManagementFragment.class, null);
            fragmentTransaction.addToBackStack("name").commit();
        });

        statsBtn.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            fragmentTransaction.replace(R.id.adminActivityLayout, BusinessStatsFragment.class, null);
            fragmentTransaction.addToBackStack("name").commit();
        });

        ordersBtn.setOnClickListener(view1 ->  {
            //TODO: order management
        });

        complaintsBtn.setOnClickListener(view1 -> {
            //TODO: complaints
        });
    }
}