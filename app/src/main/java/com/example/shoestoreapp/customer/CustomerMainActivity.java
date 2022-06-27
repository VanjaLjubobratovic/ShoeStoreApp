package com.example.shoestoreapp.customer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class CustomerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecyclerViewAdapter.OnItemListener{
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ActivityCustomerMainBinding binding;
    private DrawerLayout drawer;
    //TODO: replace this list with objects

    private final static String TAG = "CustomerMainActivity";
    private ImageButton shoppingCart;
    private ImageView bag, shoe, userProfilePic;
    private TextView userName, userEmail;

    private ArrayList<ItemModel> items = new ArrayList<>();
    private ArrayList<ItemModel> popularItems = new ArrayList<>();
    private ArrayList<ItemModel> recentItems = new ArrayList<>();
    private FirebaseFirestore database;
    private CollectionReference itemsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        user = getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseFirestore.getInstance();
        itemsRef = database.collection("/locations/webshop/items");
        usersRef = database.collection("users");
        fetchUser();
        checkUser();

        //Navigation drawer
        Toolbar toolbar = findViewById(R.id.customer_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDrawerStateChanged(int newState) {
                userName = findViewById(R.id.textViewUsername);
                userName.setText(user.getFullName());
                userEmail = findViewById(R.id.textViewEmail);
                userEmail.setText(user.getEmail());
                userProfilePic = findViewById(R.id.imageViewProfile);
                if(ContextCompat.checkSelfPermission(CustomerMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    if(user.getProfileImage() != null){
                        File file = new File(user.getProfileImage());
                        Uri imageUri = Uri.fromFile(file);
                        Glide.with(CustomerMainActivity.this).load(imageUri).into(userProfilePic);
                    }
                }
            }
        });

        Menu nav_Menu = navigationView.getMenu();
        if(user.getRole().equals("customer")) {
            nav_Menu.findItem(R.id.nav_leave_customer).setVisible(false);
        }

        //fetch inventory data from db
        //fetchItems();

        shoppingCart = findViewById(R.id.imageButtonCart);
        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO shopping cart onclick
                Intent shoppingCartIntent = new Intent(CustomerMainActivity.this, ShoppingCartActivity.class);
                shoppingCartIntent.putExtra("userData", user);
                startActivity(shoppingCartIntent);
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
            @RequiresApi(api = Build.VERSION_CODES.N)
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


    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent intent = result.getData();
            user = intent.getParcelableExtra("userResult");

        }
    });


    //Drawer onclick
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_my_profile:
                //Launching profile activity
                Intent profileIntent = new Intent(this, CustomerProfileActivity.class);
                profileIntent.putExtra("userData", user);
                activityLauncher.launch(profileIntent);
                break;
            
            case R.id.nav_store_locations:
                Intent orderHistoryIntent = new Intent(this, CustomerOrderHistoryActivity.class);
                orderHistoryIntent.putExtra("userData", user);
                orderHistoryIntent.putExtra("userReviews",user.getReviewedItems());
                startActivity(orderHistoryIntent);
                break;
            
            case R.id.nav_payment_method:
                //TODO payment method on click
                Intent shopsMap = new Intent(this, ShopsMapActivity.class);
                shopsMap.putExtra("userData", user);
                startActivity(shopsMap);
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

            case R.id.nav_complaints:
                Intent custComp = new Intent(this, CustomerComplaintsActivity.class);
                custComp.putExtra("userData", user);
                startActivity(custComp);
                break;

            case R.id.nav_leave_customer:
                finish();
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerViewPopular(){
        popularItems.clear();
        popularItems.addAll(items);
        Collections.sort(popularItems, Comparator.comparing(ItemModel::getRating, Comparator.reverseOrder()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPopularProducts);
        recyclerView.setLayoutManager(layoutManager);
        //TODO: pass filtered array
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, popularItems, this);
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerViewRecent(){
        recentItems.clear();
        recentItems.addAll(items);
        Collections.sort(recentItems, Comparator.comparing(ItemModel::getAdded, Comparator.reverseOrder()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewRecentProducts);
        recyclerView.setLayoutManager(layoutManager);
        //TODO: pass filtered array
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, recentItems, this);
        recyclerView.setAdapter(adapter);
    }

    //starting new activity and sending item selected
    @Override
    public void onItemClick(int position, String id) {
        Log.d("CUSTOMER-ITEM-CLICK", id);
        Intent profileIntent = new Intent(this, SingleItemActivity.class);

        if(id.equals("recyclerViewRecentProducts"))
            profileIntent.putExtra("selectedItem", recentItems.get(position));
        else profileIntent.putExtra("selectedItem", popularItems.get(position));
        profileIntent.putExtra("userData", user);
        startActivity(profileIntent);
    }

    public void fetchUser(){
        usersRef.whereEqualTo("email", user.getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                Log.d("FIRESTORE", "0 Results");
                                return;
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FIRESTORE", "Fetch succesful");
                                user = document.toObject(UserModel.class);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        items.clear();
        fetchItems();
    }
}