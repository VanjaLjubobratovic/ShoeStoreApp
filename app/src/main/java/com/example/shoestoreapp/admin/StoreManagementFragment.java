package com.example.shoestoreapp.admin;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.databinding.FragmentStoreManagementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StoreManagementFragment extends Fragment {
    private FragmentStoreManagementBinding binding;
    private ViewFlipper flipper;
    private ImageButton back;
    private MaterialButton addStore;

    private ArrayList<StoreModel> storeList;

    private FirebaseFirestore database;
    private StoreRecyclerViewAdapter adapter;


    public StoreManagementFragment() {
        // Required empty public constructor
    }

    public static StoreManagementFragment newInstance(String param1, String param2) {
        StoreManagementFragment fragment = new StoreManagementFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStoreManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flipper = binding.storeFlipper;
        back = binding.storeManagementBack;
        addStore = flipper.getRootView().findViewById(R.id.newStoreButton);

        fetchStores();

        back.setOnClickListener(view1 -> {
            try{
                getActivity().onBackPressed();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchStores() {
        database = FirebaseFirestore.getInstance();
        storeList = new ArrayList<>();
        database.collection("/locations").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot document : task.getResult()) {
                            StoreModel store = document.toObject(StoreModel.class);
                            if(store == null)
                                continue;

                            store.setStoreID(document.getId());
                            storeList.add(store);
                        }
                        initRecyclerView();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Neuspješno dobavljanje trgovina", Toast.LENGTH_SHORT).show();
                });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = flipper.findViewById(R.id.storeRecycler);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StoreRecyclerViewAdapter(getContext(), storeList, getActivity());
        recyclerView.setAdapter(adapter);
    }
}