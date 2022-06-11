package com.example.shoestoreapp.employee;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.databinding.ActivityEmployeeMainBinding;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class EmployeeMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ActivityEmployeeMainBinding binding;
    private DrawerLayout drawer;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        //TODO: rest of the code here
        checkUser();

        getIntent().putExtra("userData", user);
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
            String toast = "Hello " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nRole: " + user.getRole();
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_item1:
                //TODO Drawer onclick
                break;
        }
        return true;
    }
}