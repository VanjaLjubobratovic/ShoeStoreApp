package com.example.shoestoreapp.customer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class ItemModel implements Parcelable {
    private String model;
    private String color;
    private String type;
    private String image;
    private double price;
    private double rating;
    private Timestamp added;
    private ArrayList<Integer> sizes;
    private ArrayList<Integer> amounts;

    //model and color are parsed from document name so we use separate function for that


    public ItemModel(String type, String image, double price, double rating, com.google.firebase.Timestamp added, ArrayList<Integer> sizes, ArrayList<Integer> amounts) {
        this.type = type;
        this.image = image;
        this.price = price;
        this.rating = rating;
        this.added = added;
        this.sizes = sizes;
        this.amounts = amounts;
    }

    public ItemModel() {
    }

    protected ItemModel(Parcel in) {
        model = in.readString();
        color = in.readString();
        type = in.readString();
        image = in.readString();
        price = in.readDouble();
        rating = in.readDouble();
        //added = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<ItemModel> CREATOR = new Creator<ItemModel>() {
        @Override
        public ItemModel createFromParcel(Parcel in) {
            return new ItemModel(in);
        }

        @Override
        public ItemModel[] newArray(int size) {
            return new ItemModel[size];
        }
    };

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public com.google.firebase.Timestamp getAdded() {
        return added;
    }

    public ArrayList<Integer> getSizes() {
        return sizes;
    }

    public ArrayList<Integer> getAmounts() {
        return amounts;
    }

    public void parseModelColor(String modelColor) {
        String[] parts = modelColor.split("-");
        this.model = parts[0];
        this.color = parts[1];
    }

    @NonNull
    @Override
    public String toString() {
        return model + "-" + color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(model);
        parcel.writeString(color);
        parcel.writeString(type);
        parcel.writeString(image);
        parcel.writeDouble(price);
        parcel.writeDouble(rating);
    }
}
