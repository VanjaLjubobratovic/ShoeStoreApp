package com.example.shoestoreapp.customer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Objects;

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

    public ItemModel(ItemModel itemModel) {
        this(itemModel.getType(), itemModel.getImage(), itemModel.getPrice(), itemModel.getRating(),
                itemModel.getAdded(), itemModel.getSizes(), itemModel.getAmounts());
        //TODO: add model and color as fields
        this.parseModelColor(itemModel.getModel() + "-" + itemModel.getColor());
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
        sizes = in.readArrayList(Integer.class.getClassLoader());
        amounts = in.readArrayList(Integer.class.getClassLoader());
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

    public void setAmounts(ArrayList<Integer> amounts) {
        this.amounts = amounts;
    }

    public void parseModelColor(String modelColor) {
        String[] parts = modelColor.split("-");
        this.model = parts[0];
        this.color = parts[1];
    }
  
      @Override
    public int hashCode() {
          return Objects.hash(model, color);
    }

    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return model + "-" + color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemModel itemModel = (ItemModel) o;
        return model.equals(itemModel.model) && color.equals(itemModel.color);
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(model);
        parcel.writeString(color);
        parcel.writeString(type);
        parcel.writeString(image);
        parcel.writeDouble(price);
        parcel.writeDouble(rating);
        parcel.writeList(sizes);
        parcel.writeList(amounts);
    }
}
