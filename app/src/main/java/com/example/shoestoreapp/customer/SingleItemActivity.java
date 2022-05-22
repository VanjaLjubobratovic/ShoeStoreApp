package com.example.shoestoreapp.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

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
            mRatings = new ArrayList<>();
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item);

        //getting the passed data(user and selected item)
        user = getIntent().getParcelableExtra("userData");
        selectedItem = getIntent().getParcelableExtra("selectedItem");

        firebaseAuth = firebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        checkUser();


        //filling the views with data
        itemName = findViewById(R.id.itemNameTextView);
        itemName.setText("Model " + selectedItem.toString());
        itemPrice = findViewById(R.id.itemPriceLabel);
        itemPrice.setText(String.valueOf(selectedItem.getPrice()) + " Kn");
        itemRating = findViewById(R.id.itemRatingBar);
        itemRating.setRating((float) selectedItem.getRating());
        itemImage = findViewById(R.id.itemImageView);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        StorageReference imageReference = storageRef.child(selectedItem.getImage());

        Glide.with(this)
                .asBitmap()
                .load(imageReference)
                .into(itemImage);

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

    private void initDummyData(){
        mNames.add("Vanja peder");
        mRatings.add("4.5");
        mReviews.add("Shit slaps ass");

        mNames.add("Karlo Katalinic");
        mRatings.add("4.2");
        mReviews.add("Ja sam peder najveci");
    }
}