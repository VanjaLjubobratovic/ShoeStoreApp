package com.example.shoestoreapp.customer;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.admin.AdminMainActivity;
import com.example.shoestoreapp.databinding.ActivityAdminMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class CustomerProfileActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ImageButton exit;
    private Button logoutBtn;
    private String fullName,name,surname,email,address,city,postalCode,phone;
    private String[] names;
    private TextView profileName, profileSurname, profileEmail, profileAddress, profileCityAndCode,
            profilePhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        user=getIntent().getParcelableExtra("userData");
        firebaseAuth = firebaseAuth.getInstance();


        checkUser();

        //Back to previous activity button onclick
        exit = findViewById(R.id.imageButtonCloseProfile);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Logout button onclick
        logoutBtn = findViewById(R.id.buttonProfileLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                SharedPreferences sharedPreferences = CustomerProfileActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userData");
                editor.apply();
                startActivity(new Intent(CustomerProfileActivity.this, LoginActivity.class));
                finish();
            }
        });

        //Loading user data from intent data (user firebase object)
        fullName = user.getFullName();
        names = fullName.split(" ");
        if(names.length > 0){
            name = names[0];
            surname = names[1];
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
}