package com.example.shoestoreapp.employee;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class InventoryLogModel {
    private String model, color, type, description, employee;
    private ArrayList<Integer> sizes, amounts;
    private Timestamp time;

    public InventoryLogModel(String model, String color, String type, String description, String employee, ArrayList<Integer> sizes, ArrayList<Integer> amounts, Timestamp time) {
        this.model = model;
        this.color = color;
        this.type = type;
        this.description = description;
        this.employee = employee;
        this.sizes = sizes;
        this.amounts = amounts;
        this.time = time;
    }

    public InventoryLogModel() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public ArrayList<Integer> getSizes() {
        return sizes;
    }

    public void setSizes(ArrayList<Integer> sizes) {
        this.sizes = sizes;
    }

    public ArrayList<Integer> getAmounts() {
        return amounts;
    }

    public void setAmounts(ArrayList<Integer> amounts) {
        this.amounts = amounts;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
