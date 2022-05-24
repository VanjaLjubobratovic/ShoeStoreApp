package com.example.shoestoreapp.employee;

import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class ReceiptModel {
    private String employee;
    private String storeID;
    private String user;
    private double total;
    private ArrayList<ItemModel> items = new ArrayList<>();
    private Timestamp time;

    public ReceiptModel(String employee, String storeID, String user, double total, ArrayList<ItemModel> items, Timestamp time) {
        this.employee = employee;
        this.storeID = storeID;
        this.user = user;
        this.total = total;
        this.items = items;
        this.time = time;
    }

    public ReceiptModel(){
        this.total = 0;
    }

    public String getUser() {
        return user;
    }

    public String getEmployee() {
        return employee;
    }

    public String getStoreID() {
        return storeID;
    }

    public double getTotal() {
        return total;
    }

    public ArrayList<ItemModel> getItems() {
        return items;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setItems(ArrayList<ItemModel> items) {
        this.items = items;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void addItem(ItemModel item) {
        this.total += item.getPrice();
        this.items.add(item);
    }

    public void removeAt(int position) {
        this.total -= this.items.get(position).getPrice();
        this.items.remove(position);
    }
}
