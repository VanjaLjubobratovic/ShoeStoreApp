package com.example.shoestoreapp;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable{
    private String fullName;
    private String email;
    private String address;
    private String role;
    private String city;
    private String phoneNumber;
    private String postalNumber;


    public UserModel(String address, String email, String fullName, String role, String city, String phoneNumber, String postalNumber) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.role = role;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.postalNumber = postalNumber;
    }

    public UserModel(){
    }

    public UserModel(Parcel in) {
        this.address = in.readString();
        this.email = in.readString();
        this.fullName = in.readString();
        this.role = in.readString();
        this.city = in.readString();
        this.phoneNumber = in.readString();
        this.postalNumber = in.readString();
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

    public String getFullName() {
        return fullName;
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

    public String getCity() {
        return city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPostalNumber() {
        return postalNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(address);
        parcel.writeString(email);
        parcel.writeString(fullName);
        parcel.writeString(role);
        parcel.writeString(city);
        parcel.writeString(phoneNumber);
        parcel.writeString(postalNumber);
    }
}
