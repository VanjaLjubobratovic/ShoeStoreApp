package com.example.shoestoreapp.employee;

import android.content.Intent;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class EmployeeMainFragment extends Fragment {

    private BarChart barChart;
    private MaterialButton newReceiptBtn, salesListBtn, deliveryBtn, inventoryBtn;

    private UserModel user;

    public EmployeeMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newReceiptBtn = view.findViewById(R.id.newSale);
        salesListBtn = view.findViewById(R.id.salesList);
        deliveryBtn = view.findViewById(R.id.acceptDelivery);
        inventoryBtn = view.findViewById(R.id.searchInventory);

        Intent intent = getActivity().getIntent();
        user = (UserModel) intent.getParcelableExtra("userData");

        //TODO: Replace with real cashflow calculation
        barChart = view.findViewById(R.id.salesChart);
        ArrayList<BarEntry> saleSumList = new ArrayList<>();
        saleSumList.add(new BarEntry(9f,1500));
        saleSumList.add(new BarEntry(10f,1000));
        saleSumList.add(new BarEntry(11f,350));
        saleSumList.add(new BarEntry(12f,200));
        saleSumList.add(new BarEntry(13f,2500));
        saleSumList.add(new BarEntry(14f,4500));
        saleSumList.add(new BarEntry(15f,500));
        BarDataSet barDataSet = new BarDataSet(saleSumList, "Promet");
        barDataSet.setColor(R.color.purple_500);

        BarData theData = new BarData(barDataSet);

        barChart.setData(theData);

        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);


        newReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);

                fragmentTransaction.replace(R.id.employeeActivityLayout, ReceiptFragment.class, null);
                fragmentTransaction.addToBackStack("name").commit();
            }
        });

        salesListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);

                fragmentTransaction.replace(R.id.employeeActivityLayout, SalesListFragment.class, null);
                fragmentTransaction.addToBackStack("name").commit();
            }
        });

        deliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);

                fragmentTransaction.replace(R.id.employeeActivityLayout, DeliveryFragment.class, null);
                fragmentTransaction.addToBackStack("name").commit();
            }
        });

        inventoryBtn.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            fragmentTransaction.replace(R.id.employeeActivityLayout, InventoryFragment.class, null);
            fragmentTransaction.addToBackStack("name").commit();
        });
    }
}