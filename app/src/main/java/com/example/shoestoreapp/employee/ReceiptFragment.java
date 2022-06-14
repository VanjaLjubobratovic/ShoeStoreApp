package com.example.shoestoreapp.employee;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ReceiptFragment extends Fragment {

    private int sizeIndex = 0;

    //private EditText modelEt, colorEt, sizeEt;
    private Spinner modelSpinner, colorSpinner, sizeSpinner;
    private MaterialButton addButton, confirmButton;
    private ImageView shoeImage, availableImageView;
    private TextView shoePriceTextView, totalTextView;
    private ProgressBar loadingBar;

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference itemsRef;
    private StorageReference storageRef;

    private HashMap<String, ArrayList<ItemModel>> itemsMap;
    private ItemModel currentItem;
    private ArrayList<ItemModel> itemsToRemove = new ArrayList<>();
    private ReceiptModel receipt;
    private UserModel user;

    private boolean editReceipt = false;

    public ReceiptFragment() {
    }

    public static ReceiptFragment newInstance(ReceiptModel receipt) {
        ReceiptFragment fragment = new ReceiptFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("editReceipt", receipt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
        //TODO:Replace this placeholder after you implement employee to store binding in login
        //String collection = "/locations/" + getIntent().getStringExtra("shopID") + "/items";
        String collection = "/locations/" + "TestShop1" + "/items";
        itemsRef = database.collection(collection);

        user = (UserModel) getActivity().getIntent().getParcelableExtra("userData");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if(getArguments() != null && getArguments().containsKey("editReceipt")) {
            Log.d("ARGS", "Argument exists");
            receipt = (ReceiptModel) getArguments().getParcelable("editReceipt");
            editReceipt = true;
        } else {
            Log.d("ARGS", "No arguments");
            receipt = new ReceiptModel();
            receipt.setEmployee(user.getEmail());
            receipt.setUser("");
            receipt.setReceiptID("");
            //TODO:get real value
            receipt.setStoreID("TestShop1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*modelEt = view.findViewById(R.id.modelEditText);
        colorEt = view.findViewById(R.id.colorEditText);
        sizeEt = view.findViewById(R.id.sizeEditText);*/

        modelSpinner = view.findViewById(R.id.receiptModelDropdown);
        colorSpinner = view.findViewById(R.id.receiptColorDropdown);
        sizeSpinner = view.findViewById(R.id.receiptSizeDropdown);

        shoeImage = view.findViewById(R.id.shoeImage);
        availableImageView = view.findViewById(R.id.availableImageView);
        shoePriceTextView = view.findViewById(R.id.shoePriceTextView);
        loadingBar = view.findViewById(R.id.loading);
        addButton = view.findViewById(R.id.addBtn);
        confirmButton = view.findViewById(R.id.confirmBtn);
        totalTextView = view.findViewById(R.id.totalTextView);

        availableImageView.setVisibility(View.INVISIBLE);
        confirmButton.setEnabled(false);
        addButton.setEnabled(false);

        totalTextView.setText("UKUPNO: " + receipt.getTotal() + "kn");

        if(editReceipt) {
            initRecyclerView();
        }

        fetchInventory();

        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dropdownAddColors(modelSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(colorSpinner.getSelectedItem().toString().equals("-")) {
                    clearItemPreview();
                } else {
                    findItem();
                    dropdownAddSizes();
                    setItemPreview();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!colorSpinner.getSelectedItem().toString().equals("-")) {
                    if (isAvailable()) {
                        availableImageView.setImageResource(R.drawable.ic_baseline_check_24);
                        addButton.setEnabled(true);
                    } else {
                        availableImageView.setImageResource(R.drawable.ic_close_x);
                        addButton.setEnabled(false);
                    }
                    availableImageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:add item to recycler view
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);


                //TODO:make dedicated constructor after merge
                ArrayList<Integer> sizeList = new ArrayList<>(Collections.nCopies(currentItem.getSizes().size(), 0));
                sizeList.set(sizeIndex, 1);

                ItemModel receiptItem = new ItemModel(currentItem.getType(), currentItem.getImage(), currentItem.getPrice(),
                currentItem.getRating(), currentItem.getAdded(), currentItem.getSizes(), sizeList);
                receiptItem.parseModelColor(currentItem.toString());

                receipt.addItem(receiptItem);
                //TODO: Fix this terrible hack
                initRecyclerView();

                clearInput();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receipt.packItems();
                receipt.setTime();

                Log.d("CONFIRM BUTTON", receipt.getItems().get(0).getAmounts().toString());

                addReceiptToDB();
                adjustInventory();
                clearDataAndUI();
            }
        });
    }

    private void findItem() {
        ArrayList<ItemModel> models = itemsMap.get(modelSpinner.getSelectedItem().toString());
        if(models == null)
            return;

        for(ItemModel item : models) {
            if(item.getColor().equals(colorSpinner.getSelectedItem().toString())) {
                currentItem = item;
                break;
            }
        }
    }

    private boolean isAvailable() {
        int size = Integer.parseInt(sizeSpinner.getSelectedItem().toString());
        int amountIndex = currentItem.getSizes().indexOf(size);

        return currentItem.getAmounts().get(amountIndex) > 0;
    }

    private void fetchInventory() {
        itemsMap = new HashMap<>();
        itemsRef.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot document : task.getResult()) {
                            ItemModel item = document.toObject(ItemModel.class);
                            if(item == null)
                                continue;

                            item.parseModelColor(document.getId());
                            if(!itemsMap.containsKey(item.getModel()))
                                itemsMap.put(item.getModel(), new ArrayList<>());

                            try {
                                itemsMap.get(item.getModel()).add(item);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        dropdownAddModels();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Neuspjelo preuzimanje inventara", Toast.LENGTH_SHORT).show());
    }

    private void dropdownAddModels() {
        ArrayList<String> models = new ArrayList<>(itemsMap.keySet());
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, models);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(dropdownAdapter);
    }

    private void dropdownAddColors(String model) {
        ArrayList<ItemModel> extractedModel = itemsMap.get(model);
        ArrayList<String> colors = new ArrayList<>();
        colors.add("-");

        try {
            for(ItemModel item : extractedModel)
                if(!colors.contains(item.getColor()))
                    colors.add(item.getColor());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, colors);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(dropdownAdapter);

    }

    private void dropdownAddSizes() {
        ArrayList<String> sizes = new ArrayList<>();
        for(Integer i : currentItem.getSizes())
            sizes.add(String.valueOf(i));

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, sizes);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(dropdownAdapter);
    }

    private void initRecyclerView() {
        Log.d("RECYCLER VIEW", "initRecyclerView: ");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = this.getView().findViewById(R.id.receiptRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ReceiptRecyclerViewAdapter adapter = new ReceiptRecyclerViewAdapter(getContext(), receipt, totalTextView, confirmButton, itemsToRemove);
        recyclerView.setAdapter(adapter);
    }

    private void adjustInventory() {
        if(editReceipt) {
            for(ItemModel item : itemsToRemove) {
                for (int i = 0; i < item.getAmounts().size(); i++) {
                    item.getAmounts().set(i, item.getAmounts().get(i) * (-1));
                }
            }
        }

        ArrayList<ItemModel> itemsToAdjust = receipt.getItems();
        itemsToAdjust.addAll(itemsToRemove);

        //TODO: find if there's a better method
        for(ItemModel item : itemsToRemove) {
            DocumentReference itemDocumentRef = itemsRef.document(item.toString());
            ArrayList<Integer> adjustedAmountsList = new ArrayList<>();

            //TODO: Generalise this boilerplate code
            itemDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        Log.d("FIRESTORE", task.getResult().toString());
                        ItemModel itemToEdit = task.getResult().toObject(ItemModel.class);

                        for(int i = 0; i < itemToEdit.getAmounts().size(); i++) {
                            int adjustedAmount = itemToEdit.getAmounts().get(i) - item.getAmounts().get(i);
                            adjustedAmountsList.add(adjustedAmount);

                            //TODO:Merge this with fetchItem method

                            itemDocumentRef.update("amounts", adjustedAmountsList)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("adjustInventory", "Succesful amount update");
                                        }
                                    });
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("FIRESTORE", "task not successful");
                }
            });

        }
    }

    private void addReceiptToDB() {
        Map<String, Object> newReceipt = new HashMap<>();
        newReceipt.put("time", receipt.getTime());
        newReceipt.put("user", receipt.getUser());
        newReceipt.put("employee", receipt.getEmployee());
        newReceipt.put("storeID", receipt.getStoreID());
        newReceipt.put("total", receipt.getTotal());
        newReceipt.put("annulled", false);

        DocumentReference newReceiptRef = database.collection("receipts").document();

        newReceiptRef.set(newReceipt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addReceiptToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addReceiptToDB", "onFailure: ");
                    }
                });

        //TODO: make this ItemModelMethod
        for(ItemModel item : receipt.getItems()) {
            Map<String, Object> newReceiptItem = new HashMap<>();
            newReceiptItem.put("added", item.getAdded());
            newReceiptItem.put("image", item.getImage());
            newReceiptItem.put("price", item.getPrice());
            newReceiptItem.put("rating", item.getRating());
            newReceiptItem.put("type", item.getType());
            newReceiptItem.put("sizes", item.getSizes());
            newReceiptItem.put("amounts", item.getAmounts());

            newReceiptRef.collection("items").document(item.toString())
                    .set(newReceiptItem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("addItemToReceipt", "onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addItemToReceipt", "onFailure: ");
                        }
                    });
        }

        if(editReceipt)
            annulReceipt();
    }

    private void annulReceipt() {
        DocumentReference receiptRef = database.collection("receipts").document(receipt.getReceiptID());
        receiptRef.update("annulled", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("annulReceipt", "Receipt annulled");
            }
        });
    }

    private void setItemPreview() {
        StorageReference imageRef = storageRef.child(currentItem.getImage());
        Glide.with(getContext())
                .asBitmap()
                .load(imageRef)
                .into(shoeImage);

        shoePriceTextView.setText("Cijena: " + (int)currentItem.getPrice() + "kn");
        loadingBar.setVisibility(View.INVISIBLE);
    }

    private void clearItemPreview() {
        currentItem = null;
        shoeImage.setImageResource(R.drawable.running_shoe_icon);
        shoePriceTextView.setText("Cijena: ");
        availableImageView.setVisibility(View.INVISIBLE);
        loadingBar.setVisibility(View.INVISIBLE);
        sizeSpinner.setAdapter(null);
    }

    private void clearInput() {
        clearItemPreview();
        colorSpinner.setSelection(0);
        addButton.setEnabled(false);
    }

    private void clearDataAndUI() {
        //TODO: Get real values
        receipt = new ReceiptModel();
        receipt.setEmployee(user.getEmail());
        receipt.setUser("");
        receipt.setStoreID("TestShop1");
        itemsToRemove = new ArrayList<>();
        editReceipt = false;

        initRecyclerView();

        AlertDialog.Builder successfulReceipt = new AlertDialog.Builder(getContext());
        successfulReceipt.setMessage("Uspješno ste zapisali račun u bazu podataka!");
        successfulReceipt.setCancelable(true);
        successfulReceipt.show();

    }
}