package com.example.shoestoreapp.employee;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;

public class ReceiptModel implements Cloneable{
    private String employee;
    private String storeID;
    private String user;
    private double total;
    private ArrayList<ItemModel> items = new ArrayList<>();
    private Timestamp time;
    private boolean packed;

    public ReceiptModel(String employee, String storeID, String user, double total, ArrayList<ItemModel> items, Timestamp time) {
        this.employee = employee;
        this.storeID = storeID;
        this.user = user;
        this.total = total;
        this.items = items;
        this.time = time;
        this.packed = false;
    }

    public ReceiptModel(){
        this.total = 0;
        this.packed = false;
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

    public boolean isPacked() {
        return packed;
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
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

    public void setTime() {
        this.time = Timestamp.now();
    }

    public void addItem(ItemModel item) {
        this.total += item.getPrice();
        this.items.add(item);
    }

    public void removeAt(int position) {
        this.total -= this.items.get(position).getPrice();
        this.items.remove(position);
    }

    public void packItems() {
        ArrayList<ItemModel> packedItems = new ArrayList<>();
        ArrayList<ItemModel> receiptItems = this.getItems();
        for(ItemModel item : receiptItems) {
            if(!packedItems.contains(item)) {
                packedItems.add(item);
            }
            else {
                int index = packedItems.indexOf(item);
                for(int i = 0; i < receiptItems.get(0).getSizes().size(); i++) {
                    int sizeAmount = packedItems.get(index).getAmounts().get(i) + item.getAmounts().get(i);
                    packedItems.get(index).getAmounts().set(i, sizeAmount);
                }
            }
        }
        this.setItems(packedItems);
        this.packed = true;
    }

    public void unpackItems() {
        ArrayList<ItemModel> unpackedItems = new ArrayList<>();

        for(ItemModel item : this.items) {
            for(int i = 0; i < item.getSizes().size(); i++) {
                int amount = item.getAmounts().get(i);
                for(int j = 0; j < amount; j++) {
                    ArrayList<Integer> unpackedAmounts = new ArrayList<>(Collections.nCopies(item.getAmounts().size(), 0));
                    unpackedAmounts.set(i, 1);
                    ItemModel unpackedItem = new ItemModel(item);
                    unpackedItem.setAmounts(unpackedAmounts);
                    unpackedItems.add(unpackedItem);
                    //Log.d("UNPACKING", "unpackItems: " + );
                }
            }
        }
        this.setItems(unpackedItems);
        this.packed = false;
    }
}
