package com.example.shoestoreapp.employee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.databinding.ActivityEmployeeMainBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class EmployeeMainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ActivityEmployeeMainBinding binding;

    private BarChart barChart;
    private MaterialButton newReceiptBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        newReceiptBtn = binding.newSale;

        user = getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        //TODO: rest of the code here
        checkUser();


        //TODO: Replace with real cashflow calculation
        barChart = binding.salesChart;
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

            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(EmployeeMainActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
            String toast = "Hello " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nRole: " + user.getRole();
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }
    }
}