package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.CustomerStoreModel;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.DataModels.OrderModel;
import com.example.shoestoreapp.DataModels.ReceiptModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CustomerPurchaseActivity extends AppCompatActivity {

    private UserModel user;
    private OrderModel order;
    private FirebaseFirestore database;
    private ArrayList<CustomerStoreModel> storesList = new ArrayList<>();
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private Spinner locationSpinner, deliverySpinner;
    private EditText deliveryPostalNumber, deliveryCity, deliveryAddress;
    private TextView totalPrice;
    private RadioGroup paymentMethods;
    private RadioButton pickupPay, googlePay;
    private ConstraintLayout googlePayLayout, checkAddressLayout, chooseStoreLayout;
    private MaterialButton confirmButton, cancelButton;
    private String storeId = "webshop";
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_purchase);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();
        database = FirebaseFirestore.getInstance();

        fetchLocations();

        user = getIntent().getParcelableExtra("userData");

        totalPrice = findViewById(R.id.purchaseTotalTextView);

        readItems();

        locationSpinner = findViewById(R.id.deliveryLocationSpinner);
        deliverySpinner = findViewById(R.id.purchaseDeliverySpinner);

        initDeliverySpinner();

        deliveryPostalNumber = findViewById(R.id.deliveryPostalEdit);
        deliveryCity = findViewById(R.id.deliveryCityEdit);
        deliveryAddress = findViewById(R.id.deliveryAddressEdit);


        deliveryCity.setText(user.getCity());
        deliveryAddress.setText(user.getAddress());
        deliveryPostalNumber.setText(user.getPostalNumber());

        googlePayLayout = findViewById(R.id.googlePayLayout);
        checkAddressLayout = findViewById(R.id.purchaseDeliveryLayout);
        chooseStoreLayout = findViewById(R.id.purchasePickupLayout);

        confirmButton = findViewById(R.id.paymentConfirmButton);
        cancelButton = findViewById(R.id.paymentCancelButton);

        paymentMethods = findViewById(R.id.radioGroup);

        pickupPay = findViewById(R.id.paymentPickupRadio);
        googlePay = findViewById(R.id.paymentGoogleRadio);

        paymentMethods.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = paymentMethods.findViewById(i);
                int index = paymentMethods.indexOfChild(radioButton);

                switch (index){
                    case 0:
                        googlePayLayout.setVisibility(View.GONE);
                        Log.d("Tag", "button1 pressed");
                        break;
                    case 1:
                        googlePayLayout.setVisibility(View.VISIBLE);
                        Log.d("Tag", "button2 pressed");
                        break;
                }
            }
        });


        deliverySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (deliverySpinner.getSelectedItemPosition()){
                    case 0:
                        checkAddressLayout.setVisibility(View.VISIBLE);
                        chooseStoreLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        checkAddressLayout.setVisibility(View.GONE);
                        chooseStoreLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO receipt and order writing to db, along with shared pref clear
                if(paymentMethods.getCheckedRadioButtonId() == -1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Morate odabrati način plaćanja");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
                else{
                    if(deliverySpinner.getSelectedItemPosition() == 1){
                        String[] tmpAddress = locationSpinner.getSelectedItem().toString().split(" ");
                        order.setDeliveryAddress(tmpAddress[0]);
                    }
                    else{
                        order.setDeliveryAddress(deliveryPostalNumber.getText().toString() +" "+ deliveryCity.getText().toString() +" "+ deliveryAddress.getText().toString());
                    }
                    addOrderToDB();
                    addReceiptToDB();

                    subscribeToOrderTopic(String.valueOf(order.getOrderCode()));

                    editor.remove("ShoppingCartReceipt");
                    editor.apply();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Uspješno obavljena kupovina");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    private void subscribeToOrderTopic(String orderCode) {
        FirebaseMessaging.getInstance().subscribeToTopic(orderCode + "-status")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Log.d("COMPLAINT-SUB", "subscribeToComplaintTopic: SUCCESS");
                    } else Log.d("COMPLAINT-SUB", "subscribeToComplaintTopic: FAILURE");
                });
    }


    public void readItems(){
        if(sharedPref.contains("ShoppingCartReceipt")){
            Gson gson = new Gson();
            String json = sharedPref.getString("ShoppingCartReceipt","NoItems");
            order = (OrderModel) gson.fromJson(json, OrderModel.class);
            Random rand = new Random();
            order.setOrderCode(Math.abs(rand.nextInt()));
            order.setTime();
            order.setDateCreated(Timestamp.now());
            order.setUser(user.getEmail());
            order.setEmployee("webshop");
            order.setStoreID("webshop");
            order.setInStore(false);
            order.setReviewEnabled(false);
            order.setPickedUp(false);
            Integer total = (int) order.getTotal();
            totalPrice.setText("Ukupno: " + total + "kn");
        }
    }

    private void fetchLocations() {
        CollectionReference locationsRef = database.collection("locations");
        locationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        CustomerStoreModel location = document.toObject(CustomerStoreModel.class);
                        if(location == null || location.getType().equals("webshop"))
                            continue;

                        location.setName(document.getId());
                        storesList.add(location);
                        Log.d("FIRESTORE Single", location.toString());
                    }
                } else Log.d("FIRESTORE Single", "fetch failed");
                initLocationSpinner();
            }
        });
    }

    public void initLocationSpinner(){
        ArrayList<String> storeNames = new ArrayList<>();
        for(CustomerStoreModel location : storesList){
            String address = location.getAddress().split("\\d{5}")[0];
            address.replaceAll(".$", "");
            storeNames.add(location.getName() + " " + address);
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, storeNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        locationSpinner.setAdapter(spinnerArrayAdapter);
    }

    public void initDeliverySpinner(){
        ArrayList<String> deliveryMethods = new ArrayList<>();
        deliveryMethods.add("Dostava na adresu");
        deliveryMethods.add("Samostalno preuzimanje");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, deliveryMethods);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        deliverySpinner.setAdapter(spinnerArrayAdapter);
    }

    private void addReceiptToDB() {
        ReceiptModel receipt = order;
        Map<String, Object> newReceipt = new HashMap<>();
        newReceipt.put("time", receipt.getTime());
        newReceipt.put("user", receipt.getUser());
        newReceipt.put("employee", receipt.getEmployee());
        newReceipt.put("storeID", receipt.getStoreID());
        newReceipt.put("total", receipt.getTotal());
        newReceipt.put("annulled", false);

        DocumentReference newReceiptRef = database.collection("receipts").document();

        newReceiptRef.set(newReceipt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addReceiptToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addReceiptToDB", "onFailure: ");
                    }
                });

        //TODO: make this ItemModelMethod
        for(ItemModel item : receipt.getItems()) {
            Map<String, Object> newReceiptItem = new HashMap<>();
            newReceiptItem.put("added", item.getAdded());
            newReceiptItem.put("image", item.getImage());
            newReceiptItem.put("price", item.getPrice());
            newReceiptItem.put("rating", item.getRating());
            newReceiptItem.put("type", item.getType());
            newReceiptItem.put("sizes", item.getSizes());
            newReceiptItem.put("amounts", item.getAmounts());

            newReceiptRef.collection("items").document(item.toString())
                    .set(newReceiptItem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("addItemToReceipt", "onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addItemToReceipt", "onFailure: ");
                        }
                    });
        }
    }

    private void addOrderToDB() {
        Map<String, Object> newOrder = new HashMap<>();
        newOrder.put("dateCreated", order.getDateCreated());
        newOrder.put("inStore", order.isInStore());
        newOrder.put("orderCode", order.getOrderCode());
        newOrder.put("pickedUp", order.isPickedUp());
        newOrder.put("reviewEnabled", order.isReviewEnabled());
        newOrder.put("user", order.getUser());
        newOrder.put("deliveryAddress", order.getDeliveryAddress());

        DocumentReference newOrderRef = database.collection("locations/" + storeId + "/orders").document();

        newOrderRef.set(newOrder)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addOrderToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addOrderToDB", "onFailure: ");
                    }
                });

        //TODO: make this ItemModelMethod
        for(ItemModel item : order.getItems()) {
            Map<String, Object> newReceiptItem = new HashMap<>();
            newReceiptItem.put("added", item.getAdded());
            newReceiptItem.put("image", item.getImage());
            newReceiptItem.put("price", item.getPrice());
            newReceiptItem.put("rating", item.getRating());
            newReceiptItem.put("type", item.getType());
            newReceiptItem.put("sizes", item.getSizes());
            newReceiptItem.put("amounts", item.getAmounts());

            newOrderRef.collection("items").document(item.toString())
                    .set(newReceiptItem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("addItemToOrder", "onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addItemToOrder", "onFailure: ");
                        }
                    });
        }
    }
}