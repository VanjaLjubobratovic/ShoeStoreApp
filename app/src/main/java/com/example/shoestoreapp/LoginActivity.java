package com.example.shoestoreapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shoestoreapp.databinding.ActivityLoginBinding;
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


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private ProgressDialog loadingBar;
    private static final int RC_SIGN_IN = 100;

    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth;

    private static final String TAG ="GOOGLE_SIGN_IN";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        loadingBar = new ProgressDialog(LoginActivity.this);

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

        //TODO: enable login button only after credentials format check
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

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                //TODO: check user and redirect him to appropriate activity
                                Toast.makeText(LoginActivity.this, "Succesful login", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, LoggedInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
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

    private void showErrorCredentials(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null) {
            //TODO: check user and redirect him to appropriate activity
            startActivity(new Intent(LoginActivity.this, LoggedInActivity.class));
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