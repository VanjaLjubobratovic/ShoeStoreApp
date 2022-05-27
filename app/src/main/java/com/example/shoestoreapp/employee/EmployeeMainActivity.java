package com.example.shoestoreapp.employee;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.databinding.ActivityEmployeeMainBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Remember that this is a separate package when trying to use something from outside
//Take a look at how R had to be imported above this comment

public class EmployeeMainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ActivityEmployeeMainBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        //TODO: rest of the code here
        checkUser();


        getSupportFragmentManager().beginTransaction()
                .add(R.id.employeeActivityLayout, new EmployeeMainFragment())
                .commit();
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
}