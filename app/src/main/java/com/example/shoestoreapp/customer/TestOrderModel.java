package com.example.shoestoreapp.customer;

import java.util.ArrayList;

public class TestOrderModel {
    private String employee, time, price;
    private ArrayList<ItemModel> items;
    private boolean expanded;

    public TestOrderModel(String employee, String time, String price, ArrayList<ItemModel> items) {
        this.employee = employee;
        this.time = time;
        this.price = price;
        this.items = items;
        expanded = false;
    }

    public String getEmployee() {
        return employee;
    }

    public String getTime() {
        return time;
    }

    public String getPrice() {
        return price;
    }

    public ArrayList<ItemModel> getItems() {
        return items;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
