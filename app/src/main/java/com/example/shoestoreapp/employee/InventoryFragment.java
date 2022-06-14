package com.example.shoestoreapp.employee;

import android.content.ClipData;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentDeliveryBinding;
import com.example.shoestoreapp.databinding.FragmentInventoryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


public class InventoryFragment extends Fragment{
    private FragmentInventoryBinding binding;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private ProgressBar progressBar;
    private Spinner modelDropdown, colorDropdown, sizeDropdown;

    private ArrayList<ArrayList<ItemModel>> sortedItemsList, filteredItemsList;
    private ArrayList<String> listOfModels;

    private FirebaseFirestore database;
    private CollectionReference itemsRef;

    private UserModel user;

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

        sortedItemsList = new ArrayList<>();
        listOfModels = new ArrayList<>();

        database = FirebaseFirestore.getInstance();
        //TODO: fetch store attribute
        String collection = "/locations/" + "TestShop1" + "/items";
        itemsRef = database.collection(collection);

        user = (UserModel) getActivity().getIntent().getParcelableExtra("userData");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progressBar = binding.inventoryProgressbar;
        modelDropdown = binding.inventoryModelSpinner;
        colorDropdown = binding.inventoryColorSpinner;
        sizeDropdown = binding.inventorySizeSpinner;

        viewPager = binding.inventoryPager;

        fetchInventory();

        modelDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String model = modelDropdown.getSelectedItem().toString();
                int index = listOfModels.indexOf(model);

                if(index == viewPager.getCurrentItem())
                    return;

                viewPager.setCurrentItem(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        colorDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /*if(!colorDropdown.getSelectedItem().equals("-")) {
                    filterByColor();
                } else resetFilter();*/

                filter(colorDropdown.getSelectedItem().toString(), sizeDropdown.getSelectedItem().toString());

                dropdownAddModels();
                buildPages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sizeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filter(colorDropdown.getSelectedItem().toString(), sizeDropdown.getSelectedItem().toString());

                dropdownAddModels();
                buildPages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                modelDropdown.setSelection(position);
            }
        });
    }

    private void fetchInventory() {
        progressBar.setVisibility(View.VISIBLE);

        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                HashMap<String, ArrayList<ItemModel>> itemsMap = new HashMap<>();

                for(DocumentSnapshot document : task.getResult()) {
                    ItemModel item = document.toObject(ItemModel.class);
                    if(item == null)
                        continue;

                    item.parseModelColor(document.getId());

                    if(itemsMap.get(item.getModel()) == null)
                        itemsMap.put(item.getModel(), new ArrayList<>());

                    itemsMap.get(item.getModel()).add(item);
                }

                sortedItemsList = new ArrayList<>(itemsMap.values());
                listOfModels = new ArrayList<>(itemsMap.keySet());

                resetFilter();
                buildPages();
                dropdownAddModels();
                dropdownAddColors();
                dropdownAddSizes();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Neuspjelo preuzimanje inventara", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildPages() {
        pagerAdapter = new CataloguePagerAdapter(getActivity());
        viewPager.setAdapter(pagerAdapter);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void dropdownAddModels() {
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, listOfModels);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelDropdown.setAdapter(dropdownAdapter);
    }

    private void dropdownAddColors() {
        colorDropdown.setAdapter(null);
        ArrayList<String> colors = new ArrayList<>();

        colors.add("-");
        for(ArrayList<ItemModel> list : sortedItemsList) {
            for(ItemModel item : list ) {
                if(!colors.contains(item.getColor()))
                    colors.add(item.getColor());
            }
        }

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, colors);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorDropdown.setAdapter(dropdownAdapter);
    }

    private void dropdownAddSizes() {
        ArrayList<String> sizes = new ArrayList<>();
        sizes.add("-");

        for(int i = 35; i <= 47; i++)
            sizes.add(String.valueOf(i));

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, sizes);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeDropdown.setAdapter(dropdownAdapter);
    }

    private void filterByColor() {
        resetFilter();
        listOfModels = new ArrayList<>();

        for(Iterator<ArrayList<ItemModel>> listIterator = filteredItemsList.iterator(); listIterator.hasNext();) {
            ArrayList<ItemModel> filteredList = new ArrayList<>();
            ArrayList<ItemModel> oneModelList = listIterator.next();
            for(ItemModel item : oneModelList) {
                if (item.getColor().equals(colorDropdown.getSelectedItem().toString()))
                    filteredList.add(item);
            }
            oneModelList.clear();
            oneModelList.addAll(filteredList);

            if(oneModelList.isEmpty())
                listIterator.remove();
            else listOfModels.add(oneModelList.get(0).getModel());
        }

        Log.d("FILTER_BY_COLOR", sortedItemsList.toString());
    }

    private void filter(String color, String size) {
        //TODO: test this for exceptions and bugs
        if(!color.equals("-"))
            filterByColor();
        else resetFilter();

        if(!size.equals("-"))
            filterBySize();

        if(size.equals("-") && color.equals("-"))
            resetFilter();
    }

    private void filterBySize() {
        listOfModels = new ArrayList<>();
        for(Iterator<ArrayList<ItemModel>> listIterator = filteredItemsList.iterator(); listIterator.hasNext();) {
            ArrayList<ItemModel> filteredList = new ArrayList<>();
            ArrayList<ItemModel> oneModelList = listIterator.next();
            for(ItemModel item : oneModelList) {
                int amountIndex = item.getSizes().indexOf(Integer.parseInt(sizeDropdown.getSelectedItem().toString()));
                if (amountIndex != -1 && item.getAmounts().get(amountIndex) > 0)
                    filteredList.add(item);
            }
            oneModelList.clear();
            oneModelList.addAll(filteredList);

            if(oneModelList.isEmpty())
                listIterator.remove();
            else listOfModels.add(oneModelList.get(0).getModel());
        }

        Log.d("FILTER_BY_COLOR", sortedItemsList.toString());
    }

    private void resetFilter() {
        filteredItemsList = new ArrayList<>();
        listOfModels = new ArrayList<>();
        for(ArrayList<ItemModel> list : sortedItemsList) {
            filteredItemsList.add(new ArrayList<>(list));
            try {
                listOfModels.add(list.get(0).getModel());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Uh oh. Greška.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CataloguePagerAdapter extends FragmentStateAdapter {
        public CataloguePagerAdapter (FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new InventoryPageFragment(filteredItemsList.get(position), user.getRole());
        }

        @Override
        public int getItemCount() {
            return filteredItemsList.size();
        }
    }
}