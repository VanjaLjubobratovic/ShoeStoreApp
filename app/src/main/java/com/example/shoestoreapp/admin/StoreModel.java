package com.example.shoestoreapp.admin;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class StoreModel {
    private String storeID;
    private String address;
    private String type;
    private ArrayList<String> employees;
    private GeoPoint location;
    private boolean enabled;

    public StoreModel(String storeID, String address, String type, ArrayList<String> employees, GeoPoint location, boolean enabled) {
        this.storeID = storeID;
        this.address = address;
        this.type = type;
        this.employees = employees;
        this.location = location;
        this.enabled = enabled;
    }

    public StoreModel(){}

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<String> employees) {
        this.employees = employees;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
