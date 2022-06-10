package com.example.shoestoreapp.employee;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;

public class ReceiptModel implements Cloneable, Parcelable {
    private String receiptID;
    private String employee;
    private String storeID;
    private String user;
    private double total;
    private ArrayList<ItemModel> items = new ArrayList<>();
    private Timestamp time;
    private boolean packed;
    private boolean annulled;

    public ReceiptModel(String employee, String storeID, String user, double total, ArrayList<ItemModel> items, Timestamp time) {
        this.employee = employee;
        this.storeID = storeID;
        this.user = user;
        this.total = total;
        this.items = items;
        this.time = time;
        this.annulled = false;
        this.packed = false;
    }

    public ReceiptModel(){
        this.total = 0;
        this.packed = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public ReceiptModel(Parcel in) {
        this.employee = in.readString();
        this.storeID = in.readString();
        this.user = in.readString();
        this.total = in.readDouble();
        this.items = in.readArrayList(ItemModel.class.getClassLoader());
        //TODO: fix time reading
        this.time = null;
        this.packed = in.readBoolean();
        this.annulled = in.readBoolean();
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

    public String getReceiptID() {
        return  receiptID;
    }

    public void setReceiptID(String receiptID) {
        this.receiptID = receiptID;
    }

    public boolean isPacked() {
        return packed;
    }

    public boolean isAnnulled() {
        return annulled;
    }

    public void setAnnulled(boolean annulled) {
        this.annulled = annulled;
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

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public Object createFromParcel(Parcel parcel) {
                    return new ReceiptModel(parcel);
                }

                @Override
                public Object[] newArray(int i) {
                    return new ReceiptModel[i];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(employee);
        parcel.writeString(storeID);
        parcel.writeString(user);
        parcel.writeDouble(total);
        parcel.writeList(items);
        parcel.writeBoolean(packed);
        parcel.writeBoolean(annulled);
    }

    public String getItemContents(){
        String contents = "";
        for(ItemModel item : items){
            ArrayList<Integer> tmpAmounts = item.getAmounts();
            contents += item.toString();
            for(Integer amount : tmpAmounts){
                if(amount != 0){
                    contents += " " + item.getSizes().get(tmpAmounts.indexOf(amount)) + " x" + amount + "\n";
                }
            }
        }
        return contents;
    }
}
