package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.databinding.ActivityCustomerMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class CustomerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecyclerViewAdapter.OnItemListener{
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ActivityCustomerMainBinding binding;
    private DrawerLayout drawer;
    //TODO: replace this list with objects
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<Float> ratings = new ArrayList<>();

    private final static String TAG = "CustomerMainActivity";
    private ImageButton shoppingCart;
    private ImageView bag, shoe;
    private TextView test;

    private ArrayList<ItemModel> items = new ArrayList<>();
    private FirebaseFirestore database;
    private CollectionReference itemsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseFirestore.getInstance();
        itemsRef = database.collection("/locations/webshop/items");

        checkUser();

        //Navigation drawer
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //fetch inventory data from db
        fetchItems();

        shoppingCart = findViewById(R.id.imageButtonCart);
        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO shopping cart onclick
            }
        });

        //Category ImageViews OnClick
        shoe = findViewById(R.id.imageViewShoe);
        bag = findViewById(R.id.imageViewBag);
        shoe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getSupportFragmentManager().beginTransaction().replace(R.id.drawer_layout, ItemModelsFragment.newInstance("shoe",items, user)).addToBackStack(null).commit();
            }
        });
        bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getSupportFragmentManager().beginTransaction().replace(R.id.drawer_layout, ItemModelsFragment.newInstance("bag",items, user)).addToBackStack(null).commit();
            }
        });
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
                        Log.d("FIRESTORE", newItem.toString());
                    }

                    initRecyclerViewPopular();
                    initRecyclerViewRecent();
                } else Log.d("FIRESTORE", "fetch failed");
            }
        });
    }

    //Drawer onclick
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_my_profile:
                //TODO my profile on click
                //Launching profile activity
                Intent profileIntent = new Intent(this, CustomerProfileActivity.class);
                profileIntent.putExtra("userData", user);
                startActivity(profileIntent);

                break;
            case R.id.nav_oder_history:
                //TODO order history on click
                break;
            case R.id.nav_payment_method:
                //TODO payment method on click
                break;
            case R.id.nav_logout:
                //Logging out user and launching the login activity
                firebaseAuth.signOut();
                SharedPreferences sharedPreferences = CustomerMainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userData");
                editor.apply();
                startActivity(new Intent(CustomerMainActivity.this, LoginActivity.class));
                finish();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(CustomerMainActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
            String toast = "Hello " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nRole: " + user.getRole();
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }
    }


    private void initRecyclerViewPopular(){
        Log.d(TAG, "initRecyclerView");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPopularProducts);
        recyclerView.setLayoutManager(layoutManager);
        //TODO: pass filtered array
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, items, this);
        recyclerView.setAdapter(adapter);
    }

    private void initRecyclerViewRecent(){
        Log.d(TAG, "initRecyclerView");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewRecentProducts);
        recyclerView.setLayoutManager(layoutManager);
        //TODO: pass filtered array
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, items, this);
        recyclerView.setAdapter(adapter);
    }

    //starting new activity and sending item selected
    @Override
    public void onItemClick(int position, String id) {

        Intent profileIntent = new Intent(this, SingleItemActivity.class);
        profileIntent.putExtra("selectedItem", items.get(position));
        profileIntent.putExtra("userData", user);
        startActivity(profileIntent);
    }
}