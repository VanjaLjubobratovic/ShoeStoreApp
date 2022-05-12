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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shoestoreapp.admin.AdminMainActivity;
import com.example.shoestoreapp.customer.CustomerMainActivity;
import com.example.shoestoreapp.databinding.ActivityLoginBinding;
import com.example.shoestoreapp.employee.EmployeeMainActivity;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private ProgressDialog loadingBar;
    private static final int RC_SIGN_IN = 100;

    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private CollectionReference usersRef;
    private UserModel user;
    private SharedPreferences sharedPref;

    private static final String TAG ="GOOGLE_SIGN_IN";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        loadingBar = new ProgressDialog(LoginActivity.this);

        database = FirebaseFirestore.getInstance();
        usersRef = database.collection("users");
        sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);

        setContentView(binding.getRoot());

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        binding.googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "BEGIN SIGN IN");
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

        binding.loginBtn.setEnabled(true);
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
                checkCredentials();
            }
        });
    }

    private void checkCredentials() {
        String email = binding.username.getText().toString();
        String password = binding.password.getText().toString();

        if(email.isEmpty() || !email.contains("@"))
            showErrorCredentials(binding.username, "Email is not valid");
        else if(password.isEmpty() || password.length() < 7)
            showErrorCredentials(binding.password, "Password must be at least 7 characters long");
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
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
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
                                                        Log.d("FIRESTORE", user.getLastname());
                                                    }

                                                    //chosing appropriate activity based on user role
                                                    Intent intent = choseIntentByRole(user);
                                                    if(intent == null)
                                                        return;

                                                    Toast.makeText(LoginActivity.this, "Succesful login", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();

                                                    //write user data to SharedPreferences
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    Gson gson = new Gson();
                                                    String json = gson.toJson(user);
                                                    editor.putString("userData", json);
                                                    editor.commit();

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
        } else {
            Log.d("FIRESTORE", "invalid user role");
        }
        return intent;
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
        }

        if(firebaseUser != null && user != null) {
            //redirect user to appropriate activity
            Intent intent = choseIntentByRole(user);
            if(intent == null)
                return;

            intent.putExtra("userData", user);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Google Sign In intent Result");
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                firebaseAuthWithGoogleAccount(account);
            } catch (Exception e) {
                Log.d(TAG, "onActivityResult: Error " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Logged in");
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();

                        Log.d(TAG, "onSuccess: Email " + email);
                        Log.d(TAG, "onSuccess: UID " + uid);
                        
                        //check if user is new
                        if(authResult.getAdditionalUserInfo().isNewUser()) {
                            Log.d(TAG, "onSuccess: Account Created... " + email);
                            Toast.makeText(LoginActivity.this, "Account Created... \n" + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "onSuccess: Existing user " + email);
                        }

                        startActivity(new Intent(LoginActivity.this, LoggedInActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onSuccess: Login failed " + e.getMessage());
                    }
                });
    }
}