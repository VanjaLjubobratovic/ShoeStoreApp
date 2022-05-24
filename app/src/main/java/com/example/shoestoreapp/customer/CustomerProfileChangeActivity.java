package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.remote.WatchChangeAggregator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class CustomerProfileChangeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private MaterialButton cancel, save;

    private String fullName,name,surname,email,address,city,postalCode,phone, tmpCity;
    private String[] names, cityPost;
    private EditText profileName, profileSurname, profileAddress, profileCityAndCode,
            profilePhone;
    private TextView profileEmail;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile_change);



        user=getIntent().getParcelableExtra("userData");
        firebaseAuth = firebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        checkUser();


        //Cancel button on click, finish activity
        cancel = findViewById(R.id.buttonProfileChangeCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //Save button onClick, read data from EditTexts and send it to previous activity
        save = findViewById(R.id.buttonProfileChangeSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Reading new user details
                email = profileEmail.getText().toString();
                name = profileName.getText().toString();
                surname = profileSurname.getText().toString();
                address = profileAddress.getText().toString();
                phone = profilePhone.getText().toString();

                tmpCity = profileCityAndCode.getText().toString();
                cityPost = tmpCity.split(" ");
                int citySize = cityPost.length;
                if(citySize >= 2){
                    postalCode = cityPost[citySize-1];
                    city = cityPost[0];
                    for(int i = 1; i < citySize - 1;i++){
                        city += " ";
                        city += cityPost[i];
                    }
                }

                //Setting the new user details
                user.setFullName(name + " " + surname);
                user.setAddress(address);
                user.setCity(city);
                user.setPhoneNumber(phone);
                user.setPostalNumber(postalCode);
                //user.setEmail(email);

                //TODO save user data to database

                //nisam siguran ako ovo radi pa nisam htio sjebat usere, morat ces ti provjerit
                //addUserToDatabase();


                Intent intent = new Intent();
                intent.putExtra("userResult", user);
                setResult(78, intent);

                CustomerProfileChangeActivity.super.onBackPressed();
            }
        });


        //Reading user data from userDetails and writing it to appropriate elements
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

        profileName = findViewById(R.id.editTxtProfileName);
        profileName.setText(name);
        profileSurname = findViewById(R.id.editTxtProfileSurname);
        profileSurname.setText(surname);
        profileEmail = findViewById(R.id.editTxtProfileEmail);
        profileEmail.setText(email);
        profileCityAndCode = findViewById(R.id.editTxtProfileCityPostal);
        profileCityAndCode.setText(city + " "+ postalCode);
        profileAddress = findViewById(R.id.editTxtProfileAdress);
        profileAddress.setText(address);
        profilePhone = findViewById(R.id.editTxtProfilePhone);
        profilePhone.setText(phone);

    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(CustomerProfileChangeActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
        }
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DATABASE APPEND", "Error writing document", e);
                    }
                });
    }
}