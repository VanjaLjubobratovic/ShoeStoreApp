package com.example.shoestoreapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shoestoreapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private ProgressDialog loadingBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private UserModel user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingBar = new ProgressDialog(RegisterActivity.this);
        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //cancel registration
        binding.cancelRegistrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.confirmRegistrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkCredentials())
                    return;

                loadingBar.setTitle("Creating new user...");
                loadingBar.setMessage("Please wait while we create new user");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), binding.registerPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Log.d("USER REGISTRATION", "createUserWithEmail:success");
                                    //TODO: implement sending verification email
                                    addUserToDatabase();
                                } else {
                                    Log.d("USER REGISTRATION", "createUserWithEmail:failure");
                                }
                            }
                        });
            }
        });
    }

    private void addUserToDatabase() {
        //TODO: expand the model to include new fields from register screen
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("name", user.getName());
        newUser.put("lastname", user.getLastname());
        newUser.put("address", user.getAddress());
        newUser.put("email", user.getEmail());
        newUser.put("role", user.getRole());

        database.collection("users").document(user.getEmail())
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DATABASE APPEND", "DocumentSnapshot successfully written!");
                        loadingBar.dismiss();

                        Toast.makeText(RegisterActivity.this, "Account created successfuly", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DATABASE APPEND", "Error writing document", e);
                    }
                });
    }

    private Boolean checkCredentials() {
        String email = binding.registerEmail.getText().toString();
        String password = binding.registerPassword.getText().toString();
        String confirmPassword = binding.registerConfirmPassword.getText().toString();
        String address = binding.registerAddress.getText().toString();
        String zip = binding.registerZip.getText().toString();
        String city = binding.registerCity.getText().toString();
        String fullName = binding.registerName.getText().toString();
        String phone = binding.registerPhoneNumber.getText().toString();

        //TODO: create better conditions
        if(email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            showErrorCredentials(binding.registerEmail, "Email not valid");
        } else if(password.isEmpty() || password.length() < 7) {
            showErrorCredentials(binding.registerPassword, "Password must be at least 8 characters long");
        } else if(!password.equals(confirmPassword)) {
            showErrorCredentials(binding.registerConfirmPassword, "Passwords must match");
        } else if(address.isEmpty()) {
            showErrorCredentials(binding.registerAddress, "Please, input an address");
        } else if(zip.isEmpty() || zip.matches(".*[a-zA-Z]+.*")) {
            showErrorCredentials(binding.registerZip, "Invalid postal number");
        } else if(city.isEmpty()) {
            showErrorCredentials(binding.registerCity, "Invalid city");
        } else if(fullName.isEmpty()) {
            showErrorCredentials(binding.registerName, "Invalid name");
        } else if(phone.isEmpty() || phone.matches(".*[a-zA-Z]+.*")) {
            showErrorCredentials(binding.registerPhoneNumber, "Invalid phone number");
        } else {
            //TODO:do something with splitting full name into name and lastname
            //TODO:use all data to create user
            user = new UserModel(address, email, fullName, fullName, "customer");
            return true;
        }

        return false;
    }

    private void showErrorCredentials(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
}