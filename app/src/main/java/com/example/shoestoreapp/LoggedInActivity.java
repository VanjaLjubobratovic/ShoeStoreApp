package com.example.shoestoreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.shoestoreapp.databinding.ActivityLoggedInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoggedInActivity extends AppCompatActivity {
    private ActivityLoggedInBinding binding;
    private FirebaseAuth firebaseAuth;
    private UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoggedInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = getIntent().getParcelableExtra("userData");

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(LoggedInActivity.this, LoginActivity.class));
            finish();
        } else {
            String email = firebaseUser.getEmail();
            String nameLname = user.getName() + user.getLastname();
            binding.textView.setText("Hello " + nameLname + "\nEmail: " + email + "\nRole: " + user.getRole());
        }
    }
}