package com.example.shoestoreapp.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemModelsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemModelsFragment extends Fragment implements ModelRecycleViewAdapter.OnModelListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CATEGORY_PARAM = "category", ITEMS_PARAM = "listOfItems", USER_PARAM = "userParams";
    private String itemType = "";

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<Float> ratings = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView helloWorld;
    private ImageButton back;
    private UserModel user;
    private ArrayList<ItemModel> items = new ArrayList<>(), models = new ArrayList<>();
    public ItemModelsFragment() {

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemModelsFragment.
     */

    //Setting the arguments got from parent activity
    public static ItemModelsFragment newInstance(String itemType, ArrayList<ItemModel> items, UserModel user) {
        ItemModelsFragment fragment = new ItemModelsFragment();
        Bundle args = new Bundle();
        args.putString(CATEGORY_PARAM, itemType);
        args.putSerializable(ITEMS_PARAM,(Serializable) items);
        args.putParcelable(USER_PARAM, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Getting the passed values from parent activity
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemType = getArguments().getString(CATEGORY_PARAM);
            items = (ArrayList<ItemModel>) getArguments().getSerializable(ITEMS_PARAM);
            user = getArguments().getParcelable(USER_PARAM);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_models, container, false);


        //Binding the recycle view to the loaded data
        RecyclerView recyclerView = view.findViewById(R.id.recylcerViewModelList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        models = getModels(items, itemType);
        ModelRecycleViewAdapter adapter = new ModelRecycleViewAdapter(getContext(), models, this);
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        helloWorld = view.findViewById(R.id.textViewSelectModel);

        //Closing the fragment on x button click
        back = view.findViewById(R.id.imageButtonModelExit);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

    }

    //Removing duplicate models and separating by itemType
    private ArrayList<ItemModel> getModels (ArrayList<ItemModel> items, String itemType){
        ArrayList<ItemModel> uniqueModels = new ArrayList<>();
        for(ItemModel item : items){
            if(item.getType().contains(itemType) && !uniqueModels.contains(item)){
                uniqueModels.add(item);
            }
        }

        return uniqueModels;
    }


    @Override
    public void onModelClick(int position) {
        Toast.makeText(getActivity(), "CLICKED", Toast.LENGTH_SHORT).show();

        ItemModel selectedModel = models.get(position);
        Intent singleItemIntent = new Intent(getActivity(),   SingleItemActivity.class);
        singleItemIntent.putExtra("selectedItem", selectedModel);
        singleItemIntent.putExtra("userData", user);
        startActivity(singleItemIntent);
    }
}