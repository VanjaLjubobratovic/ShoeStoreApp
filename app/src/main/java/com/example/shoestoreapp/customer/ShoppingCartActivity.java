package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.employee.ReceiptModel;
import com.example.shoestoreapp.employee.ReceiptRecyclerViewAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ShoppingCartActivity extends AppCompatActivity {

    private UserModel user;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ItemModel> itemsToRemove = new ArrayList<>();
    private ImageButton backButton;
    private ReceiptModel receipt;
    private TextView totalPrice;
    private MaterialButton proceedToCheck;
    private ConstraintLayout emptyCartLayout, notEmptyLayout;
    private Spinner deliverySpinner;
    private RecyclerView shoppingCartRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);


        user = getIntent().getParcelableExtra("userData");

        //Init of all the views
        shoppingCartRecyclerView = findViewById(R.id.shoppingCartRecyclerView);
        totalPrice = findViewById(R.id.textViewTotalPrice);
        proceedToCheck = findViewById(R.id.buttonProceedToCheck);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        notEmptyLayout = findViewById(R.id.notEmptyCartLayout);

        //Adding the delivery price to the total price after a item gets removed

        backButton = findViewById(R.id.shoppingCartBackImageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initCartRecycler();

        proceedToCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent purchaseActivity = new Intent(getApplicationContext(), CustomerPurchaseActivity.class);
                purchaseActivity.putExtra("userData", user);
                startActivity(purchaseActivity);
            }
        });

    }

    //Initialization of the Item Recycler View
    public void initCartRecycler(){
        readData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        shoppingCartRecyclerView.setLayoutManager(layoutManager);
        ReceiptRecyclerViewAdapter adapter = new ReceiptRecyclerViewAdapter(this, receipt, totalPrice, proceedToCheck, itemsToRemove);
        shoppingCartRecyclerView.setAdapter(adapter);
        shoppingCartRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            //Checking if there are any items left in the cart
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                Toast.makeText(ShoppingCartActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                //If not show empty cart message
                if(receipt.getItems().size() <= 0) {
                    notEmptyLayout.setVisibility(View.GONE);
                    emptyCartLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //Reading the cart items from sharedPreferences
    public void readData(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        if(sharedPref.contains("ShoppingCartReceipt")){
            Gson gson = new Gson();
            String json = sharedPref.getString("ShoppingCartReceipt","NoItems");
            receipt = gson.fromJson(json, ReceiptModel.class);

        }
        //If there are none show empty cart message
        else{
            emptyCartLayout.setVisibility(View.VISIBLE);
            notEmptyLayout.setVisibility(View.GONE);
            receipt = new ReceiptModel();
        }

    }

    //Initialization of the Delivery spinner

    @Override
    protected void onResume() {
        readData();
        shoppingCartRecyclerView.getAdapter().notifyDataSetChanged();

        super.onResume();
    }
}