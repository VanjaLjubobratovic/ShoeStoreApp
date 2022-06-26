package com.example.shoestoreapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class UserModel implements Parcelable{
    private String fullName;
    private String email;
    private String address;
    private String role;
    private String city;
    private String phoneNumber;
    private String postalNumber;
    private ArrayList<String> reviewedItems;
    private String profileImage = null;

    public ArrayList<String> getReviewedItems() {
        return reviewedItems;
    }

    public void setReviewedItems(ArrayList<String> reviewedItems) {
        this.reviewedItems = reviewedItems;
    }

    public void addReviewedItem(String item){
        reviewedItems.add(item);
    }

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
        this.profileImage = in.readString();
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

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPostalNumber(String postalNumber) {
        this.postalNumber = postalNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
        parcel.writeString(profileImage);
    }
}
