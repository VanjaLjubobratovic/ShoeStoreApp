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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private EditText modelEt, colorEt, sizeEt;
    private MaterialButton addButton, confirmButton;
    private ImageView shoeImage, availableImageView;
    private TextView shoePriceTextView, totalTextView;
    private ProgressBar loadingBar;

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference itemsRef;
    private StorageReference storageRef;

    private ItemModel currentItem;
    private ReceiptModel receipt;
    private UserModel user;

    public ReceiptFragment() {
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

        receipt = new ReceiptModel();
        receipt.setEmployee(user.getEmail());
        receipt.setUser("");
        //TODO:get real value
        receipt.setStoreID("TestShop1");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modelEt = view.findViewById(R.id.modelEditText);
        colorEt = view.findViewById(R.id.colorEditText);
        sizeEt = view.findViewById(R.id.sizeEditText);

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

        modelEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadingBar.setVisibility(View.VISIBLE);
                fetchItems(modelEt.getText().toString(), colorEt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        colorEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadingBar.setVisibility(View.VISIBLE);
                fetchItems(modelEt.getText().toString(), colorEt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sizeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addButton.setEnabled(false);

                if(sizeEt.getText().toString().length() == 0)
                    return;

                Integer size = Integer.valueOf(sizeEt.getText().toString());
                Log.d("SIZE", "size = " + size);

                if(currentItem != null && size > 9) {
                    sizeIndex = currentItem.getSizes().indexOf(size);
                    if(sizeIndex == -1) {
                        showErrorCredentials(sizeEt, "Nepostojeća veličina");
                        return;
                    }

                    if(currentItem.getAmounts().get(sizeIndex) > 0) {
                        availableImageView.setImageResource(R.drawable.confirmicon);
                    } else {
                        availableImageView.setImageResource(R.drawable.unavailableicon);
                    }

                    addButton.setEnabled(true);
                    availableImageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:add item to recycler view
                sizeEt.clearFocus();
                colorEt.clearFocus();
                modelEt.clearFocus();
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


    private void fetchItems(String model, String color) {
        //TODO: generalize this method
        itemsRef.document(model + "-" + color).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    if (task.getResult().getData() == null) {
                        Log.d("FIRESTORE", "Item not found");
                        clearItemPreview();
                        return;
                    }
                    Log.d("FIRESTORE", task.getResult().toString());
                    currentItem = task.getResult().toObject(ItemModel.class);
                    currentItem.parseModelColor(model + "-" + color);
                    setItemPreview();
                } else {
                    Log.d("FIRESTORE", "task not successful");
                    clearItemPreview();
                }
            }
        });
    }

    private void initRecyclerView() {
        Log.d("RECYCLER VIEW", "initRecyclerView: ");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = this.getView().findViewById(R.id.receiptRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ReceiptRecyclerViewAdapter adapter = new ReceiptRecyclerViewAdapter(getContext(), receipt, totalTextView, confirmButton);
        recyclerView.setAdapter(adapter);
    }

    private void adjustInventory() {
        //TODO: find if there's a better method
        for(ItemModel item : receipt.getItems()) {
            DocumentReference itemDocumentRef = itemsRef.document(item.toString());
            ArrayList<Integer> adjustedAmountsList = new ArrayList<>();

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
    }

    private void clearInput() {
        clearItemPreview();
        modelEt.setText("");
        colorEt.setText("");
        sizeEt.setText("");
        addButton.setEnabled(false);
    }

    private void clearDataAndUI() {
        //TODO: Get real values
        receipt = new ReceiptModel();
        receipt.setEmployee(user.getEmail());
        receipt.setUser("");
        receipt.setStoreID("TestShop1");
        initRecyclerView();

        AlertDialog.Builder successfulReceipt = new AlertDialog.Builder(getContext());
        successfulReceipt.setMessage("Uspješno ste zapisali račun u bazu podataka!");
        successfulReceipt.setCancelable(true);
        successfulReceipt.show();

    }

    private void showErrorCredentials(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
}