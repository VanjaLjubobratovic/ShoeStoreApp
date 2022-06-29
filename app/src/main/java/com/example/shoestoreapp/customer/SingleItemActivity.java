package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.DataModels.ReviewModel;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.DataModels.ReceiptModel;
import com.example.shoestoreapp.customer.adapters.ReviewsRecycleViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


public class SingleItemActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private FirebaseFirestore database;
    private ItemModel selectedItem;
    private TextView itemName, itemPrice;
    private ImageView itemImage;
    private RatingBar itemRating;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ArrayList<String> curColors = new ArrayList<>();
    private ArrayList<ReviewModel> reviews = new ArrayList<>();
    private ImageButton backButton;
    private CollectionReference itemsRef, reviewsRef;
    private ArrayList<ItemModel> items = new ArrayList<>();
    private MaterialButton buyButton;
    private ArrayList<Integer> sizes = new ArrayList<>(), amounts = new ArrayList<>();
    private Spinner colorSpinner, sizeSpinner;
    private boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item);

        //getting the passed data(user and selected item)
        user = getIntent().getParcelableExtra("userData");
        selectedItem = getIntent().getParcelableExtra("selectedItem");


        //Initializing the views and database
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        itemsRef = database.collection("/locations/webshop/items");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        itemPrice = findViewById(R.id.itemPriceLabel);
        itemRating = findViewById(R.id.itemRatingBar);
        itemImage = findViewById(R.id.itemImageView);
        colorSpinner = findViewById(R.id.colorSpinner);
        sizeSpinner = findViewById(R.id.sizeSpinner);
        itemName = findViewById(R.id.itemNameTextView);

        checkUser();
        fetchItems();

        //filling the views with data
        fillElements();

        //Changing the selected item on new color selection
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(isInit) {
                    String newColor = colorSpinner.getSelectedItem().toString();
                    for (ItemModel item : items) {
                        if (selectedItem.getModel().equals(item.getModel()) && newColor.equals(item.getColor())) {
                            selectedItem = item;
                            fillElements();
                            break;
                        }
                    }
                }
                else{
                    colorSpinner.setSelection(curColors.indexOf(selectedItem.getColor()));
                    isInit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Initializing Shared Preferences to read and write shopping cart items
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();

        /*
        editor.remove("ShoppingCartReceipt");
        editor.apply();*/

        //Add to cart button onclick
        buyButton = findViewById(R.id.buyItemButton);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Converting item sizes to work with ReceiptModel items
                //TODO make this a method in ReceiptModel
                Integer sizeIndex = selectedItem.getSizes().indexOf(Integer.parseInt(sizeSpinner.getSelectedItem().toString()));

                ArrayList<Integer> sizeList = new ArrayList<>(Collections.nCopies(selectedItem.getSizes().size(), 0));
                sizeList.set(sizeIndex, 1);

                ItemModel receiptItem = new ItemModel(selectedItem.getType(), selectedItem.getImage(), selectedItem.getPrice(),
                        selectedItem.getRating(), selectedItem.getAdded(), selectedItem.getSizes(), sizeList);
                receiptItem.parseModelColor(selectedItem.toString());
                ReceiptModel receipt;

                //Reading receipt written in Shared Preferences using gson
                if(sharedPref.contains("ShoppingCartReceipt")){
                    Gson gson = new Gson();
                    String json = sharedPref.getString("ShoppingCartReceipt","NoItems");
                    receipt = gson.fromJson(json, ReceiptModel.class);
                }
                //Creating a new receipt if there isn't one already in Shared Preferences
                else{
                    receipt = new ReceiptModel();
                }
                //Adding the selected item to cart and then Shared Preferences using gson
                receipt.addItem(receiptItem);
                Gson receiptGson = new Gson();
                String receiptJson = receiptGson.toJson(receipt);
                editor.putString("ShoppingCartReceipt",receiptJson);
                editor.apply();
                Toast.makeText(SingleItemActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();

            }
        });

        //back button finish onClick
        backButton = findViewById(R.id.itemBackImageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    //Method to check if user is logged into a account
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(SingleItemActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
        }
    }

    //Fetching the list of items stored in the Firebase
    private void fetchItems() {
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    //Writing the results to a ArrayList
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ItemModel newItem = document.toObject(ItemModel.class);
                        newItem.parseModelColor(document.getId());
                        items.add(newItem);
                        Log.d("FIRESTORE Single", newItem.toString());
                    }
                    //Initialization of spinners and Review Recycler
                    initColorSpinner();
                    initSizeSpinner();
                    //fetchReviews();
                } else Log.d("FIRESTORE Single", "fetch failed");
            }
        });
    }

    //Getting reviews from firebase
    private void fetchReviews() {
        reviews.clear();
        reviewsRef = database.collection("locations/webshop/items/" + selectedItem +"/reviews");
        reviewsRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(task.getResult().size() == 0) {
                    Log.d("FIRESTORE", "0 Results");
                }
                //Writing the results to a ArrayList
                for(QueryDocumentSnapshot document : task.getResult()) {
                    ReviewModel newReview = document.toObject(ReviewModel.class);
                    reviews.add(newReview);
                    Log.d("FIRESTORE-SINGLE", newReview.toString());
                }
                initReviewRecycler();
            } else Log.d("FIRESTORE Single", "fetch failed");
        });
    }


    private void initColorSpinner(){
        String curModel = selectedItem.getModel();
        //Reading all the colors that need to be added
        for(ItemModel item : items){
            if (item.getModel().equals(curModel)){
                curColors.add(item.getColor());
            }
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, curColors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        colorSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void initSizeSpinner(){
        sizes = selectedItem.getSizes();
        //Reading all the sizes that need to be added
        ArrayList<String> sizesString = new ArrayList<>();
        for (Integer size : sizes){
            sizesString.add(size.toString());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, sizesString);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sizeSpinner.setAdapter(spinnerArrayAdapter);
    }

    //Method to fill all the elements with the selected item
    private void fillElements(){
        itemName.setText("Model " + selectedItem.toString());
        itemPrice.setText(String.valueOf(selectedItem.getPrice()) + " Kn");
        itemRating.setRating((float) selectedItem.getRating());

        StorageReference imageReference = storageRef.child(selectedItem.getImage());
        Glide.with(this)
                .asBitmap()
                .load(imageReference)
                .into(itemImage);


        fetchReviews();
    }


    //Review recycler view initialization
    private void initReviewRecycler(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        RecyclerView reviewRecyclerView = findViewById(R.id.itemReviewsRecyclerView);
        reviewRecyclerView.setLayoutManager(layoutManager);
        ReviewsRecycleViewAdapter adapter = new ReviewsRecycleViewAdapter(this, reviews);
        reviewRecyclerView.setAdapter(adapter);
    }
}