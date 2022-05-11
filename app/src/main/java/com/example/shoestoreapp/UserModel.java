package com.example.shoestoreapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.auth.User;

public class UserModel implements Parcelable{
    private String name;
    private String lastname;
    private String email;
    private String address;
    private String role;


    public UserModel(String address, String email, String lastname, String name, String role) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.address = address;
        this.role = role;
    }

    public UserModel(){
    }

    public UserModel(Parcel in) {
        this.address = in.readString();
        this.email = in.readString();
        this.lastname = in.readString();
        this.name = in.readString();
        this.role = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(address);
        parcel.writeString(email);
        parcel.writeString(lastname);
        parcel.writeString(name);
        parcel.writeString(role);
    }
}
