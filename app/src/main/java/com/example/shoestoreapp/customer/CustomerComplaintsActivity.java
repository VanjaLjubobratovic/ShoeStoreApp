package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.DataModels.ComplaintModel;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.customer.adapters.CustomerComplaintsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class CustomerComplaintsActivity extends AppCompatActivity {

    FirebaseFirestore database;
    UserModel user;
    ImageButton back;
    ArrayList<ComplaintModel> complaintsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_complaints);
        database = FirebaseFirestore.getInstance();
        user = getIntent().getParcelableExtra("userData");
        fetchComplaints();

        back = findViewById(R.id.customerComplaintsBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }



    private void fetchComplaints() {
        CollectionReference  compRef = database.collection("complaints");
        compRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    //Writing the results to a ArrayList
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ComplaintModel newComplaint = document.toObject(ComplaintModel.class);
                        if(newComplaint.getEmail().equals(user.getEmail())){
                            complaintsList.add(newComplaint);
                            Log.d("FIRESTORE Single", newComplaint.toString());
                        }
                    }
                    initComplaintRecycler();
                } else Log.d("FIRESTORE Single", "fetch failed");
            }
        });
    }

    private void initComplaintRecycler(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        RecyclerView compRecyclerView = findViewById(R.id.customerComplaintsRecycler);
        compRecyclerView.setLayoutManager(layoutManager);
        CustomerComplaintsAdapter adapter = new CustomerComplaintsAdapter(this,complaintsList);
        compRecyclerView.setAdapter(adapter);
    }
}