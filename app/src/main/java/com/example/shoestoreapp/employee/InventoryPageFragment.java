package com.example.shoestoreapp.employee;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class InventoryPageFragment extends Fragment {

    MyGridView itemsGV;
    InventoryGridAdapter adapter;
    ArrayList<ItemModel> itemsList;


    public InventoryPageFragment(ArrayList<ItemModel> itemsList) {
        this.itemsList = itemsList;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory_page, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        itemsGV = (MyGridView) view.findViewById(R.id.inventoryGridViewLayout);

        if(getContext() != null) {
            adapter = new InventoryGridAdapter(getContext(), itemsList);
            itemsGV.setAdapter(adapter);
        } else Toast.makeText(getContext(), "context null", Toast.LENGTH_SHORT).show();

    }
}