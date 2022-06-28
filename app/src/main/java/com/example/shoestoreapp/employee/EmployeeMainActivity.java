package com.example.shoestoreapp.employee;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.admin.AdminMainActivity;
import com.example.shoestoreapp.customer.CustomerMainActivity;
import com.example.shoestoreapp.customer.CustomerProfileActivity;
import com.example.shoestoreapp.customer.ShopsMapActivity;
import com.example.shoestoreapp.databinding.ActivityEmployeeMainBinding;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class EmployeeMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ActivityEmployeeMainBinding binding;
    private DrawerLayout drawer;
    private String storeID;
    private FirebaseFirestore database;



    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == 88){
                Intent intent = result.getData();

                if(intent != null){
                    user = intent.getParcelableExtra("userResult");
                    drawer.close();
                }
            }
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();

        binding = ActivityEmployeeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            user = getIntent().getParcelableExtra("userData");
            storeID = getIntent().getStringExtra("storeID");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        firebaseAuth = FirebaseAuth.getInstance();

        //TODO: rest of the code here
        checkUser();

        getIntent().putExtra("userData", user);
        getIntent().putExtra("storeID", storeID);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.employeeActivityLayout, new EmployeeMainFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.employeeToolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.employeeDrawerLayout);
        NavigationView navigationView = findViewById(R.id.employeeNavView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_layout_open, R.string.drawer_layout_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Menu nav_Menu = navigationView.getMenu();
        if(user.getRole().equals("employee")) {
            nav_Menu.findItem(R.id.nav_leave_employee).setVisible(false);
            nav_Menu.findItem(R.id.nav_admin_employee).setVisible(false);

            subToDeliveries();
        }
        if(user.getRole().equals("admin")){
            nav_Menu.findItem(R.id.nav_admin_employee).setVisible(false);
        }

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

            @Override
            public void onDrawerStateChanged(int newState) {
                TextView userName = findViewById(R.id.employeeNavName);
                userName.setText(user.getFullName());
                TextView userEmail = findViewById(R.id.employeeNavEmail);
                userEmail.setText(user.getEmail());
                ImageView userImage = findViewById(R.id.employeeNavImage);
                if(ContextCompat.checkSelfPermission(EmployeeMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    if(user.getProfileImage() != null){
                        File file = new File(user.getProfileImage());
                        Uri imageUri = Uri.fromFile(file);
                        Glide.with(EmployeeMainActivity.this).load(imageUri).into(userImage);
                    }
                }
            }
        });

    }

    private void subToDeliveries() {
        FirebaseMessaging.getInstance().subscribeToTopic(storeID + "-delivery")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Log.d("DELIVERY-SUB", "Success");
                    } else Log.d("DELIVERY-SUB", "Failed");
                });
    }

    private void unsubFromDeliveries() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(storeID + "-delivery")
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       Log.d("DELIVERY-UNSUB", "Success");
                   }else Log.d("DELIVERY-UNSUB", "Failed");
                });
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(EmployeeMainActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
            String toast = "Hello " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nRole: " + user.getRole() + "\nStoreID: " + storeID;
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_employee_logout:
                firebaseAuth.signOut();
                //TODO: unsub from all store topics
                unsubFromDeliveries();
                SharedPreferences sharedPreferences = EmployeeMainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userData");
                editor.remove("storeID");
                editor.apply();
                startActivity(new Intent(EmployeeMainActivity.this, LoginActivity.class));
                break;
            case R.id.nav_employee_profile:
                Intent myProfile = new Intent(this, CustomerProfileActivity.class);
                myProfile.putExtra("userData", user);
                activityLauncher.launch(myProfile);
                break;

            case R.id.nav_change_store:
                pickStore();
                break;

            case R.id.nav_employee_customer:
                Intent myCustomer = new Intent(this, CustomerMainActivity.class);
                myCustomer.putExtra("userData", user);
                startActivity(myCustomer);
                break;

            case R.id.nav_leave_employee:
                backToAdmin();
                break;
        }
        return true;
    }

    private void backToAdmin() {
        Intent intent = new Intent(EmployeeMainActivity.this, AdminMainActivity.class);

        //activity switch
        intent.putExtra("userData", user);
        intent.putExtra("storeID", storeID);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void pickStore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Odaberite trgovinu");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_store_picker, null);
        Spinner storeDropdown = customLayout.findViewById(R.id.storeSpinner);

        builder.setView(customLayout);
        builder.setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        ArrayList<String> stores = new ArrayList<>();
        database.collection("/locations").whereArrayContains("employees", user.getEmail())
                .get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult()) {
                    stores.add(document.getId());
                }
                dropdownAddStores(stores, storeDropdown);
            } else {
                Toast.makeText(this, "Zaposlenik ne radi niti u jednoj trgovini", Toast.LENGTH_SHORT).show();
            }
        });

        positiveButton.setOnClickListener(view -> {
            if(storeDropdown.getSelectedItem() != null) {
                //TODO actually use the storeId
                Toast.makeText(this, storeDropdown.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                storeID = storeDropdown.getSelectedItem().toString();
                dialog.dismiss();
                employeeLogin();
            }
        });

        negativeButton.setOnClickListener(view -> {

            dialog.dismiss();
        });
    }

    private void employeeLogin() {
        Intent intent = new Intent(EmployeeMainActivity.this, EmployeeMainActivity.class);

        //activity switch
        intent.putExtra("userData", user);
        intent.putExtra("storeID", storeID);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void dropdownAddStores(ArrayList<String> stores, Spinner storeDropdown) {
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stores);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storeDropdown.setAdapter(dropdownAdapter);
    }
}