package com.example.shoestoreapp.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class CustomerOrderHistoryActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ImageButton exit;
    private ArrayList<TestOrderModel> fillerOrders = new ArrayList<>();
    private ArrayList<ItemModel> orderItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_history);

        user=getIntent().getParcelableExtra("userData");
        firebaseAuth = firebaseAuth.getInstance();

        checkUser();

        exit = findViewById(R.id.orderBackImageButton);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initOrderRecycler();


    }


    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(CustomerOrderHistoryActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
        }
    }

    private void initDummyData(){
        ArrayList<Integer> sizes = new ArrayList<>(), amounts = new ArrayList<>();
        sizes.add(39);
        sizes.add(40);
        sizes.add(41);
        amounts.add(2);
        amounts.add(1);
        amounts.add(5);
        ItemModel fillerItem = new ItemModel("shoe","imageLink",200.0,4.0, Timestamp.now(),sizes, amounts);
        fillerItem.parseModelColor("501-crna");
        orderItems.add(fillerItem);
        orderItems.add(fillerItem);
        TestOrderModel fillerOrder = new TestOrderModel("Vanja Ljubobratovic","12.17.2028","200.2", orderItems);
        fillerOrders.add(fillerOrder);
        fillerOrders.add(fillerOrder);

    }

    private void initOrderRecycler(){
        initDummyData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        RecyclerView orderRecyclerView = findViewById(R.id.userOrdersRecyclerView);
        orderRecyclerView.setLayoutManager(layoutManager);
        UserOrdersRecyclerViewAdapter adapter = new UserOrdersRecyclerViewAdapter(this, fillerOrders);
        orderRecyclerView.setAdapter(adapter);
    }
}