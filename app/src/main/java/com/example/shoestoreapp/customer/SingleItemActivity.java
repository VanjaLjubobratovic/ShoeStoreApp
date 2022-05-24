package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
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

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
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

import java.io.Serializable;
import java.util.ArrayList;


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
    private ArrayList<String> mNames = new ArrayList<>(), mReviews = new ArrayList<>(),
            mRatings = new ArrayList<>(), curColors = new ArrayList<>();
    private ImageButton backButton;
    private CollectionReference itemsRef;
    private ArrayList<ItemModel> items = new ArrayList<>();
    private MaterialButton buyButton;
    private ArrayList<Integer> sizes = new ArrayList<>(), amounts = new ArrayList<>();
    private Spinner colorSpinner, sizeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item);

        //getting the passed data(user and selected item)
        user = getIntent().getParcelableExtra("userData");
        selectedItem = getIntent().getParcelableExtra("selectedItem");


        //Initializing the views and database
        firebaseAuth = firebaseAuth.getInstance();
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


        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String newColor = colorSpinner.getSelectedItem().toString();
                for(ItemModel item : items){
                    if(selectedItem.getModel().equals(item.getModel()) && newColor.equals(item.getColor())){
                        selectedItem = item;
                        fillElements();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buyButton = findViewById(R.id.buyItemButton);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

        //filling the recycler view with data
        initDummyData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        RecyclerView reviewRecyclerView = findViewById(R.id.itemReviewsRecyclerView);
        reviewRecyclerView.setLayoutManager(layoutManager);
        ReviewsRecycleViewAdapter adapter = new ReviewsRecycleViewAdapter(this,mNames,mReviews,mRatings);
        reviewRecyclerView.setAdapter(adapter);
    }

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
    private void fetchItems() {
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ItemModel newItem = document.toObject(ItemModel.class);
                        newItem.parseModelColor(document.getId());
                        items.add(newItem);
                        Log.d("FIRESTORE Single", newItem.toString());
                    }
                    initColorSpinner();
                    initSizeSpinner();
                } else Log.d("FIRESTORE Single", "fetch failed");
            }
        });
    }

    private void initColorSpinner(){
        String curModel = selectedItem.getModel();
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
        ArrayList<String> sizesString = new ArrayList<>();
        for (Integer size : sizes){
            sizesString.add(size.toString());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, sizesString);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sizeSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void fillElements(){

        itemName.setText("Model " + selectedItem.toString());
        itemPrice.setText(String.valueOf(selectedItem.getPrice()) + " Kn");
        itemRating.setRating((float) selectedItem.getRating());

        StorageReference imageReference = storageRef.child(selectedItem.getImage());
        Glide.with(this)
                .asBitmap()
                .load(imageReference)
                .into(itemImage);

    }

    private void initDummyData(){
        mNames.add("Vanja peder");
        mRatings.add("4.5");
        mReviews.add("Shit slaps ass");

        mNames.add("Karlo Katalinic");
        mRatings.add("4.2");
        mReviews.add("Ja sam peder najveci");
    }
}