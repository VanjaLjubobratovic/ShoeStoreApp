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

import java.util.ArrayList;
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

                loadingBar.setTitle("Stvaram novog korisnika...");
                loadingBar.setMessage("Molimo pričekajte, vaš račun se stvara.");
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
        newUser.put("reviewedItems", new ArrayList<String>());

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
                                successfulRegister.setMessage("Poslali smo Vam email za potvrdu vaše email adrese. " +
                                        "Molimo Vas, pratite upute u mailu da biste se mogli prijaviti u aplikaciju.");
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
            showErrorCredentials(binding.registerPassword, "Lozinka mota sadržavati bar 7 znakova");
        } else if(!password.equals(confirmPassword)) {
            showErrorCredentials(binding.registerConfirmPassword, "Lozinke se ne podudaraju");
        } else if(address.isEmpty()) {
            showErrorCredentials(binding.registerAddress, "Molimo Vas, unesite adresu.");
        } else if(zip.isEmpty() || zip.matches(".*[a-zA-Z]+.*")) {
            showErrorCredentials(binding.registerZip, "Unesite poštanski broj");
        } else if(city.isEmpty()) {
            showErrorCredentials(binding.registerCity, "Unesite grad");
        } else if(fullName.isEmpty()) {
            showErrorCredentials(binding.registerName, "Unesite ime");
        } else if(phone.isEmpty() || phone.matches(".*[a-zA-Z]+.*")) {
            showErrorCredentials(binding.registerPhoneNumber, "Nevažeći broj telefona");
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