package com.example.shoestoreapp.customer;

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
import android.widget.TextView;
import android.widget.Toast;


import com.example.shoestoreapp.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemModelsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemModelsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CATEGORY_PARAM = "category";
    private String itemType = "";

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<Float> ratings = new ArrayList<>();
    private RecyclerView recyclerView;

    private TextView helloWorld;

    public ItemModelsFragment() {

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemModelsFragment.
     */

    public static ItemModelsFragment newInstance(String itemType) {
        ItemModelsFragment fragment = new ItemModelsFragment();
        Bundle args = new Bundle();
        args.putString(CATEGORY_PARAM, itemType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemType = getArguments().getString(CATEGORY_PARAM);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_models, container, false);


        //Binding the recycle view to the loaded data
        RecyclerView recyclerView = view.findViewById(R.id.recylcerViewModelList);
        initDummyData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        ModelRecycleViewAdapter adapter = new ModelRecycleViewAdapter(getContext(), mNames, mImageUrls);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        helloWorld = view.findViewById(R.id.textViewSelectModel);
        helloWorld.setText(itemType);

    }

    //load data depending on which category was clicked
    private void initDummyData(){
        if(itemType.equals("shoe")) {

            mImageUrls.add("https://c1.staticflickr.com/5/4636/25316407448_de5fbf183d_o.jpg");
            mNames.add("Cipele model 1");

            mImageUrls.add("https://i.redd.it/tpsnoz5bzo501.jpg");
            mNames.add("Cipele model 2");


            mImageUrls.add("https://i.redd.it/qn7f9oqu7o501.jpg");
            mNames.add("Cipele model 3");


            mImageUrls.add("https://i.redd.it/j6myfqglup501.jpg");
            mNames.add("Cipele model 4");
        }
        else{

            mImageUrls.add("https://c1.staticflickr.com/5/4636/25316407448_de5fbf183d_o.jpg");
            mNames.add("Torba model 1");

            mImageUrls.add("https://i.redd.it/tpsnoz5bzo501.jpg");
            mNames.add("Torba model 2");


            mImageUrls.add("https://i.redd.it/qn7f9oqu7o501.jpg");
            mNames.add("Torba model 3");


            mImageUrls.add("https://i.redd.it/j6myfqglup501.jpg");
            mNames.add("Torba model 4");
        }
    }

}