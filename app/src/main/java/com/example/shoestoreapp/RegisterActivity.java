package com.example.shoestoreapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("fullName", user.getFullName());
        newUser.put("address", user.getAddress());
        newUser.put("email", user.getEmail());
        newUser.put("role", user.getRole());
        newUser.put("postalNumber", user.getPostalNumber());
        newUser.put("city", user.getCity());
        newUser.put("phoneNumber", user.getPhoneNumber());

        database.collection("users").document(user.getEmail())
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DATABASE APPEND", "DocumentSnapshot successfully written!");

                        sendVerificationEmail();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DATABASE APPEND", "Error writing document", e);
                    }
                });
    }

    private void sendVerificationEmail() {
        if(firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                loadingBar.dismiss();
                                AlertDialog.Builder successfulRegister = new AlertDialog.Builder(RegisterActivity.this);
                                successfulRegister.setMessage("We have sent you an email to verify your email address. " +
                                        "Please follow the instructions in the email before you can log in.");
                                successfulRegister.setCancelable(true);

                                successfulRegister.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                firebaseAuth.signOut();
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                );
                                successfulRegister.show();
                            } else {
                                Log.d("EMAIL VERIFICATION", "Send email failure");
                            }
                        }
                    });
        } else {
            Log.d("EMAIL VERIFICAION", "Null user");
        }
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
            user = new UserModel(address, email, fullName, "customer", city, phone, zip);
            return true;
        }

        return false;
    }

    private void showErrorCredentials(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
}