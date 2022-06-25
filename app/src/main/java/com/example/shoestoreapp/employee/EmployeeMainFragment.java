package com.example.shoestoreapp.employee;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class EmployeeMainFragment extends Fragment {

    private BarChart barChart;

    private MaterialButton newReceiptBtn, salesListBtn, deliveryBtn, inventoryBtn, orderBtn;

    private UserModel user;

    private CollectionReference receiptsRef;
    private FirebaseFirestore database;

    private HashMap<Integer, Double> trafficMap;
    private String storeID;
    private String storeID2;
    private final Integer START_HOUR = 8;
    private final Integer CLOSING_HOUR = 23;

    public EmployeeMainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
        receiptsRef = database.collection("/receipts");

        //TODO: exception management
        user = getActivity().getIntent().getParcelableExtra("userData");
        storeID = getActivity().getIntent().getStringExtra("storeID");
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
        orderBtn = view.findViewById(R.id.rezervacije);
        inventoryBtn = view.findViewById(R.id.searchInventory);

        Intent intent = getActivity().getIntent();
        user = (UserModel) intent.getParcelableExtra("userData");

        barChart = view.findViewById(R.id.salesChart);

        //fetchTraffic();


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


        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);

                fragmentTransaction.replace(R.id.employeeActivityLayout, OrdersFragment.class, null);
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

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        fetchTraffic();
    }

    private void setGraph() {
        ArrayList<BarEntry> saleSumList = new ArrayList<>();
        //creating X axis values by hours and populating y axis for each hour
        for(Integer i = START_HOUR; i <= CLOSING_HOUR; i++) {
            int totalPerH = 0;
            if(trafficMap.containsKey(i)) {
                try {
                    totalPerH = trafficMap.get(i).intValue();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            saleSumList.add(new BarEntry(i, totalPerH));
        }

        BarDataSet barDataSet = new BarDataSet(saleSumList, "Promet");
        barDataSet.setColor(R.color.purple_500);

        Log.d("SET_GRAPH", trafficMap.keySet().toString());
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);

        barChart.invalidate();
        barChart.refreshDrawableState();
    }

    private void fetchTraffic() {
        //Creating receipt search period of one day
        trafficMap = new HashMap<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date startDate = c.getTime();
        c.add(Calendar.DATE, 1);
        Date endDate = c.getTime();

        receiptsRef.whereEqualTo("storeID", storeID).orderBy("time").startAt(startDate).endAt(endDate).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot document : task.getResult()) {
                                ReceiptModel receipt = document.toObject(ReceiptModel.class);
                                if(receipt == null)
                                    return;

                                //sorting receipts by hour
                                Date receiptTime = new Date(receipt.getTime().getSeconds() * 1000);
                                int hour = receiptTime.getHours();

                                if(!trafficMap.containsKey(hour))
                                    trafficMap.put(hour, receipt.getTotal());
                                else trafficMap.merge(hour, (Double)receipt.getTotal(), Double::sum);
                            }
                        }

                        Log.d("FETCH_TRAFFIC", "Success");
                        setGraph();
                    }
                });
    }
}