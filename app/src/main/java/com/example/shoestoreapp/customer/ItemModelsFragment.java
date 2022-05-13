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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private String itemType = "";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
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
    // TODO: Rename and change types and number of parameters
    public static ItemModelsFragment newInstance(String itemType) {
        ItemModelsFragment fragment = new ItemModelsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, itemType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemType = getArguments().getString(ARG_PARAM1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_models, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        helloWorld = view.findViewById(R.id.textViewSelectModel);
        helloWorld.setText(itemType);
        RecyclerView recyclerView = getView().findViewById(R.id.recylcerViewModelList);
        initDummyData(recyclerView);

    }

    private void initDummyData(RecyclerView recycleModel){
        if(itemType == "shoe") {
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
        //Initializing the RecycleViews with the loaded data
        initRecyclerView(recycleModel);
    }

    private void initRecyclerView(RecyclerView recycleModel){

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycleModel.setLayoutManager(layoutManager);
        ModelRecycleViewAdapter adapter = new ModelRecycleViewAdapter(getActivity(), mNames, mImageUrls);
        //app crashes here
        //recycleModel.setAdapter(adapter);
    }
}