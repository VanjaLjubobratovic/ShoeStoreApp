package com.example.shoestoreapp.DataModels;


import com.google.firebase.Timestamp;

public class OrderModel extends ReceiptModel {
    private Timestamp dateCreated;
    private Integer orderCode;
    private boolean expanded;
    private boolean inStore;
    private boolean pickedUp;
    private boolean reviewEnabled;
    private String deliveryAddress;

    @Override
    public String toString() {
        return "OrderModel{" +
                "orderCode=" + orderCode +
                '}';
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public boolean isInStore() {
        return inStore;
    }

    public void setInStore(boolean inStore) {
        this.inStore = inStore;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public boolean isReviewEnabled() {
        return reviewEnabled;
    }

    public void setReviewEnabled(boolean reviewEnabled) {
        this.reviewEnabled = reviewEnabled;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Integer orderCode) {
        this.orderCode = orderCode;
    }
}
