package com.example.shoestoreapp.employee;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentDeliveryBinding;
import com.example.shoestoreapp.databinding.FragmentInventoryBinding;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;


public class InventoryFragment extends Fragment {
    private FragmentInventoryBinding binding;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    private ArrayList<ArrayList<ItemModel>> itemModelsList;


    public InventoryFragment() {
        // Required empty public constructor
    }

    public static InventoryFragment newInstance(String param1, String param2) {
        InventoryFragment fragment = new InventoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemModelsList = new ArrayList<>();

        //TODO: change this dummy code
        ArrayList<ItemModel> itemsList = new ArrayList<>();
        ArrayList<Integer> sizes = new ArrayList<>(
                Arrays.asList(35, 36, 37, 38, 39, 40));
        ArrayList<Integer> amounts = new ArrayList<>(
                Arrays.asList(1, 1, 1, 1, 1, 1));

        ItemModel item = new ItemModel("cipela", "501crvene.jpg", 280.00, 5, Timestamp.now(), sizes, amounts);
        item.parseModelColor("501-crvena");

        for(int i = 0; i < 15; i++)
            itemsList.add(item);
        itemModelsList.add(itemsList);

        itemsList = new ArrayList<>();
        item = new ItemModel("cipela", "505zuta.jpg", 280.00, 5, Timestamp.now(), sizes, amounts);
        item.parseModelColor("505-zuta");

        for(int i = 0; i < 15; i++)
            itemsList.add(item);
        itemModelsList.add(itemsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = binding.inventoryPager;
        pagerAdapter = new CataloguePagerAdapter(getActivity());
        viewPager.setAdapter(pagerAdapter);
    }



    private class CataloguePagerAdapter extends FragmentStateAdapter {
        public CataloguePagerAdapter (FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new InventoryPageFragment(itemModelsList.get(position));
        }

        @Override
        public int getItemCount() {
            return itemModelsList.size();
        }
    }
}