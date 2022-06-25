package com.example.shoestoreapp.employee;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
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
    TextView title;
    ArrayList<ItemModel> itemsList;

    String userRole;
    String storeID;


    public InventoryPageFragment(ArrayList<ItemModel> itemsList, String userRole, String storeID) {
        this.itemsList = itemsList;
        this.userRole = userRole;

        //TODO: exception management
        this.storeID = storeID;

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
        title = view.findViewById(R.id.pageTitle);

        if(getContext() != null) {
            adapter = new InventoryGridAdapter(getContext(), itemsList);
            itemsGV.setAdapter(adapter);
        } else Toast.makeText(getContext(), "context null", Toast.LENGTH_SHORT).show();

        title.setText("MODEL: " + itemsList.get(0).getModel());
        itemsGV.setOnItemClickListener((parent, view1, position, id) -> onItemClick(position));
    }


    public void onItemClick(int position) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        //TODO: fetch this path
        Fragment fragment = InventoryAdjustmentFragment.newInstance(itemsList.get(position), "/locations/" + storeID + "/items");

        //Determining whether fragment was opened from admin or employee screen
        //TODO: decide whether to show this or not
        if(userRole.equals("admin")) {
            //ft.replace(R.id.adminActivityLayout, fragment);
            return;
        } else ft.replace(R.id.employeeActivityLayout, fragment);

        //Toast.makeText(getContext(), userRole, Toast.LENGTH_SHORT).show();
        ft.addToBackStack("name").commit();
    }
}