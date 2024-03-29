package com.example.shoestoreapp.admin;

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
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.customer.CustomerMainActivity;
import com.example.shoestoreapp.customer.CustomerProfileActivity;
import com.example.shoestoreapp.databinding.ActivityAdminMainBinding;
import com.example.shoestoreapp.employee.EmployeeMainActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.util.ArrayList;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ActivityAdminMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private FirebaseFirestore database;
    private DrawerLayout drawer;
    private String storeID = "TestShop1";


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseFirestore.getInstance();
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        //TODO: rest of the code here
        checkUser();

        subscribeToComplaints();

        getIntent().putExtra("userData", user);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.adminActivityLayout, new AdminMainFragment())
                .commit();


        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.adminDrawerLayout);
        NavigationView navigationView = findViewById(R.id.adminNavView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_layout_open, R.string.drawer_layout_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_leave_employee).setVisible(false);
        nav_Menu.findItem(R.id.nav_change_store).setVisible(false);

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
                if(ContextCompat.checkSelfPermission(AdminMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){
                    if(user.getProfileImage() != null){
                        File file = new File(user.getProfileImage());
                        Uri imageUri = Uri.fromFile(file);
                        Glide.with(AdminMainActivity.this).load(imageUri).into(userImage);
                    }
                }
            }
        });
    }

    private void subscribeToComplaints() {
        FirebaseMessaging.getInstance().subscribeToTopic("complaints")
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       Log.d("COMPLAINTS-SUB", "subscribeToComplaints: SUCCESS");
                   } else Log.d("COMPLAINTS-SUB", "subscribeToComplaints: FAILURE");
                });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(AdminMainActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
            String toast = "Hello " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nRole: " + user.getRole();
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_employee_logout:
                firebaseAuth.signOut();
                SharedPreferences sharedPreferences = AdminMainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userData");
                editor.apply();
                startActivity(new Intent(AdminMainActivity.this, LoginActivity.class));
                finish();
                break;

            case R.id.nav_employee_profile:
                Intent myProfile = new Intent(this, CustomerProfileActivity.class);
                myProfile.putExtra("userData", user);
                activityLauncher.launch(myProfile);
                break;

            case R.id.nav_change_store:
                //PICK STORE BUTTON
                break;

            case R.id.nav_employee_customer:
                Intent myCustomer = new Intent(this, CustomerMainActivity.class);
                myCustomer.putExtra("userData", user);
                startActivity(myCustomer);
                break;

            case R.id.nav_admin_employee:
                //SWITCH TO EMPLOYEE BUTTON
                pickStore();
                break;
        }
        return true;
    }

    private void pickStore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Odaberite trgovinu");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_store_picker, null);
        Spinner storeDropdown = customLayout.findViewById(R.id.storeSpinner);

        builder.setView(customLayout);
        builder.setPositiveButton("Login", null)
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
        Intent intent = new Intent(AdminMainActivity.this, EmployeeMainActivity.class);

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