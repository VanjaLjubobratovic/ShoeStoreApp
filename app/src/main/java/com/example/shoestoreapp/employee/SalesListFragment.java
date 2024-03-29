package com.example.shoestoreapp.employee;

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
import android.widget.ProgressBar;

import com.example.shoestoreapp.DataModels.ReceiptModel;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.employee.adapters.ReceiptListRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class SalesListFragment extends Fragment implements ReceiptListRecyclerViewAdapter.OnReceiptListener {
    private MaterialButton dateEt;
    private ProgressBar progressBar;


    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference receiptsRef;
    private StorageReference storageRef;
    private String storeID;

    private UserModel user;
    private ArrayList<ReceiptModel> receiptList;
    private DatePicker datePicker;


    public SalesListFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
        receiptsRef = database.collection("/receipts");

        receiptList = new ArrayList<>();

        user = getActivity().getIntent().getParcelableExtra("userData");
        //TODO:exception management
        storeID = getActivity().getIntent().getStringExtra("storeID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateEt = view.findViewById(R.id.dateEt);
        progressBar = view.findViewById(R.id.receiptListProgressBar);

        progressBar.setVisibility(View.INVISIBLE);

        dateEt.setOnClickListener(view1 -> {
            datePicker = new DatePicker();
            datePicker.setObject(dateEt);
            datePicker.show(getActivity().getSupportFragmentManager(), "DATE PICK");
        });

        dateEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                progressBar.setVisibility(View.VISIBLE);
                receiptList = new ArrayList<>();
                initRecyclerView();
                fetchReceipts();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void fetchReceipts() {
        //Query receiptsQuery = receiptsRef.whereEqualTo("storeID", storeID).orderBy("time").startAt(dateEt.getText());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = sdf.parse(dateEt.getText().toString());
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.DATE, 1);
            endDate = c.getTime();

            Log.d("DATE", "startDate " + startDate.toString() + "endDate " + endDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }


        receiptsRef.whereEqualTo("storeID", storeID).orderBy("time").startAt(startDate).endAt(endDate).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d("fetchReceipts query", "onComplete: ");
                    for (DocumentSnapshot document : task.getResult()) {
                        ReceiptModel receipt = document.toObject(ReceiptModel.class);
                        if(receipt == null)
                            continue;

                        //receipt.setTotal(0);
                        receipt.setReceiptID(document.getId());
                        fetchItems(document.getId(), receipt);
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    Log.d("fetchReceipts query", "onFailure: ");
                }
            }
        });
    }

    private void fetchItems(String documentID, ReceiptModel receipt) {
        receiptsRef.document(documentID).collection("items").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("fetchItems query", "onComplete: ");

                            for(DocumentSnapshot document : task.getResult()) {
                                ItemModel item = document.toObject(ItemModel.class);
                                if(item == null)
                                    continue;

                                item.parseModelColor(document.getId());
                                receipt.addItemNoPrice(item);
                            }
                            receipt.unpackItems();
                            receiptList.add(receipt);
                            initRecyclerView();
                        } else {
                            Log.d("fetchItems query", "onFailure: ");
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerView() {
        Log.d("RECYCLER VIEW", "initRecyclerView: ");
        if(receiptList != null) {
            Comparator<ReceiptModel> timeSorter = Comparator.comparing(ReceiptModel::getTime, Comparator.reverseOrder());
            Collections.sort(receiptList, timeSorter);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = this.getView().findViewById(R.id.receiptListRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ReceiptListRecyclerViewAdapter adapter = new ReceiptListRecyclerViewAdapter(getContext(), receiptList, this, getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onReceiptClick(int position) {

    }
}