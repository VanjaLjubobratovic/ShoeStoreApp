package com.example.shoestoreapp.customer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class CustomerProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ImageButton exit;
    private ImageView profilePic;
    private MaterialButton logoutBtn, changeBtn;
    private String fullName,name,surname,email,address,city,postalCode,phone;
    private String[] names;
    private TextView profileName, profileSurname, profileEmail, profileAddress, profileCityAndCode,
            profilePhone;
    private final Integer EXT_REQUEST_CODE = 71;


    //Getting new details after changing user profile details
    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == 78){
                Intent intent = result.getData();

                if(intent != null){
                    user = intent.getParcelableExtra("userResult");
                    setUserData();
                }
            }

        }
    });

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.putExtra("userResult", user);
        setResult(49, intent);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        user=getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();


        checkUser();

        //Back to previous activity button onclick
        exit = findViewById(R.id.imageButtonCloseProfile);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("userResult", user);
                setResult(88, intent);

                CustomerProfileActivity.super.onBackPressed();
            }
        });

        //Logout button onclick
        logoutBtn = findViewById(R.id.buttonProfileLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //logging out the user
                firebaseAuth.signOut();
                SharedPreferences sharedPreferences = CustomerProfileActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userData");
                editor.apply();
                startActivity(new Intent(CustomerProfileActivity.this, LoginActivity.class));
                finish();
            }
        });


        //Change profile details onClick -> new activity
        changeBtn = findViewById(R.id.buttonProfileChanges);
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new activity with return data
                Intent profileChangeIntent = new Intent(CustomerProfileActivity.this, CustomerProfileChangeActivity.class);
                profileChangeIntent.putExtra("userData", user);
                activityLauncher.launch(profileChangeIntent);
            }
        });

        //Loading user data from intent data (user firebase object)
        setUserData();

    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(CustomerProfileActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
        }
    }

    //Reading user data from userDetails and writing it to appropriate elements
    private void setUserData(){
        fullName = user.getFullName();
        names = fullName.split(" ");
        if(names.length > 1){
            name = names[0];
            surname = names[1];
        }
        else{
            name = names[0];
            surname = "";
        }
        email = user.getEmail();
        address = user.getAddress();
        city = user.getCity();
        postalCode = user.getPostalNumber();
        phone = user.getPhoneNumber();


        profileName = findViewById(R.id.textViewProfileName);
        profileName.setText(name);
        profileSurname = findViewById(R.id.textViewProfileSurname);
        profileSurname.setText(surname);
        profileEmail = findViewById(R.id.textViewProfileEmail);
        profileEmail.setText(email);
        profileCityAndCode = findViewById(R.id.textViewProfileCityPostal);
        profileCityAndCode.setText(city + " "+ postalCode);
        profileAddress = findViewById(R.id.textViewProfileAdress);
        profileAddress.setText(address);
        profilePhone = findViewById(R.id.textViewProfilePhone);
        profilePhone.setText(phone);
        profilePic = findViewById(R.id.imageViewProfilePic);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            if(user.getProfileImage() != null){
                File file = new File(user.getProfileImage());
                Uri imageUri = Uri.fromFile(file);
                Glide.with(CustomerProfileActivity.this).load(imageUri).into(profilePic);
            }
        }
        else{
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXT_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXT_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("REQUEST", "onRequestPermissionsResult: ");
                File file = new File(user.getProfileImage());
                Uri imageUri = Uri.fromFile(file);
                Glide.with(CustomerProfileActivity.this).load(imageUri).into(profilePic);
            } else {
                Toast.makeText(this, "Odbijeno dopuštenje za slike", Toast.LENGTH_SHORT).show();
            }
        }
    }
}