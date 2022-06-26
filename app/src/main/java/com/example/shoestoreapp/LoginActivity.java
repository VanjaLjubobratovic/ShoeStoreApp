package com.example.shoestoreapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.shoestoreapp.admin.AdminMainActivity;
import com.example.shoestoreapp.customer.CustomerMainActivity;
import com.example.shoestoreapp.customer.CustomerProfileChangeActivity;
import com.example.shoestoreapp.databinding.ActivityLoginBinding;
import com.example.shoestoreapp.employee.EmployeeMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private ProgressDialog loadingBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private CollectionReference usersRef;
    private UserModel user;
    private SharedPreferences sharedPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        loadingBar = new ProgressDialog(LoginActivity.this);

        database = FirebaseFirestore.getInstance();
        usersRef = database.collection("users");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        binding.loginBtn.setEnabled(true);
        binding.loginBtn.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            checkCredentials();
        });

        binding.registerTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        binding.forgotPassText.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Unesite vašu email adresu");

            final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
            builder.setView(customLayout);
            builder.setPositiveButton("OK", null)
                    .setNegativeButton("Odustani", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setOnClickListener(view -> {
                EditText et = customLayout.findViewById(R.id.editText);
                String email = et.getText().toString();
                if(email.isEmpty() || !email.contains("@") || !email.contains(".")) {
                    showErrorCredentials(et, "Invalid email format");
                } else {
                    sendPasswordReset(email);
                    dialog.dismiss();
                }
            });
        });
    }

    private void checkCredentials() {
        //TODO: check if email is verified
        String email = binding.username.getText().toString();
        String password = binding.password.getText().toString();

        if(email.isEmpty() || !email.contains("@"))
            showErrorCredentials(binding.username, "Email is not valid");
        else if(password.isEmpty() || password.length() < 7)
            showErrorCredentials(binding.password, "Password must be at least 8 characters long");
        else {
            loadingBar.setTitle("Authenticating user...");
            loadingBar.setMessage("Please wait while we check your credentials...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            /*
             * Following code will first attempt to authenticate user with a given password
             * and email.
             * After that is successful, the database will be queried to get the user data
             * and to determine which activity should be started according to user role.
             *
             * If authentication fails, alert dialog will be shown and user will be asked
             * to try again.
             */
            //TODO:clean this code up
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {

                                if(!firebaseAuth.getCurrentUser().isEmailVerified()) {
                                    loadingBar.dismiss();
                                    AlertDialog.Builder failedLoginAlert = new AlertDialog.Builder(LoginActivity.this);
                                    failedLoginAlert.setMessage("Please verify your email address before signing in.");
                                    failedLoginAlert.setCancelable(true);

                                    failedLoginAlert.setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.cancel();
                                                }
                                            }
                                    );

                                    failedLoginAlert.setNegativeButton(
                                            "Resend email",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    firebaseAuth.getCurrentUser().sendEmailVerification()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        Toast.makeText(LoginActivity.this, "Verification email has been sent.", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Log.d("EMAIL VERIFICATION", "Failed to send verification email");
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                    );
                                    failedLoginAlert.show();
                                } else {
                                    //database query to get user data
                                    usersRef.whereEqualTo("email", email).get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.isSuccessful()) {
                                                        if(task.getResult().size() == 0) {
                                                            Log.d("FIRESTORE", "0 Results");
                                                            return;
                                                        }

                                                        for(QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d("FIRESTORE", "Fetch succesful");
                                                            user = document.toObject(UserModel.class);
                                                            Log.d("FIRESTORE", user.getFullName());
                                                        }

                                                        //chosing appropriate activity based on user role
                                                        Intent intent = choseIntentByRole(user);
                                                        //TODO: employee thing is kind of a hack, but it works
                                                        if(intent == null || user.getRole().equals("employee"))
                                                            return;

                                                        Toast.makeText(LoginActivity.this, "Succesful login", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();

                                                        //write user data to SharedPreferences
                                                        SharedPreferences.Editor editor = sharedPref.edit();
                                                        Gson gson = new Gson();
                                                        String json = gson.toJson(user);
                                                        editor.putString("userData", json);
                                                        editor.apply();

                                                        //activity switch
                                                        intent.putExtra("userData", user);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();

                                                    } else {
                                                        Log.d("FIRESTORE", "Failed to fetch user");
                                                    }
                                                }
                                            });
                                }
                            } else {
                                //TODO: generate more detailed alerts based on the reason of request failure
                                loadingBar.dismiss();
                                AlertDialog.Builder failedLoginAlert = new AlertDialog.Builder(LoginActivity.this);
                                failedLoginAlert.setMessage("Login has failed. Please check your email and password and try again");
                                failedLoginAlert.setCancelable(true);

                                failedLoginAlert.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        }
                                );
                                failedLoginAlert.show();

                                Toast.makeText(LoginActivity.this, "Failed to login", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    private Intent choseIntentByRole(UserModel user) {
        Intent intent = null;
        if(user == null) {
            Log.d("FIRESTORE", "NULL USER");
        } else if(user.getRole().equals("customer")) {
            intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
        } else if(user.getRole().equals("admin")) {
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else if(user.getRole().equals("employee")) {
            intent = new Intent(LoginActivity.this, EmployeeMainActivity.class);
            pickStore(intent);
        } else {
            Log.d("FIRESTORE", "invalid user role");
        }
        return intent;
    }

    private void pickStore(Intent intent) {
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
                employeeLogin(intent, storeDropdown.getSelectedItem().toString());
                dialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(view -> {
            loadingBar.dismiss();
            dialog.dismiss();
        });
    }

    private void employeeLogin(Intent intent, String storeID) {
        Toast.makeText(LoginActivity.this, "Succesful login", Toast.LENGTH_SHORT).show();
        loadingBar.dismiss();

        //write user data to SharedPreferences
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("userData", json);
        editor.putString("storeID", storeID);
        editor.apply();

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

    private void showErrorCredentials(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //fetch user data from memory
        if(sharedPref.contains("userData")) {
            Gson gson = new Gson();
            String json = sharedPref.getString("userData", "");
            user = gson.fromJson(json, UserModel.class);
        } else {
            Log.d("CHECK USER", "No user in shared preferences");
            return;
        }

        if(firebaseUser == null) {
            Log.d("CHECK USER", "firebaseUser is null");
        } else if(firebaseUser.isEmailVerified()) {
            //redirect user to appropriate activity
            Intent intent = choseIntentByRole(user);
            //TODO: maybe check sharedPref
            if(intent == null || user.getRole().equals("employee"))
                return;
            intent.putExtra("userData", user);
            startActivity(intent);
            finish();
        } else {
            Log.d("CHECK USER", "User email not verified");
        }
    }

    public void sendPasswordReset(String email) {
        try {
            AlertDialog.Builder resetAlert = new AlertDialog.Builder(LoginActivity.this);
            resetAlert.setCancelable(true);

            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    resetAlert.setMessage("Password reset email has been sent!");
                    resetAlert.show();
                } else {
                    resetAlert.setMessage("Account with given email does not exist.");
                    resetAlert.show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}