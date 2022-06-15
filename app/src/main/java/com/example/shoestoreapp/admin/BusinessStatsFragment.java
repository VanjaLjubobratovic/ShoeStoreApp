package com.example.shoestoreapp.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentBusinessStatsBinding;
import com.example.shoestoreapp.employee.DatePicker;
import com.example.shoestoreapp.employee.ReceiptListRecyclerViewAdapter;
import com.example.shoestoreapp.employee.ReceiptModel;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BusinessStatsFragment extends Fragment {
    private FragmentBusinessStatsBinding binding;
    private MaterialButton dateBtn;
    private Spinner storeSpinner, periodSpinner;
    private TextView totalTV;
    private BarChart dataChart;

    private FirebaseFirestore database;
    private ArrayList<String> storeList;
    private ArrayList<String> timeSpans;
    private ReceiptModel itemsMerged;

    private DatePicker datePicker;

    public BusinessStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();

        fetchStores();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBusinessStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateBtn = binding.statsDateButton;
        dataChart = binding.statsChart;
        storeSpinner = binding.statsStoreSpinner;
        periodSpinner = binding.statsTimeSpinner;
        totalTV = binding.statsTotal;

        dateBtn.setOnClickListener(view1 -> {
            datePicker = new DatePicker();
            datePicker.setObject(dateBtn);
            datePicker.show(getActivity().getSupportFragmentManager(), "DATE PICK");
        });

        dateBtn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fetchReceipts();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void fetchStores() {
        database = FirebaseFirestore.getInstance();
        storeList = new ArrayList<>();

        database.collection("/locations").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot document : task.getResult())
                            storeList.add(document.getId());

                        dropdownAddStores();
                        dropdownAddPeriods();
                    }
                });
    }

    private void dropdownAddStores() {
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, storeList);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storeSpinner.setAdapter(dropdownAdapter);
    }

    private void dropdownAddPeriods() {
        timeSpans = new ArrayList<>();
        timeSpans.add("dan");
        timeSpans.add("tjedan");
        timeSpans.add("mjesec");
        timeSpans.add("godina");

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, timeSpans);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(dropdownAdapter);
    }

    private void fetchReceipts() {
        database = FirebaseFirestore.getInstance();
        itemsMerged = new ReceiptModel();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(dateBtn.getText().toString()));
            c.add(Calendar.DATE, 1);
            endDate = c.getTime();

            c.add(Calendar.DATE, parsePeriod());
            startDate = c.getTime();

            Log.d("DATE", startDate.toString() + " " + endDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        database.collection("/receipts").whereEqualTo("storeID", storeSpinner.getSelectedItem()).orderBy("time")
                .startAt(startDate).endAt(endDate).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot document : task.getResult()) {
                            ReceiptModel receipt = document.toObject(ReceiptModel.class);
                            if (receipt == null)
                                return;

                            receipt.setReceiptID(document.getId());
                            fetchItems(document.getId(), receipt);
                        }
                        initRecyclerView();
                    }
                })
                .addOnFailureListener(task ->{
                    Toast.makeText(getContext(), "Neuspješno dobavljanje", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchItems(String documentID, ReceiptModel receipt) {
        ArrayList<ItemModel> items = new ArrayList<>();
        database.collection("receipts").document(documentID).collection("items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("fetchItems query", "onComplete: ");

                        for(DocumentSnapshot document : task.getResult()) {
                            ItemModel item = document.toObject(ItemModel.class);
                            if(item == null)
                                continue;

                            item.parseModelColor(document.getId());
                            itemsMerged.addItem(item);
                        }
                        itemsMerged.packItems();
                        initRecyclerView();
                    } else {
                        Log.d("fetchItems query", "onFailure: ");
                    }
                });
    }

    private void initRecyclerView() {
        Log.d("RECYCLER VIEW", itemsMerged.getItems().toString());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = binding.statsItemsRecycler;
        recyclerView.setLayoutManager(layoutManager);
        BusinessStatsRecyclerAdapter adapter = new BusinessStatsRecyclerAdapter(getContext(), itemsMerged.getItems());
        recyclerView.setAdapter(adapter);

        totalTV.setText("Ukupni promet: " + itemsMerged.getTotal() + "kn");
    }


    private int parsePeriod() {
        if(periodSpinner.getSelectedItem().equals("dan"))
            return -1;
        if(periodSpinner.getSelectedItem().equals("tjedan"))
            return -7;
        if(periodSpinner.getSelectedItem().equals("mjesec"))
            return -30;
        else return -365;
    }
}