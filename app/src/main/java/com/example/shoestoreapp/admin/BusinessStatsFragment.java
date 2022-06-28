package com.example.shoestoreapp.admin;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class BusinessStatsFragment extends Fragment {
    private FragmentBusinessStatsBinding binding;
    private MaterialButton dateBtn;
    private Spinner storeSpinner, periodSpinner;
    private TextView totalTV;
    private BarChart dataChart;

    private FirebaseFirestore database;
    private ArrayList<String> storeList;
    private ArrayList<String> timeSpans;
    private ArrayList<ReceiptModel> receiptsList;
    private ReceiptModel itemsMerged;
    private HashMap<Date, Double> trafficMap;

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

            @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchReceipts() {
        database = FirebaseFirestore.getInstance();
        itemsMerged = new ReceiptModel();
        receiptsList = new ArrayList<>();

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

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        database.collection("/receipts").whereEqualTo("storeID", storeSpinner.getSelectedItem()).orderBy("time")
                .startAt(startDate).endAt(endDate).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot document : task.getResult()) {
                            ReceiptModel receipt = document.toObject(ReceiptModel.class);
                            if (receipt == null || receipt.isAnnulled())
                                continue;

                            receipt.setReceiptID(document.getId());
                            fetchItems(document.getId(), receipt);
                            receiptsList.add(receipt);
                        }
                        initRecyclerView();
                        initChart(finalStartDate, finalEndDate);
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

        //totalTV.setText("Ukupni promet: " + itemsMerged.getTotal() + "kn");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initChart(Date startDate, Date endDate) {
        int period = -parsePeriod();
        ArrayList<Date> keys = new ArrayList<>();
        if(period == 1)
            sortByHour();
        else if(period == 7 || period == 30)
            sortByDay(period, startDate, endDate);
        else sortByMonth(startDate, endDate);
    }

    private void sortByHour() {
        ArrayList<Integer> hours = new ArrayList<>();
        for(int i = 0; i < 24; i++)
            hours.add(i);
        ArrayList<Double> totals = new ArrayList<>(Collections.nCopies(hours.size(), 0.0));

        for (ReceiptModel receipt : receiptsList) {
            Date receiptTime = new Date(receipt.getTime().getSeconds() * 1000);
            int hour = receiptTime.getHours();
            int index = hours.indexOf(hour);

            Log.d("HOURS", "receipt time: " + receiptTime);
            Log.d("HOURS", "hour: " + hour);
            totals.set(index, totals.get(index) + receipt.getTotal());
        }

        Log.d("HOURS", hours.toString());
        Log.d("HOURS", totals.toString());
        setGraph(hours, totals);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sortByDay(int steps, Date startDate, Date endDate) {
        ArrayList<Integer> days = new ArrayList<>();
        ArrayList<Double> totals = new ArrayList<>(Collections.nCopies(steps, 0.0));
        LocalDate startLocal = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocal = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for (LocalDate date = startLocal; date.isBefore(endLocal); date = date.plusDays(1)) {
            days.add(date.getDayOfMonth());
        }

        for(ReceiptModel receipt : receiptsList) {
            LocalDate receiptTime = Instant.ofEpochSecond(receipt.getTime().getSeconds()).atZone(ZoneId.systemDefault()).toLocalDate();
            int day = receiptTime.getDayOfMonth();
            Log.d("DAYS", "receipt date: " + receiptTime);
            Log.d("DAYS", "receipt day: " + day);
            int index = days.indexOf(day);

            if(receiptTime.getMonthValue() == endLocal.getMonthValue())
                index = days.lastIndexOf(day);

            totals.set(index, totals.get(index) + receipt.getTotal());
        }

        Log.d("DAYS", days.toString());
        Log.d("DAYS", totals.toString());

        setGraph(days, totals);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sortByMonth(Date startDate, Date endDate) {
        ArrayList<Integer> months = new ArrayList<>();
        LocalDate startLocal = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocal = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for (LocalDate date = startLocal; date.isBefore(endLocal); date = date.plusMonths(1)) {
            months.add(date.getMonthValue());
        }

        ArrayList<Double> totals = new ArrayList<>(Collections.nCopies(months.size(), 0.0));

        for(ReceiptModel receipt : receiptsList) {
            LocalDate receiptTime = receipt.getTime().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int month = receiptTime.getMonthValue();

            Log.d("MONTH", "Receipt: " + receipt.getTime().toDate() + " MonthConvert: " + month);
            int index = months.indexOf(month);

            if(receiptTime.getYear() == endLocal.getYear())
                index = months.lastIndexOf(month);

            totals.set(index, totals.get(index) + receipt.getTotal());
        }

        setGraph(months, totals);
    }

    private void setGraph(ArrayList<Integer> xAxisVals, ArrayList<Double> yAxisVals) {
        ArrayList<BarEntry> saleSumList = new ArrayList<>();
        double total = 0;

        for(double i : yAxisVals)
            total += i;
        totalTV.setText("Ukupni promet: " + total + "kn");

        for(int i = 0; i < xAxisVals.size(); i++)
            saleSumList.add(new BarEntry(i, yAxisVals.get(i).intValue()));

        BarDataSet barDataSet = new BarDataSet(saleSumList, "Promet");
        barDataSet.setColor(R.color.purple_500);

        Log.d("SET_GRAPH", xAxisVals.toString());
        Log.d("SET_GRAPH", yAxisVals.toString());
        BarData barData = new BarData(barDataSet);
        dataChart.setData(barData);

        dataChart.setTouchEnabled(true);
        dataChart.setDragEnabled(false);
        dataChart.getXAxis().setDrawGridLines(false);
        dataChart.getAxisRight().setDrawGridLines(false);
        dataChart.getAxisLeft().setDrawGridLines(false);
        XAxis xAxis = dataChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);;

        xAxis.setValueFormatter(new IndexAxisValueFormatter(getDate(xAxisVals)));

        dataChart.invalidate();
        dataChart.refreshDrawableState();
    }

    private ArrayList<String> getDate(ArrayList<Integer> xAxisVals) {
        ArrayList<String> labels = new ArrayList<>();
        for (Integer i : xAxisVals)
            labels.add(i.toString());

        return labels;
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